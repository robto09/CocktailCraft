package com.cocktailcraft.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileProtectionComplete
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
class DatabaseFileProtectionTest {

    private val fileManager = NSFileManager.defaultManager
    private val directory = NSTemporaryDirectory() + "db-protection-" + NSUUID().UUIDString
    private val database = "$directory/test.db"

    init {
        fileManager.createDirectoryAtPath(
            directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }

    @AfterTest
    fun cleanUp() {
        fileManager.removeItemAtPath(directory, error = null)
    }

    private fun create(path: String) {
        assertTrue(fileManager.createFileAtPath(path, contents = null, attributes = null), "create $path")
    }

    private fun isExcludedFromBackup(path: String): Boolean =
        NSURL.fileURLWithPath(path)
            .resourceValuesForKeys(listOf(NSURLIsExcludedFromBackupKey), error = null)
            ?.get(NSURLIsExcludedFromBackupKey) == true

    @Test
    fun databaseItsSideFilesAndItsDirectoryAreExcludedFromBackup() {
        val files = listOf(database, "$database-wal", "$database-shm")
        files.forEach(::create)

        protectDatabaseFiles(database)

        (files + directory).forEach { assertTrue(isExcludedFromBackup(it), "$it excluded from backup") }
    }

    @Test
    fun protectionIsCompleteWhereTheFileSystemRecordsIt() {
        create(database)

        protectDatabaseFiles(database)

        // The simulator's file system may not record a protection class;
        // where it does, it must be Complete.
        listOf(directory, database).forEach { path ->
            val protection = fileManager.attributesOfItemAtPath(path, error = null)?.get(NSFileProtectionKey)
            if (protection != null) assertEquals<Any?>(NSFileProtectionComplete, protection, path)
        }
    }

    @Test
    fun missingFilesAreSkippedAndRepeatingIsHarmless() {
        // Fresh install: nothing exists yet but the directory
        protectDatabaseFiles(database)
        assertTrue(isExcludedFromBackup(directory))

        create(database)
        protectDatabaseFiles(database)
        protectDatabaseFiles(database)
        assertTrue(isExcludedFromBackup(database))
    }
}

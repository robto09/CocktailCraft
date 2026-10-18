package com.cocktailcraft.data.local

import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileProtectionComplete
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey

/**
 * Keeps the SQLite database at [databasePath] — orders with delivery
 * addresses, cart, favorites, reviews — out of iCloud/Finder backups and
 * encrypted whenever the device is locked. Call before the driver opens it;
 * safe to repeat on every launch.
 *
 * Both attributes are set on the database's directory as well as on the
 * files: SQLite deletes and recreates its -wal/-shm (and -journal) files as
 * connections come and go, and a recreated file would lose a per-file
 * flag. A directory excluded from backup excludes everything in it, and
 * new files take their directory's protection class. The existing files
 * are updated explicitly, because a directory's class does not change files
 * already in it (installs from before this change).
 *
 * NSFileProtectionComplete rather than CompleteUntilFirstUserAuthentication:
 * nothing needs the database while the device is locked. The widget
 * extension never opens it (it reads the JSON snapshots WidgetDataBridge
 * writes to the App Group container), and the app declares no background
 * modes, so it is suspended within seconds of backgrounding — before the
 * key is evicted about ten seconds after a lock. The one database read at
 * backgrounding (WidgetDataBridge's random-pool refresh) goes through
 * repositories that return a Result, so even an access after eviction
 * fails soft and the widget keeps its previous pool.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun protectDatabaseFiles(databasePath: String) {
    val fileManager = NSFileManager.defaultManager
    val directory = databasePath.substringBeforeLast('/')
    val paths = listOf(directory, databasePath) + SqliteSideFileSuffixes.map { databasePath + it }
    paths.filter { fileManager.fileExistsAtPath(it) }.forEach { path ->
        val protected = fileManager.setAttributes(
            mapOf(NSFileProtectionKey to NSFileProtectionComplete),
            ofItemAtPath = path,
            error = null
        )
        val excluded = NSURL.fileURLWithPath(path)
            .setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
        if (!protected || !excluded) {
            val name = path.substringAfterLast('/')
            Logger.withTag("DatabaseFileProtection").w {
                "Could not fully protect $name: protection=$protected, backupExcluded=$excluded"
            }
        }
    }
}

private val SqliteSideFileSuffixes = listOf("-wal", "-shm", "-journal")

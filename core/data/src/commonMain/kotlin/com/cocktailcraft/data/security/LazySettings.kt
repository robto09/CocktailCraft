package com.cocktailcraft.data.security

import com.russhwolf.settings.Settings

/**
 * A [Settings] whose backing store is created on first use instead of when
 * Koin resolves it.
 *
 * The secure store is expensive to open — on Android, Keystore access,
 * Tink keyset parsing, decrypting the store file and the one-time
 * migrations; on iOS, Keychain reads plus the migration —
 * and it sits on the first frame's injection path (MainActivity's theme
 * ViewModel → ManageProfileUseCase → AuthRepository). Wrapping it here keeps
 * resolution free: every AuthRepositoryImpl access already runs inside
 * withContext(ioDispatcher), so the store is now opened there, off the main
 * thread. [lazy] is synchronized, so concurrent first calls open it once; if
 * opening throws, the call fails (and surfaces as an auth Result.Error)
 * and the next call tries again.
 */
internal class LazySettings(private val delegate: Lazy<Settings>) : Settings {

    constructor(create: () -> Settings) : this(lazy(create))

    private val settings: Settings get() = delegate.value

    override val keys: Set<String> get() = settings.keys
    override val size: Int get() = settings.size

    override fun clear() = settings.clear()
    override fun remove(key: String) = settings.remove(key)
    override fun hasKey(key: String): Boolean = settings.hasKey(key)

    override fun putInt(key: String, value: Int) = settings.putInt(key, value)
    override fun getInt(key: String, defaultValue: Int): Int = settings.getInt(key, defaultValue)
    override fun getIntOrNull(key: String): Int? = settings.getIntOrNull(key)

    override fun putLong(key: String, value: Long) = settings.putLong(key, value)
    override fun getLong(key: String, defaultValue: Long): Long = settings.getLong(key, defaultValue)
    override fun getLongOrNull(key: String): Long? = settings.getLongOrNull(key)

    override fun putString(key: String, value: String) = settings.putString(key, value)
    override fun getString(key: String, defaultValue: String): String = settings.getString(key, defaultValue)
    override fun getStringOrNull(key: String): String? = settings.getStringOrNull(key)

    override fun putFloat(key: String, value: Float) = settings.putFloat(key, value)
    override fun getFloat(key: String, defaultValue: Float): Float = settings.getFloat(key, defaultValue)
    override fun getFloatOrNull(key: String): Float? = settings.getFloatOrNull(key)

    override fun putDouble(key: String, value: Double) = settings.putDouble(key, value)
    override fun getDouble(key: String, defaultValue: Double): Double = settings.getDouble(key, defaultValue)
    override fun getDoubleOrNull(key: String): Double? = settings.getDoubleOrNull(key)

    override fun putBoolean(key: String, value: Boolean) = settings.putBoolean(key, value)
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = settings.getBoolean(key, defaultValue)
    override fun getBooleanOrNull(key: String): Boolean? = settings.getBooleanOrNull(key)
}

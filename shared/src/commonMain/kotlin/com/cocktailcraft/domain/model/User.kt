package com.cocktailcraft.domain.model

import com.cocktailcraft.designsystem.AccentColorTokens
import kotlin.native.HiddenFromObjC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String = "",
    val email: String,
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val isEmailVerified: Boolean = false,
    val joinDate: String? = null,
    val address: Address? = null,
    val preferences: Map<String, String> = emptyMap()
) {
    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}
@Serializable
data class Address(
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = ""
) {
    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}
@Serializable
data class UserPreferences(
    val darkMode: Boolean = false,
    val followSystemTheme: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val language: String = "en",
    // Defaults let JSON persisted before these fields existed still decode.
    // Serialized as "accent" (not the original "accentColor") on purpose:
    // the accent setting shipped non-functional with a "blue" default, so
    // any persisted "accentColor" value was never a choice the user saw
    // honored. Renaming the key resets everyone to the brand coral default
    // the moment accents actually start working.
    @SerialName("accent")
    val accentColor: String = AccentColorTokens.DEFAULT,
    val fontSize: String = "medium",
    val isHighContrast: Boolean = false,
    val isReducedMotion: Boolean = false
) {
    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}
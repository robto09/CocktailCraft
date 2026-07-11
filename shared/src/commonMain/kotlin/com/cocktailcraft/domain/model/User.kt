package com.cocktailcraft.domain.model

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
)

@Serializable
data class Address(
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = ""
)

@Serializable
data class UserPreferences(
    val darkMode: Boolean = false,
    val followSystemTheme: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val language: String = "en"
)
package com.cocktailcraft.android.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.BuildConfig
import com.cocktailcraft.android.ui.components.AnimatedThemeToggleRow
import com.cocktailcraft.android.ui.components.SettingsItem
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.change_password
import com.cocktailcraft.resources.dark_mode
import com.cocktailcraft.resources.edit_profile
import com.cocktailcraft.resources.email_preferences
import com.cocktailcraft.resources.follow_system_theme
import com.cocktailcraft.resources.help_support
import com.cocktailcraft.resources.logout
import com.cocktailcraft.resources.notification_settings
import com.cocktailcraft.resources.order_history
import com.cocktailcraft.resources.profile_about
import com.cocktailcraft.resources.profile_about_app_name
import com.cocktailcraft.resources.profile_account_settings
import com.cocktailcraft.resources.profile_app_settings
import com.cocktailcraft.resources.profile_controlled_by_system
import com.cocktailcraft.resources.profile_copyright
import com.cocktailcraft.resources.profile_toggle_off
import com.cocktailcraft.resources.profile_toggle_on
import com.cocktailcraft.resources.profile_version_format

// The Profile screen's settings cards: account, app (theme, notifications,
// cache, sign-out) and about.

@Composable
internal fun AccountSettingsCard(
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.lg),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(MR.strings.profile_account_settings),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = Spacing.lg)
                    .semantics { heading() }
            )

            SettingsItem(
                icon = Icons.Default.Person,
                title = stringResource(MR.strings.edit_profile),
                onClick = onEditProfileClick
            )

            SettingsItem(
                icon = Icons.Default.Lock,
                title = stringResource(MR.strings.change_password),
                onClick = onChangePasswordClick
            )

            SettingsItem(
                icon = Icons.Default.Email,
                title = stringResource(MR.strings.email_preferences),
                onClick = { /* Handle email preferences */ }
            )

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = stringResource(MR.strings.notification_settings),
                onClick = { /* Handle notification settings */ }
            )
        }
    }
}

@Composable
internal fun AppSettingsCard(
    isLoggedIn: Boolean,
    isDarkMode: Boolean,
    isSystemTheme: Boolean,
    onOrderHistoryClick: () -> Unit,
    onToggleFollowSystemTheme: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.lg),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(MR.strings.profile_app_settings),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = Spacing.lg)
                    .semantics { heading() }
            )

            SettingsItem(
                icon = Icons.Default.DateRange,
                title = stringResource(MR.strings.order_history),
                onClick = onOrderHistoryClick
            )

            SettingsItem(
                icon = Icons.AutoMirrored.Filled.Help,
                title = stringResource(MR.strings.help_support),
                onClick = { /* Handle help & support */ }
            )

            // Follow System Theme Toggle with animated switch
            AnimatedThemeToggleRow(
                title = stringResource(MR.strings.follow_system_theme),
                subtitle = if (isSystemTheme) {
                    stringResource(
                        MR.strings.profile_toggle_on
                    )
                } else {
                    stringResource(MR.strings.profile_toggle_off)
                },
                icon = Icons.Default.DateRange,
                isChecked = isSystemTheme,
                onToggle = onToggleFollowSystemTheme,
                modifier = Modifier.fillMaxWidth()
            )

            // Dark Mode Toggle with animated switch (only enabled if not following system theme)
            AnimatedThemeToggleRow(
                title = stringResource(MR.strings.dark_mode),
                subtitle = if (isSystemTheme) {
                    stringResource(MR.strings.profile_controlled_by_system)
                } else {
                    if (isDarkMode) {
                        stringResource(
                            MR.strings.profile_toggle_on
                        )
                    } else {
                        stringResource(MR.strings.profile_toggle_off)
                    }
                },
                icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                isChecked = isDarkMode,
                onToggle = onToggleDarkMode,
                enabled = !isSystemTheme,
                modifier = Modifier.fillMaxWidth()
            )

            // Only show logout if signed in
            if (isLoggedIn) {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = stringResource(MR.strings.logout),
                    onClick = onLogoutClick,
                    textColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
internal fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(MR.strings.profile_about),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = Spacing.lg)
                    .semantics { heading() }
            )

            Text(
                text = stringResource(MR.strings.profile_about_app_name),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = stringResource(MR.strings.profile_version_format, BuildConfig.VERSION_NAME),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = stringResource(MR.strings.profile_copyright),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "Account settings", showBackground = true)
@Composable
private fun AccountSettingsCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        AccountSettingsCard()
    }
}

@Preview(name = "App settings", showBackground = true)
@Composable
private fun AppSettingsCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        AppSettingsCard(
            isLoggedIn = true,
            isDarkMode = false,
            isSystemTheme = false,
            onOrderHistoryClick = {},
            onToggleFollowSystemTheme = {},
            onToggleDarkMode = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "App settings (dark)", showBackground = true)
@Composable
private fun AppSettingsCardDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        AppSettingsCard(
            isLoggedIn = true,
            isDarkMode = true,
            isSystemTheme = false,
            onOrderHistoryClick = {},
            onToggleFollowSystemTheme = {},
            onToggleDarkMode = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "App settings — large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun AppSettingsCardLargeFontPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        AppSettingsCard(
            isLoggedIn = true,
            isDarkMode = false,
            isSystemTheme = true,
            onOrderHistoryClick = {},
            onToggleFollowSystemTheme = {},
            onToggleDarkMode = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "About", showBackground = true)
@Composable
private fun AboutCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        AboutCard()
    }
}

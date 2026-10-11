package com.cocktailcraft.android.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc

// UI-edge resolution of the shared strings (MR.strings / MR.plurals, defined
// once in core/resources/src/commonMain/moko-resources). moko generates them as
// Android string resources in the :core:resources module, so these are thin overloads
// of the Compose/Context lookups that keep call sites terse:
// stringResource(MR.strings.cart_empty_title).

@Composable
@ReadOnlyComposable
fun stringResource(resource: StringResource): String =
    androidx.compose.ui.res.stringResource(resource.resourceId)

@Composable
@ReadOnlyComposable
fun stringResource(resource: StringResource, vararg formatArgs: Any): String =
    androidx.compose.ui.res.stringResource(resource.resourceId, *formatArgs)

@Composable
@ReadOnlyComposable
fun pluralStringResource(resource: PluralsResource, count: Int, vararg formatArgs: Any): String =
    androidx.compose.ui.res.pluralStringResource(resource.resourceId, count, *formatArgs)

/**
 * Resolves a shared-code [StringDesc] (e.g. a UserFriendlyError title) for
 * the current configuration; reading LocalConfiguration re-resolves it when
 * the locale changes.
 */
@Composable
@ReadOnlyComposable
fun StringDesc.resolve(): String {
    LocalConfiguration.current
    return toString(LocalContext.current)
}

/** Context-based lookup for non-Compose callers (Glance widgets, workers). */
fun Context.getString(resource: StringResource, vararg formatArgs: Any): String =
    getString(resource.resourceId, *formatArgs)

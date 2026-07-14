package com.cocktailcraft.android.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.cocktailcraft.android.MainActivity
import com.cocktailcraft.android.navigation.COCKTAIL_DEEP_LINK_BASE_PATH

/**
 * Explicit VIEW intent that opens [MainActivity] on the given cocktail's
 * detail screen via the navDeepLink registered on CocktailDetailRoute.
 */
internal fun cocktailDetailIntent(context: Context, cocktailId: String): Intent =
    Intent(
        Intent.ACTION_VIEW,
        "$COCKTAIL_DEEP_LINK_BASE_PATH/${Uri.encode(cocktailId)}".toUri(),
        context,
        MainActivity::class.java
    )

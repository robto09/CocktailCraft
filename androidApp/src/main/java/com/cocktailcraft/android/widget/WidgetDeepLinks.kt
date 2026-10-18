package com.cocktailcraft.android.widget

import android.content.Context
import android.content.Intent
import com.cocktailcraft.android.MainActivity
import com.cocktailcraft.android.navigation.CocktailDeepLink

/**
 * Explicit VIEW intent that opens [MainActivity] on the given cocktail's
 * detail screen via the navDeepLink registered on CocktailDetailRoute.
 */
internal fun cocktailDetailIntent(context: Context, cocktailId: String): Intent =
    Intent(
        Intent.ACTION_VIEW,
        CocktailDeepLink.uriFor(cocktailId),
        context,
        MainActivity::class.java
    )

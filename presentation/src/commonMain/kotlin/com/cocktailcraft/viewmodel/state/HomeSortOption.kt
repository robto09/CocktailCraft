package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.home_sort_default
import com.cocktailcraft.resources.home_sort_name_asc
import com.cocktailcraft.resources.home_sort_popularity
import com.cocktailcraft.resources.home_sort_price_asc
import com.cocktailcraft.resources.home_sort_price_desc
import com.cocktailcraft.resources.home_sort_rating
import dev.icerock.moko.resources.StringResource

/**
 * How the Home list is ordered. [DEFAULT] is the catalog's own order (the
 * stored category page, or search results as returned). [label] is the
 * shared, localized option name both platforms show in their sort menu.
 */
enum class HomeSortOption(val label: StringResource) {
    DEFAULT(MR.strings.home_sort_default),
    NAME_ASC(MR.strings.home_sort_name_asc),
    PRICE_ASC(MR.strings.home_sort_price_asc),
    PRICE_DESC(MR.strings.home_sort_price_desc),
    RATING(MR.strings.home_sort_rating),
    POPULARITY(MR.strings.home_sort_popularity)
}

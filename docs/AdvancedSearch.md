# Advanced Search and Filtering

This document describes the Advanced Search and Filtering functionality in CocktailCraft.

## Overview

Advanced search lets users narrow the cocktail list by combining several criteria at once. The filter set is deliberately limited to **four dimensions TheCocktailDB free tier (`v1/1`) actually supports**:

| Filter | Endpoint | Notes |
|---|---|---|
| Query (name) | `search.php?s=` | Returns full drink records |
| Category | `filter.php?c=` | Returns slim records (id, name, thumb) |
| Ingredient (single) | `filter.php?i=` | Slim; multi-ingredient is a paid v2 feature |
| Alcoholic | `filter.php?a=` | Tri-state: any / alcoholic / non-alcoholic |

(The API also supports filtering by glass via `filter.php?g=`, but the glass filter was dropped from the product as low-value.)

Everything the API cannot supply — price, rating, popularity, taste profile, complexity, preparation time, multi/exclude ingredients — has been removed. Those were fabricated or keyword-guessed client-side and never reached any filtering logic.

## Architecture: one call per filter, ID-set intersection

TheCocktailDB's `filter.php` accepts only **one parameter per request** and cannot combine dimensions server-side. However, each call returns the *complete* ID set for its dimension, so combined filtering is done by **intersecting those ID sets on the client**:

1. **Offline** — filter the in-memory cocktail cache directly on the four real fields.
2. **Nothing active** — return the default listing (`filterByCategory("Cocktail")`).
3. Otherwise, make **one API call per active filter**, in priority order (query → category → ingredient → alcoholic), each routed through the existing rate limiter.
4. The **base list** is the first active result (query wins, so full records are preferred over slim ones).
5. The **result** keeps only the cocktails whose id appears in *every* other active set.
6. If any active filter call fails, the whole search returns `Result.Error` — a filter is never silently dropped.

This is accurate (every id comes from the server), needs no per-drink hydration, and invents no data. Cost is one sequential call per active filter; typical usage is one or two filters. Slim results render with the same "tap to view" placeholder cards used elsewhere for category browsing, and the detail view hydrates on tap.

The implementation lives in `CocktailRepositoryImpl.advancedSearch(filters)` (`shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/CocktailRepositoryImpl.kt`), reached through `SearchCocktailsUseCase.search(filters)`.

## Shared state and ViewModel

`SharedHomeViewModel` (`shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedHomeViewModel.kt`) is the single entry point for both platforms:

- **`applyFilters(filters: SearchFilters)`** — stores `filters` in `uiState.searchFilters` (so the active-filter chips reflect them) and loads the intersected results.
- **`clearSearchFilters()`** — resets to an empty `SearchFilters()` and reloads the default list.
- **`searchCocktails(query)`** — composes the query with any active filters (`searchFilters.copy(query = query)`), so the search bar and the advanced filters work together rather than overriding each other.
- **`loadFilterOptions()`** — populates the API-backed option lists `uiState.filterCategories` / `filterIngredients` via `list.php` (`?c/i=list`) through `CocktailCatalogRepository`. Both platforms consume the same lists.

`SearchFilters` (`shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/SearchFilters.kt`) is a four-field data class with a single `hasActiveFilters()` helper:

```kotlin
data class SearchFilters(
    val query: String = "",
    val category: String? = null,
    val ingredient: String? = null,   // single ingredient
    val alcoholic: Boolean? = null,   // null = any
)
```

## Platform UI

Both platforms drive the same shared ViewModel; the filter options and active-filter chips come from shared state.

### Android — expandable panel

- `ExpandableAdvancedSearchPanel` (`androidApp/.../ui/components/ExpandableAdvancedSearchPanel.kt`) is an inline, collapsible panel with three sections: **Category** (dropdown), **Ingredient** (single-select searchable dialog), **Alcoholic** (Any / Alcoholic / Non-Alcoholic). Apply forwards the whole `SearchFilters` to `viewModel.applyFilters(...)`; Clear calls `viewModel.clearSearchFilters()`.
- `SearchFilterChips` (`androidApp/.../ui/components/SearchFilterChips.kt`) renders the active filters from `state.searchFilters`; dismissing a chip re-applies the reduced set.
- `HomeScreen` reads the option lists from `state.filterCategories/filterIngredients` and calls `loadFilterOptions()` when advanced search opens.

### iOS — filter sheet

- `AdvancedSearchSheet` (in `iosApp/CocktailCraft/Views/HomeViewSKIE.swift`) is a SwiftUI `Form` sheet with the same three controls: Category picker, Ingredient picker, Alcoholic segmented control. It calls `loadFilterOptions()` on appear, Apply calls `applyFilters(...)`, Clear calls `clearSearchFilters()`.
- The active-filter chip row in `HomeViewSKIE` is derived from `state.searchFilters` (no local filter state).
- `HomeViewModelSKIE` (`iosApp/CocktailCraft/ViewModels/HomeViewModelSKIE.swift`) exposes `applyFilters`, `loadFilterOptions` (async, via SKIE) and `clearSearchFilters` (sync). The Kotlin `Boolean?` alcoholic value bridges to Swift as `KotlinBoolean?` (nil = any).

## Data flow

1. User opens advanced search (Android panel / iOS sheet); the option lists load via `loadFilterOptions()`.
2. User selects filters and taps Apply; a `SearchFilters` is built and passed to `applyFilters(filters)`.
3. The ViewModel stores the filters in state and calls `SearchCocktailsUseCase.search(filters)`.
4. `advancedSearch` performs one call per active filter and intersects the ID sets.
5. Results replace `state.cocktails`; the active filters appear as chips, each dismissable.

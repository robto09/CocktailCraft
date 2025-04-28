# Advanced Search and Filtering

This document describes the implementation of the Advanced Search and Filtering functionality in the CocktailCraft application.

## Overview

The Advanced Search and Filtering system allows users to search for cocktails using multiple criteria simultaneously, providing a powerful way to discover cocktails that match specific preferences. The system supports filtering by various attributes including:

- Category
- Ingredients (both inclusion and exclusion)
- Alcoholic/Non-alcoholic
- Glass type
- Price range
- Taste profile
- Complexity level
- Preparation time

## Implementation Details

### Key Components

1. **SearchFilters Model**
   - Located in `shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/SearchFilters.kt`
   - Represents all possible filter criteria as a data class
   - Includes helper methods for checking active filters and generating descriptions

2. **AdvancedSearchPanel**
   - Located in `androidApp/src/main/java/com/cocktailcraft/ui/components/AdvancedSearchPanel.kt`
   - Provides a comprehensive UI for setting and applying multiple filters
   - Implemented as a modal dialog with collapsible sections for each filter type

3. **Filter Components**
   - `FilterSection`: Collapsible section for organizing filter controls
   - `CategorySelector`: Dropdown for selecting cocktail categories
   - `IngredientSelector`: Multi-select interface for including/excluding ingredients
   - `AlcoholicFilterContent`: Toggle switches for alcoholic/non-alcoholic options
   - `GlassSelector`: Dropdown for selecting glass types
   - `PriceRangeFilterContent`: Range slider for setting price boundaries
   - `TasteProfileSelector`: Chip-based selector for taste profiles
   - `ComplexitySelector`: Chip-based selector for complexity levels
   - `PrepTimeSelector`: Chip-based selector for preparation times

4. **SearchFilterChips**
   - Located in `androidApp/src/main/java/com/cocktailcraft/ui/components/SearchFilterChips.kt`
   - Displays active filters as chips for quick visibility and removal
   - Provides a "Clear All" option to reset all filters

### Filter Types

1. **Category Filter**
   - Allows filtering by cocktail category (e.g., "Cocktail", "Shot", "Ordinary Drink")
   - Implemented as a dropdown selector with "All Categories" option

2. **Ingredient Filter**
   - Supports both inclusion and exclusion of ingredients
   - Allows multiple ingredients to be selected for each
   - Provides a searchable dialog for finding specific ingredients

3. **Alcoholic Filter**
   - Toggle switches for showing only alcoholic or non-alcoholic drinks
   - Mutually exclusive options with ability to clear both

4. **Glass Type Filter**
   - Dropdown selector for filtering by glass type
   - Includes "All Glass Types" option to clear the filter

5. **Price Range Filter**
   - Range slider for setting minimum and maximum price
   - Toggle to enable/disable price filtering

6. **Taste Profile Filter**
   - Chip-based selector for taste profiles (Sweet, Sour, Bitter, etc.)
   - Based on the `TasteProfile` enum

7. **Complexity Filter**
   - Chip-based selector for complexity levels (Easy, Medium, Complex)
   - Based on the `Complexity` enum

8. **Preparation Time Filter**
   - Chip-based selector for preparation times (Quick, Medium, Long)
   - Based on the `PreparationTime` enum

### Data Flow

1. User opens the Advanced Search panel from the main search interface
2. User selects desired filters across multiple categories
3. On "Apply Filters", the selected criteria are combined into a `SearchFilters` object
4. The `SearchFilters` object is passed to the repository layer via the ViewModel
5. The repository applies the filters to retrieve matching cocktails
6. Results are displayed to the user with active filters shown as chips

### Repository Implementation

The `CocktailRepositoryImpl` class handles the application of filters:

1. Starts with a base list of cocktails (from search, category, or all cocktails)
2. Applies each filter sequentially to narrow down results
3. For complex filters like taste profile, uses heuristics based on ingredients and descriptions
4. Returns a filtered list of cocktails that match all criteria

## Usage Example

```kotlin
// Create a SearchFilters object with multiple criteria
val filters = SearchFilters(
    query = "Margarita",
    category = "Cocktail",
    ingredients = listOf("Tequila", "Lime Juice"),
    excludeIngredients = listOf("Salt"),
    alcoholic = true,
    tasteProfile = TasteProfile.SOUR,
    complexity = Complexity.MEDIUM
)

// Apply filters through the ViewModel
viewModel.applyFilters(filters)

// The UI will update to show matching cocktails
```

## UI Components

The Advanced Search UI is designed for usability and clarity:

- **Collapsible Sections**: Each filter type has its own collapsible section
- **Visual Indicators**: Active filters are highlighted and summarized
- **Chip-Based Selection**: Where appropriate, filters use chips for easy selection
- **Searchable Dialogs**: For large lists like ingredients, searchable dialogs are provided
- **Clear Options**: Each filter can be individually cleared, and a "Clear All" option is available

## Future Enhancements

1. **Saved Filters**: Allow users to save and name their favorite filter combinations
2. **Filter Suggestions**: Suggest popular filter combinations based on user preferences
3. **Visual Filter Builder**: Provide a more visual interface for building complex filter combinations
4. **Filter Analytics**: Track which filters are most commonly used to improve the interface
5. **Voice Search Integration**: Allow setting filters via voice commands

## Technical Considerations

1. **Performance**: Filters are applied efficiently to minimize processing time
2. **Caching**: Frequently used filter results are cached to improve performance
3. **Offline Support**: Basic filtering works offline with cached cocktail data
4. **Extensibility**: The filter system is designed to be easily extended with new filter types

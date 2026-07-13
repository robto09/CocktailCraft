# Cocktail Recommendation System

This document outlines the implementation of the cocktail recommendation system in the CocktailCraft app, including the approach, limitations, and design decisions.

## API Limitations and Constraints

The CocktailCraft app uses the free tier of TheCocktailDB API, which has several limitations that influenced our recommendation system design:

1. **Limited API Endpoints**: The free tier only provides basic search and lookup functionality without dedicated recommendation endpoints.
2. **Rate Limiting**: Excessive API calls may be throttled or blocked.
3. **No User Tracking**: The API doesn't track user preferences or provide personalization features.
4. **Limited Data Access**: Some advanced data fields are only available in the paid tier.

## Recommendation System Options Considered

We evaluated several approaches for implementing a recommendation system within these constraints:

### 1. Server-side Recommendation Engine
- **Description**: Building a custom backend service to track user preferences and generate recommendations
- **Pros**: Powerful personalization, advanced algorithms
- **Cons**: Requires backend infrastructure, significant development effort, ongoing maintenance
- **Feasibility**: Low (given current project constraints)

### 2. Third-party Recommendation Service
- **Description**: Integrating with a specialized recommendation API
- **Pros**: Sophisticated algorithms, minimal development effort
- **Cons**: Additional costs, dependency on external service, data privacy concerns
- **Feasibility**: Medium (cost concerns)

### 3. Client-side Simple Recommendations
- **Description**: Implementing basic recommendations directly in the app
- **Pros**: No additional backend needed, works within API limitations
- **Cons**: Limited sophistication, less personalized
- **Feasibility**: High

### 4. Local History-Based Recommendations
- **Description**: Using locally stored user history to suggest similar cocktails
- **Pros**: Privacy-friendly, works offline, no additional API calls
- **Cons**: Limited to user's own history, cold start problem
- **Feasibility**: High

## Implemented Approach: Client-side Category-Based Related Cocktails

We implemented option 3 in its simplest useful form: a single category-based "related cocktails" strategy computed client-side from data the app already fetches. (An earlier multi-strategy `CocktailRecommendationEngine` was removed during the repository refactor; the sections above are kept as design history.)

### Key Components

1. **`GetCocktailDetailUseCase.getRelatedCocktails(cocktail, limit)`** (`shared/src/commonMain/kotlin/com/cocktailcraft/domain/usecase/GetCocktailDetailUseCase.kt`): fetches the current cocktail's category via `CocktailSearchRepository.filterByCategory(...)`, excludes the cocktail itself, and returns a small random sample (default 3). Any failure degrades to an empty list.
2. **`SharedCocktailDetailViewModel`**: exposes the result as `relatedCocktails` in the shared `DetailUiState`, consumed identically by both platforms.
3. **UI Components**: the "You might also like" section — `DetailRecommendationsSection` in the Android `CocktailDetailScreen`, `relatedCocktailsSection` in the iOS `CocktailDetailView`.

The Home screen additionally offers `SharedHomeViewModel.getCocktailsByCategory(category, limit)`, a purely local pick over the already-loaded list using a stable per-id sort key, so repeated calls return the same items (SH-10 determinism).

### Recommendation Strategy

```kotlin
suspend fun getRelatedCocktails(cocktail: Cocktail, limit: Int = 3): List<Cocktail> {
    return try {
        val cocktails = searchRepository.filterByCategory(cocktail.category ?: CocktailCategories.DEFAULT).getOrDefault(emptyList())
        cocktails.filter { it.id != cocktail.id }.shuffled().take(limit)
    } catch (e: Exception) {
        emptyList()
    }
}
```

### Optimization Techniques

1. **Caching**: `filterByCategory` goes through the shared cocktail cache, so recommendations usually cost no extra API call beyond what the category listing already spent
2. **Batched Loading**: The related list is loaded once per detail view, alongside the detail fetch
3. **Fallback Mechanisms**: Errors degrade to an empty list — the section hides rather than breaking the screen
4. **Offline Support**: When offline, the category filter is served from cached data, so recommendations still work without a connection

### UI Implementation

The recommendation UI is designed to be:

1. **Non-intrusive**: Displayed as a horizontal carousel below the main content
2. **Visually appealing**: Animated entry, consistent with app design language
3. **Performance-optimized**: Lazy loading, efficient image handling
4. **Gracefully degrading**: Shimmer while loading; an unobtrusive placeholder (Android) or a hidden section (iOS) when nothing is available

## Pros and Cons of the Implemented Approach

### Pros
1. **Works within API limitations**: No excessive API calls or paid tier requirements
2. **Enhances user experience**: Provides discovery without complex backend
3. **Offline-friendly**: Can work with cached data
4. **Scalable**: Can be enhanced later if we move to a paid API or custom backend

### Cons
1. **Limited sophistication**: Simpler than advanced recommendation algorithms
2. **Less personalized**: Limited user preference analysis
3. **Cold start issues**: Limited recommendations for new users
4. **API dependency**: Still relies on API data structure

## Future Enhancements

If the app grows and resources become available, we could enhance the recommendation system by:

1. **Implementing a custom backend**: For more sophisticated recommendation algorithms
2. **Adding collaborative filtering**: "Users who liked this also liked..."
3. **Incorporating explicit user preferences**: Allowing users to specify taste preferences
4. **Expanding data sources**: Integrating with additional cocktail databases
5. **Adding machine learning**: For more personalized recommendations

## Conclusion

Our hybrid client-side recommendation approach provides a valuable feature enhancement while working within the constraints of the free TheCocktailDB API. It balances user experience, technical feasibility, and resource constraints to deliver useful cocktail recommendations without requiring additional backend infrastructure or paid API tiers.

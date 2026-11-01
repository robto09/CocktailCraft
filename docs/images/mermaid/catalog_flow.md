# Catalog Flow

How a Home category page reaches the screen. The database is the single
source of truth: the screen observes the stored page and a refresh writes
into the database, so the observer sees new data through the same flow
(and keeps showing the stored page when offline). The ViewModel holds the
whole stored page and shows it a page at a time (`CategoryPages` in
`HomeListing.kt`), so scrolling for more makes no request.

```mermaid
flowchart TD
    Screen["Home screen<br/>(Compose / SwiftUI via HomeViewModelSKIE)"]
    VM["SharedHomeViewModel<br/>(:presentation)"]
    UC["LoadCocktailsByCategoryUseCase<br/>(:core:domain)"]
    Repo["CocktailCatalogRepository<br/>observeCategory / refreshCategory"]
    Fetcher["CocktailCategoryFetcher<br/>(:core:data)"]
    Cache["CocktailCache"]
    DB[("SQLDelight<br/>cached_cocktail + category_page")]
    Remote["CocktailRemoteDataSource"]
    Limiter["ApiRateLimiter"]
    API["TheCocktailDB API"]

    Screen -->|"uiState"| VM
    VM --> UC
    UC -->|"observe"| Repo
    UC -->|"invoke (refresh, then read)"| Repo
    Repo --> Fetcher
    Fetcher -->|"observe page"| Cache
    Fetcher -->|"refresh when stale or forced"| Remote
    Remote --> Limiter
    Remote --> API
    Fetcher -->|"write fetched cocktails + page"| Cache
    Cache <--> DB
```

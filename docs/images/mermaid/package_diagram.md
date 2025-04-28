# Package Diagram

```mermaid
graph TD
    %% Main package
    CocktailCraft[com.cocktailcraft]

    %% Android App
    AndroidApp[androidApp]
    JavaCocktailCraft[java.com.cocktailcraft]

    %% Screens
    Screens[screens]
    HomeScreen[HomeScreen]
    CartScreen[CartScreen]
    FavoritesScreen[FavoritesScreen]
    ProfileScreen[ProfileScreen]
    OrderListScreen[OrderListScreen]
    CocktailDetailScreen[CocktailDetailScreen]
    OfflineModeScreen[OfflineModeScreen]

    %% UI
    UI[ui]
    Components[components]
    CartItemCard[CartItemCard]
    CocktailItem[CocktailItem]
    FilterChip[FilterChip]
    RatingBar[RatingBar]
    WriteReviewDialog[WriteReviewDialog]
    ErrorDialog[ErrorDialog]
    ErrorBanner[ErrorBanner]

    Main[main]
    MainScreen[MainScreen]

    Theme[theme]
    ThemeFile[Theme]
    AnimatedTheme[AnimatedTheme]
    AppColors[AppColors]

    %% ViewModel
    ViewModel[viewmodel]
    BaseViewModel[BaseViewModel]
    HomeViewModel[HomeViewModel]
    CartViewModel[CartViewModel]
    FavoritesViewModel[FavoritesViewModel]
    ProfileViewModel[ProfileViewModel]
    OrderViewModel[OrderViewModel]
    ReviewViewModel[ReviewViewModel]
    ThemeViewModel[ThemeViewModel]
    OfflineModeViewModel[OfflineModeViewModel]
    CocktailDetailViewModel[CocktailDetailViewModel]

    %% Navigation
    Navigation[navigation]
    NavigationManager[NavigationManager]
    Screen[Screen]

    %% Model
    Model[model]
    SortOption[SortOption]
    UserPreferences[UserPreferences]

    %% Util
    AppUtil[util]
    ErrorUtils[ErrorUtils]

    %% Main Activity
    MainActivity[MainActivity]
    CocktailCraftApplication[CocktailCraftApplication]

    %% Shared Module
    Shared[shared]
    CommonMain[commonMain.kotlin.com.cocktailcraft]

    %% Domain
    Domain[domain]
    DomainModel[model]
    Cocktail[Cocktail]
    CocktailCartItem[CocktailCartItem]
    CocktailIngredient[CocktailIngredient]
    Order[Order]
    Review[Review]
    User[User]

    DomainRepository[repository]
    AuthRepository[AuthRepository]
    CartRepository[CartRepository]
    CocktailRepository[CocktailRepository]
    OrderRepository[OrderRepository]
    FavoritesRepository[FavoritesRepository]

    UseCase[usecase]
    PlaceOrderUseCase[PlaceOrderUseCase]
    ToggleFavoriteUseCase[ToggleFavoriteUseCase]

    Recommendation[recommendation]
    CocktailRecommendationEngine[CocktailRecommendationEngine]

    Config[config]
    AppConfig[AppConfig]

    Util[util]
    Result[Result]
    NetworkMonitor[NetworkMonitor]

    %% Data
    Data[data]
    DataRepository[repository]
    AuthRepositoryImpl[AuthRepositoryImpl]
    CartRepositoryImpl[CartRepositoryImpl]
    CocktailRepositoryImpl[CocktailRepositoryImpl]
    OrderRepositoryImpl[OrderRepositoryImpl]
    FavoritesRepositoryImpl[FavoritesRepositoryImpl]

    Cache[cache]
    CocktailCache[CocktailCache]

    Remote[remote]
    CocktailApi[CocktailApi]
    CocktailApiImpl[CocktailApiImpl]
    CocktailDto[CocktailDto]

    DataConfig[config]
    AppConfigImpl[AppConfigImpl]

    %% DI
    DI[di]
    AppModule[AppModule]
    DataModule[DataModule]
    DomainModule[DomainModule]
    NetworkModule[NetworkModule]
    PlatformModule[PlatformModule]
    RecommendationModule[RecommendationModule]

    %% Platform-specific
    AndroidMain[androidMain.kotlin.com.cocktailcraft]
    AndroidDI[di]
    AndroidPlatformModule[PlatformModule]
    AndroidNetworkMonitor[util.NetworkMonitor]

    IOSMain[iosMain.kotlin.com.cocktailcraft]
    IOSDI[di]
    IOSPlatformModule[PlatformModule]
    IOSNetworkMonitor[util.NetworkMonitor]

    %% Package Hierarchy
    CocktailCraft --> AndroidApp
    CocktailCraft --> Shared

    AndroidApp --> JavaCocktailCraft

    JavaCocktailCraft --> Screens
    JavaCocktailCraft --> UI
    JavaCocktailCraft --> ViewModel
    JavaCocktailCraft --> Navigation
    JavaCocktailCraft --> Model
    JavaCocktailCraft --> AppUtil
    JavaCocktailCraft --> MainActivity
    JavaCocktailCraft --> CocktailCraftApplication

    Screens --> HomeScreen
    Screens --> CartScreen
    Screens --> FavoritesScreen
    Screens --> ProfileScreen
    Screens --> OrderListScreen
    Screens --> CocktailDetailScreen
    Screens --> OfflineModeScreen

    UI --> Components
    UI --> Main
    UI --> Theme

    Components --> CartItemCard
    Components --> CocktailItem
    Components --> FilterChip
    Components --> RatingBar
    Components --> WriteReviewDialog
    Components --> ErrorDialog
    Components --> ErrorBanner

    Main --> MainScreen

    Theme --> ThemeFile
    Theme --> AnimatedTheme
    Theme --> AppColors

    AppUtil --> ErrorUtils

    ViewModel --> BaseViewModel
    ViewModel --> HomeViewModel
    ViewModel --> CartViewModel
    ViewModel --> FavoritesViewModel
    ViewModel --> ProfileViewModel
    ViewModel --> OrderViewModel
    ViewModel --> ReviewViewModel
    ViewModel --> ThemeViewModel
    ViewModel --> OfflineModeViewModel
    ViewModel --> CocktailDetailViewModel

    Navigation --> NavigationManager
    Navigation --> Screen

    Model --> SortOption
    Model --> UserPreferences

    Shared --> CommonMain
    Shared --> AndroidMain
    Shared --> IOSMain

    CommonMain --> Domain
    CommonMain --> Data
    CommonMain --> DI

    Domain --> DomainModel
    Domain --> DomainRepository
    Domain --> UseCase
    Domain --> Recommendation
    Domain --> Config
    Domain --> Util

    DomainModel --> Cocktail
    DomainModel --> CocktailCartItem
    DomainModel --> CocktailIngredient
    DomainModel --> Order
    DomainModel --> Review
    DomainModel --> User

    DomainRepository --> AuthRepository
    DomainRepository --> CartRepository
    DomainRepository --> CocktailRepository
    DomainRepository --> OrderRepository
    DomainRepository --> FavoritesRepository

    UseCase --> PlaceOrderUseCase
    UseCase --> ToggleFavoriteUseCase

    Recommendation --> CocktailRecommendationEngine

    Config --> AppConfig

    Util --> Result
    Util --> NetworkMonitor

    Data --> DataRepository
    Data --> Remote
    Data --> Cache
    Data --> DataConfig

    DataRepository --> AuthRepositoryImpl
    DataRepository --> CartRepositoryImpl
    DataRepository --> CocktailRepositoryImpl
    DataRepository --> OrderRepositoryImpl
    DataRepository --> FavoritesRepositoryImpl

    Cache --> CocktailCache

    Remote --> CocktailApi
    Remote --> CocktailApiImpl
    Remote --> CocktailDto

    DataConfig --> AppConfigImpl

    DI --> AppModule
    DI --> DataModule
    DI --> DomainModule
    DI --> NetworkModule
    DI --> PlatformModule
    DI --> RecommendationModule

    AndroidMain --> AndroidDI
    AndroidMain --> AndroidNetworkMonitor
    AndroidDI --> AndroidPlatformModule

    IOSMain --> IOSDI
    IOSMain --> IOSNetworkMonitor
    IOSDI --> IOSPlatformModule

    %% Dependencies between packages
    Screens -- ViewModel
    Screens -- Navigation
    Screens -- UI

    Main -- Screens
    Main -- Navigation
    Main -- ViewModel

    BaseViewModel -- ErrorUtils
    HomeViewModel -- BaseViewModel
    CartViewModel -- BaseViewModel
    FavoritesViewModel -- BaseViewModel
    ProfileViewModel -- BaseViewModel
    OrderViewModel -- BaseViewModel
    ReviewViewModel -- BaseViewModel
    ThemeViewModel -- BaseViewModel
    OfflineModeViewModel -- BaseViewModel
    CocktailDetailViewModel -- BaseViewModel

    ViewModel -- DomainRepository
    ViewModel -- UseCase
    ViewModel -- DomainModel
    ViewModel -- Recommendation
    ViewModel -- NetworkMonitor

    ThemeViewModel -- UserPreferences
    OfflineModeViewModel -- NetworkMonitor

    UseCase -- DomainRepository
    UseCase -- DomainModel

    CocktailRecommendationEngine -- CocktailRepository
    CocktailRecommendationEngine -- FavoritesRepository

    DataRepository -- DomainRepository
    DataRepository -- DomainModel
    DataRepository -- Remote
    DataRepository -- Cache
    DataRepository -- NetworkMonitor

    CocktailCache -- Cocktail

    DataConfig -- Config

    AppModule -- DataModule
    AppModule -- DomainModule
    AppModule -- NetworkModule

    DataModule -- DataRepository
    DataModule -- Remote
    DataModule -- Cache

    DomainModule -- UseCase
    DomainModule -- Config

    NetworkModule -- Remote
    NetworkModule -- NetworkMonitor

    RecommendationModule -- Recommendation
    RecommendationModule -- CocktailDetailViewModel

    CocktailCraftApplication -- DI
```

This package diagram shows the organization of the CocktailCraft codebase, including:

1. **Android App**: UI components, ViewModels, Navigation, and Utilities
   - New screens for Offline Mode
   - Error handling components
   - Animated theme components for Dark Mode
   - Base ViewModel with error handling
   - Theme and Offline Mode ViewModels

2. **Shared Module**: Domain and Data layers
   - Domain Layer: Models, Repository Interfaces, Use Cases, and Recommendation Engine
   - Data Layer: Repository Implementations, Remote Data Sources, Caching System, and Network Monitor
   - Cross-Cutting Concerns: Dependency Injection with modular Koin setup

3. **Platform-Specific**: Android and iOS implementations of platform-specific features like Network Monitoring

The diagram shows the relationships and dependencies between these packages, highlighting the modular architecture of the application.
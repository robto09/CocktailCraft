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
    
    %% UI
    UI[ui]
    Components[components]
    CartItemCard[CartItemCard]
    CocktailItem[CocktailItem]
    FilterChip[FilterChip]
    RatingBar[RatingBar]
    WriteReviewDialog[WriteReviewDialog]
    
    Main[main]
    MainScreen[MainScreen]
    
    Theme[theme]
    ThemeFile[Theme]
    
    %% ViewModel
    ViewModel[viewmodel]
    HomeViewModel[HomeViewModel]
    CartViewModel[CartViewModel]
    FavoritesViewModel[FavoritesViewModel]
    ProfileViewModel[ProfileViewModel]
    OrderViewModel[OrderViewModel]
    ReviewViewModel[ReviewViewModel]
    
    %% Navigation
    Navigation[navigation]
    NavigationManager[NavigationManager]
    Screen[Screen]
    
    %% Model
    Model[model]
    SortOption[SortOption]
    
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
    
    UseCase[usecase]
    PlaceOrderUseCase[PlaceOrderUseCase]
    ToggleFavoriteUseCase[ToggleFavoriteUseCase]
    
    Config[config]
    AppConfig[AppConfig]
    
    Util[util]
    Result[Result]
    
    %% Data
    Data[data]
    DataRepository[repository]
    AuthRepositoryImpl[AuthRepositoryImpl]
    CartRepositoryImpl[CartRepositoryImpl]
    CocktailRepositoryImpl[CocktailRepositoryImpl]
    OrderRepositoryImpl[OrderRepositoryImpl]
    
    Remote[remote]
    CocktailApi[CocktailApi]
    CocktailApiImpl[CocktailApiImpl]
    CocktailDto[CocktailDto]
    
    DataConfig[config]
    AppConfigImpl[AppConfigImpl]
    
    %% DI
    DI[di]
    AppModule[AppModule]
    PlatformModule[PlatformModule]
    
    %% Platform-specific
    AndroidMain[androidMain.kotlin.com.cocktailcraft]
    AndroidDI[di]
    AndroidPlatformModule[PlatformModule]
    
    IOSMain[iosMain.kotlin.com.cocktailcraft]
    IOSDI[di]
    IOSPlatformModule[PlatformModule]
    
    %% Package Hierarchy
    CocktailCraft --> AndroidApp
    CocktailCraft --> Shared
    
    AndroidApp --> JavaCocktailCraft
    
    JavaCocktailCraft --> Screens
    JavaCocktailCraft --> UI
    JavaCocktailCraft --> ViewModel
    JavaCocktailCraft --> Navigation
    JavaCocktailCraft --> Model
    JavaCocktailCraft --> MainActivity
    JavaCocktailCraft --> CocktailCraftApplication
    
    Screens --> HomeScreen
    Screens --> CartScreen
    Screens --> FavoritesScreen
    Screens --> ProfileScreen
    Screens --> OrderListScreen
    Screens --> CocktailDetailScreen
    
    UI --> Components
    UI --> Main
    UI --> Theme
    
    Components --> CartItemCard
    Components --> CocktailItem
    Components --> FilterChip
    Components --> RatingBar
    Components --> WriteReviewDialog
    
    Main --> MainScreen
    
    Theme --> ThemeFile
    
    ViewModel --> HomeViewModel
    ViewModel --> CartViewModel
    ViewModel --> FavoritesViewModel
    ViewModel --> ProfileViewModel
    ViewModel --> OrderViewModel
    ViewModel --> ReviewViewModel
    
    Navigation --> NavigationManager
    Navigation --> Screen
    
    Model --> SortOption
    
    Shared --> CommonMain
    Shared --> AndroidMain
    Shared --> IOSMain
    
    CommonMain --> Domain
    CommonMain --> Data
    CommonMain --> DI
    
    Domain --> DomainModel
    Domain --> DomainRepository
    Domain --> UseCase
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
    
    UseCase --> PlaceOrderUseCase
    UseCase --> ToggleFavoriteUseCase
    
    Config --> AppConfig
    
    Util --> Result
    
    Data --> DataRepository
    Data --> Remote
    Data --> DataConfig
    
    DataRepository --> AuthRepositoryImpl
    DataRepository --> CartRepositoryImpl
    DataRepository --> CocktailRepositoryImpl
    DataRepository --> OrderRepositoryImpl
    
    Remote --> CocktailApi
    Remote --> CocktailApiImpl
    Remote --> CocktailDto
    
    DataConfig --> AppConfigImpl
    
    DI --> AppModule
    DI --> PlatformModule
    
    AndroidMain --> AndroidDI
    AndroidDI --> AndroidPlatformModule
    
    IOSMain --> IOSDI
    IOSDI --> IOSPlatformModule
    
    %% Dependencies between packages
    Screens -- ViewModel
    Screens -- Navigation
    Screens -- UI
    
    Main -- Screens
    Main -- Navigation
    Main -- ViewModel
    
    ViewModel -- DomainRepository
    ViewModel -- UseCase
    ViewModel -- DomainModel
    
    UseCase -- DomainRepository
    UseCase -- DomainModel
    
    DataRepository -- DomainRepository
    DataRepository -- DomainModel
    DataRepository -- Remote
    
    DataConfig -- Config
    
    DI -- DataRepository
    DI -- Remote
    DI -- UseCase
    
    CocktailCraftApplication -- DI
```

This package diagram shows the organization of the CocktailCraft codebase, including the androidApp and shared modules, and the various packages within each module.
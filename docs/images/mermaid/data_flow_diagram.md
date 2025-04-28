# Data Flow Diagram

```mermaid
flowchart TD
    %% External Entities
    user([User])
    api([Cocktail API\nTheCocktailDB])
    system([System\nOS Theme & Network])

    %% Processes
    ui[UI Layer\nCompose Screens]
    theme[Theme System\nLight/Dark Mode]
    error_ui[Error Handling UI]
    viewmodels[ViewModels]
    base_vm[Base ViewModel\nError Handling]
    usecases[Use Cases]
    recommendation[Recommendation Engine]
    repositories[Repository Layer]
    cache[Caching System\nOffline Mode]
    network[Network Monitor]
    error_handler[Error Handler]

    %% Data Stores
    prefs[(Local Storage\nSharedPreferences)]
    datastore[(DataStore)]
    cache_store[(Cache Storage)]

    %% Data Flows - System Interactions
    system -->|System Theme Changes| theme
    system -->|Network Status| network

    %% Data Flows - User Interactions
    user -->|User Input\nClicks, Text Input| ui
    ui -->|UI Updates\nDisplay Data| user

    %% Theme System
    ui -->|Theme Preferences| theme
    theme -->|Theme Updates| ui
    theme -->|Save Theme Preferences| viewmodels
    viewmodels -->|Theme State| theme

    %% Error Handling UI
    error_handler -->|Error Information| error_ui
    error_ui -->|Error Display| ui
    ui -->|Retry Actions| error_handler

    %% UI to ViewModels
    ui -->|UI Events\nActions| viewmodels
    viewmodels -->|State Updates\nUI Data| ui

    %% Base ViewModel
    viewmodels -->|Inherit Error Handling| base_vm
    base_vm -->|Error Events| error_handler
    error_handler -->|User-Friendly Errors| base_vm

    %% ViewModels to Use Cases & Recommendation
    viewmodels -->|Business Logic\nRequests| usecases
    usecases -->|Domain Data\nResults| viewmodels
    viewmodels -->|Get Recommendations| recommendation
    recommendation -->|Similar Cocktails| viewmodels

    %% Use Cases to Repositories
    usecases -->|Data Operations| repositories
    repositories -->|Domain Models| usecases
    recommendation -->|Fetch Data| repositories

    %% Network Monitoring
    network -->|Connection Status| repositories
    network -->|Connection Status| viewmodels

    %% Repositories to Data Sources
    repositories -->|API Requests\nWhen Online| api
    api -->|API Responses\nDTOs| repositories

    repositories -->|Cache Data\nFor Offline| cache
    cache -->|Cached Data\nWhen Offline| repositories

    repositories -->|Read/Write\nPreferences| prefs
    prefs -->|Stored Data| repositories

    repositories -->|Read/Write\nStructured Data| datastore
    datastore -->|Stored Data| repositories

    cache -->|Store Cached Data| cache_store
    cache_store -->|Retrieve Cached Data| cache

    %% Notes
    subgraph UI_Data [UI Data Flows]
        direction LR
        ui_data1[Cocktail List Display]
        ui_data2[Cocktail Details]
        ui_data3[Cart Items]
        ui_data4[Order History]
        ui_data5[User Profile]
        ui_data6[Theme Settings]
        ui_data7[Offline Mode Status]
        ui_data8[Error Messages]
        ui_data9[Recommendations]
    end

    subgraph VM_Data [ViewModel Data]
        direction LR
        vm_data1[Cocktail Lists]
        vm_data2[Cart State]
        vm_data3[Order State]
        vm_data4[User State]
        vm_data5[Favorites State]
        vm_data6[Theme State]
        vm_data7[Network State]
        vm_data8[Error State]
        vm_data9[Recommendation State]
    end

    subgraph Repo_Ops [Repository Operations]
        direction LR
        repo_op1[Fetch Cocktails from API]
        repo_op2[Store/Retrieve Favorites]
        repo_op3[Store/Retrieve Cart Items]
        repo_op4[Store/Retrieve Orders]
        repo_op5[Store/Retrieve User Data]
        repo_op6[Cache Cocktails]
        repo_op7[Check Network Status]
        repo_op8[Handle API Errors]
    end

    subgraph Cache_Ops [Cache Operations]
        direction LR
        cache_op1[Store Recently Viewed]
        cache_op2[Retrieve When Offline]
        cache_op3[Clear Cache]
        cache_op4[Check Cache Age]
    end

    subgraph API_Data [API Data]
        direction LR
        api_data1[Cocktail Information]
        api_data2[Ingredients]
        api_data3[Categories]
        api_data4[Glasses]
    end

    UI_Data -.-> ui
    VM_Data -.-> viewmodels
    Repo_Ops -.-> repositories
    Cache_Ops -.-> cache
    API_Data -.-> api
```

This data flow diagram shows how data moves through the CocktailCraft application, including:

1. **User Interface Layer**: Shows how user input flows through the UI components, including theme settings and error handling UI
2. **ViewModel Layer**: Illustrates how ViewModels manage state and handle errors through the BaseViewModel
3. **Domain Layer**: Shows how Use Cases and the Recommendation Engine interact with repositories
4. **Data Layer**: Depicts how repositories interact with the API, caching system, and local storage
5. **External Systems**: Includes the Cocktail API, system theme changes, and network status

The diagram highlights the new features:
- **Dark Mode**: Theme system with user preferences and system theme integration
- **Offline Mode**: Caching system and network monitoring for offline functionality
- **Error Handling**: Comprehensive error handling flow from API to user interface
- **Recommendation System**: Flow of data for cocktail recommendations
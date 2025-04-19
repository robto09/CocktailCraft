# Data Flow Diagram

```mermaid
flowchart TD
    %% External Entities
    user([User])
    api([Cocktail API\nTheCocktailDB])
    
    %% Processes
    ui[UI Layer\nCompose Screens]
    viewmodels[ViewModels]
    usecases[Use Cases]
    repositories[Repository Layer]
    
    %% Data Stores
    prefs[(Local Storage\nSharedPreferences)]
    datastore[(DataStore)]
    
    %% Data Flows - User Interactions
    user -->|User Input\nClicks, Text Input| ui
    ui -->|UI Updates\nDisplay Data| user
    
    %% UI to ViewModels
    ui -->|UI Events\nActions| viewmodels
    viewmodels -->|State Updates\nUI Data| ui
    
    %% ViewModels to Use Cases
    viewmodels -->|Business Logic\nRequests| usecases
    usecases -->|Domain Data\nResults| viewmodels
    
    %% Use Cases to Repositories
    usecases -->|Data Operations| repositories
    repositories -->|Domain Models| usecases
    
    %% Repositories to Data Sources
    repositories -->|API Requests| api
    api -->|API Responses\nDTOs| repositories
    
    repositories -->|Read/Write\nPreferences| prefs
    prefs -->|Stored Data| repositories
    
    repositories -->|Read/Write\nStructured Data| datastore
    datastore -->|Stored Data| repositories
    
    %% Notes
    subgraph UI_Data [UI Data Flows]
        direction LR
        ui_data1[Cocktail List Display]
        ui_data2[Cocktail Details]
        ui_data3[Cart Items]
        ui_data4[Order History]
        ui_data5[User Profile]
    end
    
    subgraph VM_Data [ViewModel Data]
        direction LR
        vm_data1[Cocktail Lists]
        vm_data2[Cart State]
        vm_data3[Order State]
        vm_data4[User State]
        vm_data5[Favorites State]
    end
    
    subgraph Repo_Ops [Repository Operations]
        direction LR
        repo_op1[Fetch Cocktails from API]
        repo_op2[Store/Retrieve Favorites]
        repo_op3[Store/Retrieve Cart Items]
        repo_op4[Store/Retrieve Orders]
        repo_op5[Store/Retrieve User Data]
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
    API_Data -.-> api
```

This data flow diagram shows how data moves through the CocktailCraft application, from user input through the various layers to external systems and back to the user interface.
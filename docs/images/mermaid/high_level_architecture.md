# High-Level Architecture Diagram

```mermaid
graph TD
    subgraph "CocktailCraft Application"
        subgraph "Android App"
            UI["UI Layer<br>(Compose Screens)"]
            VM["ViewModels"]
            Nav["Navigation"]
        end
        
        subgraph "Shared Module"
            subgraph "Domain Layer"
                Models["Models"]
                RepoInt["Repository Interfaces"]
                UC["Use Cases"]
            end
            
            subgraph "Data Layer"
                RepoImpl["Repository Implementations"]
                Remote["Remote Data Source"]
                Local["Local Data Source"]
            end
        end
        
        subgraph "Platform-Specific"
            AndroidImpl["Android Implementation"]
            iOSImpl["iOS Implementation"]
        end
    end
    
    %% Relationships
    UI --> VM
    VM --> UC
    VM --> Nav
    UC --> RepoInt
    RepoImpl --> RepoInt
    RepoImpl --> Remote
    RepoImpl --> Local
    RepoImpl --> Models
    AndroidImpl --> RepoImpl
    iOSImpl --> RepoImpl
```

This diagram shows the high-level architecture of the CocktailCraft application, including the Android App (UI Layer, ViewModels, Navigation) and the Shared Module (Domain Layer, Data Layer, Platform-Specific implementations).
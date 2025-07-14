# iOS App Setup Instructions

## Prerequisites
- Xcode 14.0 or later
- CocoaPods installed (`sudo gem install cocoapods`)
- macOS 12.0 or later

## Setup Steps

### 1. Build the Shared Framework
```bash
cd ..
./gradlew :shared:podPublishXCFramework
```

### 2. Create Xcode Project
1. Open Xcode
2. Create new iOS App:
   - Product Name: `CocktailCraft`
   - Organization Identifier: `com.cocktailcraft`
   - Interface: SwiftUI
   - Language: Swift
   - Use Core Data: No
   - Include Tests: Yes

### 3. Add Project Files
1. Delete the default `ContentView.swift`
2. Drag all files from `iosApp/CocktailCraft/` into the Xcode project
3. Ensure "Copy items if needed" is checked
4. Select "Create groups" for added folders

### 4. Install CocoaPods
```bash
cd iosApp
pod install
```

### 5. Open Workspace
- Close the `.xcodeproj` file if open
- Open `CocktailCraft.xcworkspace` (not `.xcodeproj`)

### 6. Configure Build Settings
1. Select the project in navigator
2. Select the app target
3. Go to Build Settings
4. Search for "Other Linker Flags"
5. Add `-lsqlite3` if not present

### 7. Update Info.plist
Add the following keys for network access:
```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

### 8. Run the App
1. Select a simulator or device
2. Press Cmd+R to build and run

## Project Structure

```
iosApp/
├── CocktailCraft/
│   ├── CocktailCraftApp.swift      # App entry point
│   ├── ContentView.swift           # Main tab navigation
│   ├── Views/                      # All SwiftUI views
│   │   ├── HomeView.swift
│   │   ├── CartView.swift
│   │   ├── FavoritesView.swift
│   │   ├── ProfileView.swift
│   │   └── ...
│   ├── ViewModels/                 # iOS ViewModels
│   │   ├── HomeViewModel.swift
│   │   ├── CartViewModel.swift
│   │   └── ...
│   └── Components/                 # Reusable UI components
│       ├── CocktailCard.swift
│       ├── ErrorView.swift
│       └── ...
└── Podfile                         # CocoaPods configuration
```

## Key Features Implemented

### Phase 3 Completed ✅
- iOS project structure created
- CocoaPods configured for shared module integration
- Basic SwiftUI app with tab navigation
- Koin dependency injection initialized
- All main screens created (Home, Cart, Favorites, Profile)
- Reusable components (CocktailCard, ErrorView, EmptyStateView)
- ViewModels that bridge SwiftUI with shared module
- Error handling using shared ErrorHandler

### Next Steps (Phase 4)
1. Implement actual data loading from shared repositories
2. Add offline support
3. Implement proper state management
4. Add animations and transitions
5. Polish UI/UX to match Android app
6. Add unit and UI tests

## Troubleshooting

### Pod Install Fails
```bash
cd ..
./gradlew :shared:podPublishXCFramework
cd iosApp
pod install --repo-update
```

### Build Errors
1. Clean build folder: Cmd+Shift+K
2. Delete derived data
3. Restart Xcode

### Shared Module Not Found
Ensure the shared framework was built:
```bash
./gradlew :shared:linkDebugFrameworkIosX64
```
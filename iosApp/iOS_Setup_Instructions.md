# iOS App Setup Instructions

## Prerequisites
- Xcode 16 or later (the project builds with the Swift 5.10 toolchain — `SWIFT_VERSION: "5.10"` in `project.yml`)
- CocoaPods installed (`sudo gem install cocoapods`)
- macOS 12.0 or later

> **Source of truth (IO-8):** `project.yml` (XcodeGen) defines the Xcode
> project — targets, entitlements, per-target ATS pinning, and the test
> targets. The committed `CocktailCraft.xcodeproj` is **generated output**:
> never hand-edit it, and regenerate it (`xcodegen generate && pod install`)
> whenever `project.yml` changes or files are added/removed, then commit the
> regenerated project alongside your change. Install XcodeGen with
> `brew install xcodegen`.

## Setup Steps

### 1. Build the Shared Framework
```bash
cd ..
./gradlew :shared:podPublishXCFramework
```

### 2. Generate the Xcode Project
```bash
cd iosApp
xcodegen generate
```
(The manual project-creation steps below are legacy documentation of what the
generated project contains — you never need to perform them by hand.)

### 2b. (Legacy reference) Create Xcode Project
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

### 7. App Transport Security (already configured)
No ATS changes are needed — and do **not** add `NSAllowsArbitraryLoads`. The app enforces TLS certificate pinning for TheCocktailDB via `NSAppTransportSecurity` → `NSPinnedDomains` in `CocktailCraft/Info.plist` (pinning the Cloudflare CA pool for `thecocktaildb.com`), mirrored for the widget extension in `project.yml`. If pinned requests start failing after a Cloudflare CA rotation, refresh the pin set per the comment in `Info.plist`.

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
│   │   ├── HomeViewSKIE.swift
│   │   ├── CartView.swift
│   │   ├── FavoritesView.swift
│   │   ├── ProfileView.swift
│   │   └── ...
│   ├── ViewModels/                 # SKIE wrappers around the shared ViewModels
│   │   ├── SharedViewModelWrapper.swift
│   │   ├── HomeViewModelSKIE.swift
│   │   ├── CartViewModelSKIE.swift
│   │   └── ...
│   ├── Components/                 # Reusable UI components
│   │   ├── CocktailCard.swift
│   │   ├── ErrorView.swift
│   │   └── ...
│   ├── Theme/                      # AppColors, AppTheme
│   └── Utils/                      # BackgroundSyncManager, NetworkMonitor, widget bridges
├── project.yml                     # XcodeGen project definition (source of truth)
└── Podfile                         # CocoaPods configuration
```

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
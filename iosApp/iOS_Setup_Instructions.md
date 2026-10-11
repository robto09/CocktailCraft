# iOS App Setup Instructions

## Prerequisites
- Xcode 16 or later (the project builds with the Swift 5.10 toolchain — `SWIFT_VERSION: "5.10"` in `project.yml`)
- XcodeGen (`brew install xcodegen`)
- A JDK 17+ — every app build runs Gradle to compile the shared Kotlin framework
- macOS 12.0 or later

No CocoaPods: the shared Kotlin framework uses Kotlin's direct Xcode
integration and the third-party Swift libraries come from Swift Package
Manager, so there is no Podfile, no `pod install` and no `.xcworkspace`.

> **Source of truth (IO-8):** `project.yml` (XcodeGen) defines the Xcode
> project — targets, entitlements, per-target ATS pinning, Swift packages,
> the Kotlin build phase, and the test targets. The committed
> `CocktailCraft.xcodeproj` (including its SPM `Package.resolved`) is
> **generated output**: never hand-edit it, and regenerate it
> (`xcodegen generate`) whenever `project.yml` changes or files are
> added/removed, then commit the regenerated project alongside your change.

## Setup Steps

### 1. Generate the Xcode Project
```bash
cd iosApp
xcodegen generate
```
(The manual project-creation steps below are legacy documentation of what the
generated project contains — you never need to perform them by hand.)

### 2. Open the Project
Open `CocktailCraft.xcodeproj`. Xcode resolves the Swift packages (Kingfisher,
DebugSwift) on first open, pinned by the committed `Package.resolved`.

There is no separate step to build the shared framework: the app target's
**Compile Kotlin Framework** build phase runs
`./gradlew :shared:embedAndSignAppleFrameworkForXcode` before Swift
compilation on every build. Gradle builds the static `shared.framework` for
the active configuration, SDK and architecture into
`shared/build/xcode-frameworks/<Configuration>/<sdk>/`, which the app (and the
unit test target, for `import shared`) finds via `FRAMEWORK_SEARCH_PATHS`.
Because the framework is static, the app links `sqlite3` (SQLDelight native
driver) and `libc++` itself through `OTHER_LDFLAGS` in `project.yml`. The
first build downloads the Kotlin/Native toolchain and takes a few minutes;
later builds are incremental.

### 3. (Legacy reference) Create Xcode Project
1. Open Xcode
2. Create new iOS App:
   - Product Name: `CocktailCraft`
   - Organization Identifier: `com.cocktailcraft`
   - Interface: SwiftUI
   - Language: Swift
   - Use Core Data: No
   - Include Tests: Yes

### 4. (Legacy reference) Add Project Files
1. Delete the default `ContentView.swift`
2. Drag all files from `iosApp/CocktailCraft/` into the Xcode project
3. Ensure "Copy items if needed" is checked
4. Select "Create groups" for added folders

### 5. Debug-only DebugSwift
DebugSwift is a Swift package dependency of the app, but it never ships:
Release excludes `DebugSwift.o` from the link, the **Strip DebugSwift from
Release** build phase removes its resource bundle (and fails the build if any
DebugSwift symbol is linked), and all app-side usage is wrapped in
`#if DEBUG`. See the [DebugSwift Guide](../docs/DebugSwift.md).

### 6. Migrating an Existing CocoaPods Checkout
Delete the leftover CocoaPods artifacts (both are gitignored), regenerate, and
open the `.xcodeproj` from now on:
```bash
cd iosApp
rm -rf Pods CocktailCraft.xcworkspace
xcodegen generate
```

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
│   └── Utils/                      # AppRouter, NetworkMonitor, widget bridges
└── project.yml                     # XcodeGen project definition (source of truth)
```

## Troubleshooting

### "Compile Kotlin Framework" Fails
The phase only shells out to Gradle, so reproduce it from the repository root
to see the full error:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```
Make sure a JDK 17+ is installed and visible to Xcode. The phase needs
`ENABLE_USER_SCRIPT_SANDBOXING = NO` (set in `project.yml`); if you see a
sandbox error, regenerate the project rather than editing build settings.

### Swift Packages Fail to Resolve
File > Packages > Reset Package Caches in Xcode, or from `iosApp/`:
```bash
xcodebuild -resolvePackageDependencies -project CocktailCraft.xcodeproj
```

### Build Errors
1. Clean build folder: Cmd+Shift+K
2. Delete derived data
3. Restart Xcode

### Shared Module Not Found
`No such module 'shared'` means the Compile Kotlin Framework phase did not
produce `shared/build/xcode-frameworks/<Configuration>/<sdk>/shared.framework`
— check that phase's output in the build log. If the project predates the
CocoaPods removal, regenerate it (`xcodegen generate`) and open
`CocktailCraft.xcodeproj`, not the old workspace.
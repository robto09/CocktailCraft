# iOS App Setup Instructions

## Prerequisites
- Xcode 26 or later: the app builds against the iOS 26 SDK (`ToolbarItemPlacement.title` in `AppTheme.swift`). The app targets use Swift 5.10 (`SWIFT_VERSION: "5.10"` in `project.yml`)
- XcodeGen (`brew install xcodegen`)
- A JDK 17+ — every app build runs Gradle to compile the shared Kotlin framework
- A Mac with Apple silicon on macOS 15.6 (Sequoia) or later: the oldest macOS Xcode 26 runs on, and the shared Kotlin framework is built for arm64 simulators and devices only

The app and its widget extension target iOS 17.0 (`deploymentTarget` in `project.yml`).

No CocoaPods: the shared Kotlin framework uses Kotlin's direct Xcode
integration and the third-party Swift libraries come from Swift Package
Manager, so there is no Podfile, no `pod install` and no `.xcworkspace`.

> **Source of truth:** `project.yml` (XcodeGen) defines the Xcode
> project — targets, entitlements, Swift packages,
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

### 2. Open the Project
Open `CocktailCraft.xcodeproj`. Xcode resolves the Swift packages (Kingfisher,
DebugSwift, and swift-snapshot-testing for the unit tests) on first open,
pinned by the committed `Package.resolved`.

There is no separate step to build the shared framework: the app target's
**Compile Kotlin Framework** build phase runs
`./gradlew :shared:embedAndSignAppleFrameworkForXcode` before Swift
compilation on every build. Gradle builds the static `shared.framework` for
the active configuration, SDK and architecture into
`shared/build/xcode-frameworks/<Configuration>/<sdk>/`, which the app (and the
unit test target, for `import shared`) finds via `FRAMEWORK_SEARCH_PATHS`.
Because the framework is static, the app links `sqlite3` (SQLDelight native
driver) and `libc++` itself through `OTHER_LDFLAGS` in `project.yml`. The
same Gradle call runs moko-resources' `:shared:copyFrameworkResourcesToApp`,
which copies the shared strings bundle (`CocktailCraft:shared.bundle`) into the
app — a static framework cannot carry resources. The first build downloads
the Kotlin/Native toolchain and takes a few minutes; later builds are
incremental.

### 3. Debug-only DebugSwift
DebugSwift is a Swift package dependency of the app, but it never ships:
Release excludes `DebugSwift.o` from the link, the **Strip DebugSwift from
Release** build phase removes its resource bundle (and fails the build if any
DebugSwift symbol is linked), and all app-side usage is wrapped in
`#if DEBUG`. See the [DebugSwift Guide](../docs/DebugSwift.md).

### 4. Migrating an Existing CocoaPods Checkout
Delete the leftover CocoaPods artifacts (both are gitignored), regenerate, and
open the `.xcodeproj` from now on:
```bash
cd iosApp
rm -rf Pods CocktailCraft.xcworkspace
xcodegen generate
```

### 5. App Transport Security (already configured)
No ATS changes are needed — and do **not** add `NSAllowsArbitraryLoads`. The app uses the platform defaults (HTTPS only, system trust) and deliberately has no certificate pinning: TheCocktailDB is a public, unauthenticated API, and pinning the CA pool its CDN rotates on its own schedule would risk cutting the app off the network (see the comment in `CocktailCraft/Info.plist`). The widget extension does no networking (it reads App Group snapshots the app writes), so it has no ATS config of its own.

The app's privacy manifest is `CocktailCraft/PrivacyInfo.xcprivacy`: no tracking and no collected data (everything the user creates stays on the device), and one required-reason API, `NSUserDefaults` for the app's own settings (`CA92.1`). Declare any new required-reason API there. The widget extension reads App Group files, not defaults, so it has no manifest.

### 6. Run the App
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
│   └── Utils/                      # AppRouter, ConnectivityObserver, widget bridges
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
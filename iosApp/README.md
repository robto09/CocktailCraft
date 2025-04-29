# CocktailCraft iOS App

This directory contains the iOS implementation of CocktailCraft, leveraging the shared Kotlin Multiplatform codebase.

## Initial Development Environment Setup

### 1. Install macOS Requirements
- A Mac computer running macOS Ventura (13.0) or later
- At least 35GB of available disk space
- 8GB RAM minimum (16GB recommended)

### 2. Install Xcode
1. Open the App Store on your Mac
2. Search for "Xcode"
3. Click "Get" or "Install"
4. Wait for the download and installation to complete (this may take a while)
5. Once installed, open Xcode to complete initial setup
6. Accept the license agreement when prompted
7. Allow Xcode to install additional components

### 3. Install Command Line Tools
```bash
xcode-select --install
```

### 4. Install Homebrew (Package Manager)
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### 5. Install Ruby Version Manager (RVM)
```bash
\curl -sSL https://get.rvm.io | bash -s stable
source ~/.rvm/scripts/rvm
rvm install 3.0.0
rvm use 3.0.0 --default
```

### 6. Install CocoaPods
```bash
gem install cocoapods
```

### 7. Install KMM Plugin
1. Open Xcode
2. Go to Xcode → Settings → Components
3. Download and install the latest version of the KMM plugin

### 8. Project Setup
1. Clone the repository if you haven't already:
```bash
git clone <repository-url>
cd CocktailCraft
```

2. Install project dependencies:
```bash
pod install
```

3. Open the project:
```bash
open CocktailCraft.xcworkspace
```

## Project Structure

```
iosApp/
├── CocktailCraft/
│   ├── UI/
│   │   ├── Screens/         # SwiftUI view controllers
│   │   ├── Components/      # Reusable UI components
│   │   └── Theme/          # iOS-specific theming
│   ├── ViewModels/         # iOS-specific ViewModels
│   ├── DI/                 # iOS dependency injection
│   └── Platform/           # iOS-specific implementations
└── CocktailCraftTests/     # iOS unit tests
```

## Architecture Overview

### MVVM + Clean Architecture

The iOS app follows MVVM pattern with Clean Architecture principles, matching the Android implementation:

1. **Presentation Layer (UI)**
   - SwiftUI Views
   - ViewModels (shared from KMP + iOS-specific)
   - UI State management
   - UI Components

2. **Domain Layer (Shared)**
   - Use Cases
   - Domain Models
   - Repository Interfaces
   - Business Rules

3. **Data Layer (Shared)**
   - Repository Implementations
   - Data Sources
   - DTOs
   - Mapping Logic

### Recommended Libraries

1. **State Management & Architecture**
   - `Combine` - Reactive programming framework
   - `SwiftUI` - Declarative UI framework
   - `Factory` - Swift dependency injection

2. **Networking & Data**
   - `KMM-ktor` - Shared networking (already configured)
   - `Kingfisher` - Image loading and caching
   - `KeychainSwift` - Secure storage

3. **Testing**
   - `XCTest` - Unit and UI testing
   - `ViewInspector` - SwiftUI testing
   - `SnapshotTesting` - UI snapshot testing

4. **Development Tools**
   - `SwiftLint` - Code style enforcement
   - `SwiftFormat` - Code formatting
   - `R.swift` - Strong typed resources

## Common Setup Issues and Solutions

### 1. Xcode Installation Issues
- **Issue**: Xcode download fails
  - Solution: Try downloading directly from [Apple Developer](https://developer.apple.com/download/applications/)
- **Issue**: "Install failed" error
  - Solution: Ensure you have enough disk space and try again

### 2. Command Line Tools Issues
- **Issue**: "Installation failed" message
  - Solution: Download manually from [Apple Developer](https://developer.apple.com/download/all/)
- **Issue**: Version mismatch
  - Solution: Run `sudo rm -rf /Library/Developer/CommandLineTools` and reinstall

### 3. CocoaPods Issues
- **Issue**: Permission errors
  - Solution: Run with sudo: `sudo gem install cocoapods`
- **Issue**: Ruby version conflicts
  - Solution: Use RVM to manage Ruby versions

### 4. Project-Specific Issues
- **Issue**: "Framework not found" errors
  - Solution: Clean build folder (Cmd + Shift + K) and rebuild
- **Issue**: Pod installation fails
  - Solution: Delete Podfile.lock and run `pod install` again

## Next Steps After Setup

1. Verify the setup:
```bash
xcodebuild -version  # Check Xcode installation
pod --version       # Check CocoaPods installation
```

2. Configure Xcode preferences:
- Set up source control
- Configure text editing preferences
- Set up custom key bindings if needed

3. Start with the development:
- Review the architecture documentation
- Check the Android implementation for feature parity
- Begin with basic UI components

## Resources

- [Getting Started with Xcode](https://developer.apple.com/xcode/resources/)
- [SwiftUI Documentation](https://developer.apple.com/documentation/swiftui)
- [Kotlin Multiplatform Mobile](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [iOS Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines)
- [Clean Architecture with MVVM](https://www.kodeco.com/29416318-getting-started-with-clean-architecture-for-ios)
- [Combine Framework](https://developer.apple.com/documentation/combine)

## Support

If you encounter any issues during setup:
1. Check the Common Issues section above
2. Review the error message carefully
3. Search the project issues on GitHub
4. Ask for help in the project's discussion forum

Remember to keep your development environment updated regularly to avoid compatibility issues.
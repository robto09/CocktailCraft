# CocktailCraft iOS App

The iOS client for the CocktailCraft application, built with SwiftUI and Kotlin Multiplatform.

## Requirements

- Xcode 14.0 or later
- iOS 15.0 or later
- CocoaPods
- Swift 5.0
- JDK 17
- Kotlin 1.9.22

## Setup Instructions

1. Install required tools:
   ```bash
   brew install kotlin
   brew install cocoapods
   ```

2. Generate Xcode project:
   ```bash
   cd iosApp
   xcodegen generate
   ```

3. Install dependencies:
   ```bash
   pod install
   ```

4. Open the workspace:
   ```bash
   open CocktailCraft.xcworkspace
   ```

## Project Structure

```
iosApp/
├── CocktailCraft/
│   ├── CocktailCraftApp.swift        # Main app entry point
│   ├── DI/
│   │   └── DependencyContainer.swift  # Dependency injection container
│   ├── UI/
│   │   ├── Components/               # Reusable UI components
│   │   ├── Screens/                  # Main app screens
│   │   └── Theme/                    # App theme and styling
│   └── Info.plist                    # App configuration
└── project.yml                       # XcodeGen project configuration
```

## Features

### Authentication
- Email/password login and registration
- Biometric authentication
- Password reset
- Token management
- Deep link handling

### Home Screen
- Featured cocktails
- Popular cocktails
- Category filtering
- Search functionality
- Advanced search filters

### Cocktail Details
- Detailed cocktail information
- Ingredients and instructions
- Reviews and ratings
- Add to cart/favorites
- Share functionality

### Cart
- Add/remove items
- Quantity adjustment
- Special instructions
- Checkout process
- Order confirmation

### Orders
- Active order tracking
- Order history
- Order cancellation
- Refund requests
- Order ratings

### Favorites
- Save favorite cocktails
- Remove from favorites
- Share favorites list
- Sort and filter options

### Profile
- Personal information
- Delivery addresses
- Payment methods
- Notification preferences
- Theme settings

## Architecture

### MVVM Pattern
- ViewModels in shared Kotlin code
- SwiftUI Views for presentation
- Unidirectional data flow
- State management with Flows

### Dependency Injection
- Koin for shared code
- DependencyContainer for iOS-specific code
- View model factories
- Repository injection

### Navigation
- SwiftUI navigation
- Deep link handling
- Screen coordination
- Tab-based navigation

### Data Flow
1. User interaction in SwiftUI View
2. Action handled by shared ViewModel
3. Repository processes action
4. Result flows back through ViewModel
5. View updates with new state

## Best Practices

### SwiftUI
- Use SwiftUI previews
- Extract reusable components
- Follow Apple HIG
- Support dark mode
- Handle dynamic type

### Performance
- Image caching
- Lazy loading
- Background operations
- Memory management
- Network optimization

### Security
- Secure storage
- Biometric authentication
- Token management
- Data encryption
- Input validation

## Common Issues

### Framework Not Found
If you encounter "Framework not found" errors:
1. Clean build folder
2. Run pod install
3. Rebuild shared module
4. Clean and rebuild iOS project

### Kotlin Framework Build
If Kotlin framework fails to build:
1. Check Kotlin version
2. Update Gradle wrapper
3. Clean shared module
4. Rebuild framework

### Xcode Project Generation
If XcodeGen fails:
1. Check project.yml syntax
2. Update XcodeGen
3. Clear derived data
4. Regenerate project

## Testing

### Unit Tests
- ViewModel tests
- Repository tests
- Use case tests
- Utility tests

### UI Tests
- Screen tests
- Component tests
- Navigation tests
- Integration tests

## Deployment

### App Store
1. Update version numbers
2. Run SwiftLint
3. Update Info.plist
4. Generate screenshots
5. Submit for review

### TestFlight
1. Archive project
2. Upload build
3. Add test information
4. Invite testers

## Contributing

1. Fork repository
2. Create feature branch
3. Implement changes
4. Add tests
5. Submit pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details
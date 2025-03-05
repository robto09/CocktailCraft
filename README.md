# CocktailCraft App

## Overview
This is a Kotlin Multiplatform project for a Cocktail Crafting app, designed to run on both Android and iOS platforms. It uses Ktor for networking, Koin for dependency injection, and supports serialization and coroutines.

## Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher
- **Android Studio**: Latest version
- **Xcode**: Latest version (for iOS development)
- **Kotlin Multiplatform Plugin**: Installed in your IDE

## Project Setup
1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd BreadApp2_CorrectedProject
   ```

2. **Open the Project**:
   - Open the project in Android Studio.

3. **Sync the Project**:
   - Allow Gradle to sync and download all necessary dependencies.

4. **Build the Project**:
   - For Android: Use the `androidApp` module to build and run the app on an Android device or emulator.
   - For iOS: Open the `iosApp` module in Xcode and run the app on an iOS device or simulator.

## Common Issues and Solutions
- **Deprecated Functions**: If you encounter warnings about deprecated functions, such as `android()`, replace them with the recommended alternatives like `androidTarget()`.

- **Unresolved Dependencies**: Ensure that all dependencies are correctly defined in the `versions.toml` and `build.gradle.kts` files. Run a Gradle sync after making changes.

- **iOS Source Set Errors**: If focusing on Android development, comment out iOS targets and source sets in the `shared/build.gradle.kts` file to avoid related errors.

- **Kotlin Multiplatform Setup**: Ensure that the Kotlin Multiplatform setup in `shared/build.gradle.kts` is correctly configured for your target platforms.

## Additional Resources
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Ktor Documentation](https://ktor.io/docs/)
- [Koin Documentation](https://insert-koin.io/docs/)

Feel free to reach out for further assistance or contribute to the project! 
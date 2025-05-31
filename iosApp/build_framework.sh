#!/bin/bash

# Build the shared framework for iOS
cd ..

# Build for both architectures
echo "Building shared framework..."
./gradlew shared:linkDebugFrameworkIosX64
./gradlew shared:linkDebugFrameworkIosSimulatorArm64

# Copy the appropriate framework to the iOS project
FRAMEWORK_SOURCE="../shared/build/bin/iosX64/debugFramework/shared.framework"
FRAMEWORK_DEST="iosApp/CocktailCraft/CocktailCraft/shared.framework"

echo "Copying framework..."
rm -rf "$FRAMEWORK_DEST"
cp -R "$FRAMEWORK_SOURCE" "$FRAMEWORK_DEST"

echo "Framework built and copied successfully!"
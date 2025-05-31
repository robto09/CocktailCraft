#!/bin/bash

# Build the shared framework for iOS
cd ..

# Build for both architectures
echo "Building shared framework..."
./gradlew shared:linkDebugFrameworkIosX64
./gradlew shared:linkDebugFrameworkIosSimulatorArm64

# Copy the appropriate framework to the iOS project
# Determine which architecture to use based on current machine
if [[ $(uname -m) == 'arm64' ]]; then
    FRAMEWORK_SOURCE="shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework"
else
    FRAMEWORK_SOURCE="shared/build/bin/iosX64/debugFramework/shared.framework"
fi
FRAMEWORK_DEST="iosApp/CocktailCraft/CocktailCraft/shared.framework"

echo "Copying framework..."
rm -rf "$FRAMEWORK_DEST"
cp -R "$FRAMEWORK_SOURCE" "$FRAMEWORK_DEST"

echo "Framework built and copied successfully!"
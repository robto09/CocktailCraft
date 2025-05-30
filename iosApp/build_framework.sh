#!/bin/bash

# Build the shared framework for iOS
cd ..
./gradlew shared:linkDebugFrameworkIosSimulatorArm64

echo "Framework built successfully at:"
echo "../shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework"
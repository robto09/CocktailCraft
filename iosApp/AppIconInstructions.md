# App Icon Generation Instructions

## Overview
The app icon design is implemented as a SwiftUI view in `AppIconView.swift`. Follow these steps to generate and add the app icon to your iOS project.

## Steps to Generate App Icon

### 1. Generate the Icon Image
1. Open `AppIconView.swift` in Xcode
2. Run the app in the iOS Simulator
3. Navigate to the preview or add this temporary code to ContentView:
   ```swift
   AppIconView(size: 1024)
       .frame(width: 1024, height: 1024)
   ```
4. Take a screenshot (Cmd+S in simulator)
5. The screenshot will be saved to your Desktop

### 2. Alternative: Use SwiftUI Preview
1. Open `AppIconView.swift`
2. Click on the preview canvas
3. Click the "Export Preview" button
4. Save as PNG at 1024x1024 resolution

### 3. Generate Icon Sizes
Use an online tool like:
- https://www.appicon.co/
- https://makeappicon.com/
- Or use Xcode's built-in asset catalog generator

### 4. Add to Xcode Project
1. Open `Assets.xcassets` in Xcode
2. Select `AppIcon`
3. Drag and drop the generated icons to their respective slots
4. Ensure you have icons for all required sizes

## Required Icon Sizes for iOS
- 1024x1024 (App Store)
- 180x180 (iPhone @3x)
- 120x120 (iPhone @2x)
- 152x152 (iPad @2x)
- 76x76 (iPad @1x)
- And other sizes as needed

## Design Details
- Primary Color: #EB6A43 (Coral/Orange)
- Background: Gradient from primary to darker shade
- Icon: White cocktail glass with garnish
- Corner Radius: ~22.37% of icon size (iOS standard)

## Launch Screen
The launch screen is already implemented as `LaunchScreenView.swift` and will display automatically when the app starts.
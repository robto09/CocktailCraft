# iOS Migration Todo List

## Completed Tasks ✅
- [x] Create new branch for iOS migration
- [x] Phase 1: Analyze Android-specific code for sharing

## Pending Tasks 📋

### Phase 2: Refactor Shared Module for iOS Compatibility
- [ ] Move SortOption.kt to shared module
- [ ] Extract core ErrorUtils logic to shared module
- [ ] Enable iOS targets in shared module Gradle
- [ ] Add kmm-viewmodel dependency
- [ ] Create multiplatform ViewModel base class

### Phase 3: iOS Project Setup
- [ ] Configure iOS framework generation
- [ ] Create Xcode project
- [ ] Setup dependency injection for iOS

### Phase 4: iOS App Implementation
- [ ] Implement core app structure & navigation
- [ ] Implement all screens in SwiftUI
- [ ] Implement UI components
- [ ] Implement platform-specific features
- [ ] Testing and deployment

## Current Status
Working on Phase 2 - starting with moving SortOption.kt to the shared module.
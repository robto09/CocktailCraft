# SKIE (Swift Kotlin Interface Enhancer) Complete Documentation

## Overview

SKIE is a Kotlin Multiplatform (KMP) tool that improves Swift interoperability by enhancing how Kotlin code is translated and used in Swift development. It bridges semantic differences between Kotlin and Swift, making cross-platform development more seamless.

### Key Benefits
- Improves Swift developer experience when using Kotlin Multiplatform
- Reduces boilerplate and complexity in cross-platform code  
- Provides more native-feeling Swift interactions with Kotlin code
- Preserves type information and enables proper Swift language features

## Installation

### Requirements
- Kotlin Multiplatform Project
- Module that creates Xcode Frameworks

### Installation Steps

1. **Locate KMP Module for Xcode Frameworks**
   - Typically has `kotlin("native.cocoapods")` plugin
   - Plugin applies only to modules creating Xcode Frameworks

2. **Add SKIE Gradle Plugin**
   In `build.gradle.kts`, add:
   ```kotlin
   plugins {
       id("co.touchlab.skie") version "0.10.4"
   }
   ```

3. **Configure Repositories**
   In `settings.gradle.kts`, ensure:
   ```kotlin
   pluginManagement {
       repositories {
           google()
           mavenCentral()
           gradlePluginPortal()
       }
   }
   ```

4. **Migration Considerations**
   - Review migration documentation for existing projects
   - SKIE will instrument exported code in frameworks
   - Gradle caching issues may require `./gradlew dependencies --refresh-dependencies`

## Features

### Enumerations Improvements

SKIE automatically generates Swift enums from Kotlin enums with several benefits:

- **Exhaustive Switching**: Enables exhaustive switch statements without requiring a default case
- **Improved Naming**: Converts Kotlin enum case names to follow Swift naming conventions
- **Compile-time Safety**: Provides warnings if enum cases are not fully handled

#### Example Transformation
Kotlin enum:
```kotlin
enum class Turn {
    Left, Right, Neither
}
```

Swift with SKIE:
```swift
func changeDirection(turn: Turn) {
    switch turn {
    case .left:        goLeft()
    case .right:       goRight()
    case .neither:     noChange()
    }
    // No default case needed
}
```

#### Conversion Methods
- `toKotlinEnum()`: Convert Swift enum to original Kotlin enum
- `toSwiftEnum()`: Convert Kotlin enum to Swift enum

### Sealed Classes

SKIE generates a Swift enum that wraps Kotlin sealed classes, enabling:

- Exhaustive switching over sealed class hierarchies
- An `onEnum(of:)` function to convert Kotlin classes to Swift enums
- Support for optional sealed classes

#### Example Usage
Kotlin Sealed Class:
```kotlin
sealed class Status {
    object Loading : Status()
    data class Error(val message: String) : Status()
    data class Success(val result: SomeData) : Status()
}
```

Swift Usage With SKIE:
```swift
switch onEnum(of: status) {
case .loading:
    showLoading()
case .error(let error):
    showError(message: error.message)
case .success(let success):
    showResult(data: success.result)
}
```

### Default Arguments

SKIE generates Kotlin overloads to simulate default argument semantics, though this feature is:

- Currently considered deprecated due to potential performance overhead
- Disabled by default
- Generates `O(2^n)` overloads (where n = default arguments)
- Limited to 5 default arguments

#### Example
For a `User` data class with a `copy` method, SKIE generates:
- `User.doCopy()`
- `User.doCopy(name:)`
- `User.doCopy(age:)`

### Global Functions

SKIE generates actual global functions to simplify calling Kotlin global functions in Swift.

#### Example
In Kotlin:
```kotlin
fun globalFunction(i: Int): Int = i
```

Without SKIE:
```swift
FileKt.globalFunction(i: 1)
```

With SKIE:
```swift
globalFunction(i: 1)
```

### Interface Extensions

SKIE generates Swift wrappers for interface extensions, allowing more natural Swift syntax:

#### Functions
- Before SKIE: `FileKt.foo(C(), i: 1)`
- With SKIE: `C().foo(i: 1)`

#### Properties
- Before SKIE: Separate getter/setter functions
- With SKIE: Direct property access like `c.bar = 1`

### Overloaded Functions

SKIE improves function naming across languages by:
- Removing unnecessary underscores in function names
- Allowing more natural function calls in Swift

#### Example
Kotlin:
```kotlin
fun foo(i: Int) {}
fun foo(i: String) {}
```

With SKIE, Swift can call them as:
```swift
foo(i: 1)
foo(i: "A")
```

### Suspend Functions

SKIE generates actual Swift async functions from Kotlin suspend functions with:

- Full cancellation support between Kotlin and Swift
- Thread flexibility (can be called from any thread)
- Native Swift async/await behavior

#### Example Usage
```swift
let chatRoom = ChatRoom()
let task = Task.detached {
    try? await chatRoom.send(message: "some message")
}
task.cancel() // Cancels Kotlin coroutine
```

### Flows

SKIE automatically converts Kotlin Flows to Swift AsyncSequence:

- Preserves generic type arguments
- Supports two-way cancellation
- Handles Flow, SharedFlow, StateFlow, and their Mutable variants
- Seamless integration with Swift's task and lifecycle management

#### Example Use Case
```swift
class ChatRoomViewModel: ObservableObject {
    let chatRoom = ChatRoom()
    @Published private(set) var messages: [String] = []
    
    @MainActor
    func activate() async {
        for await messages in chatRoom.messages {
            self.messages = messages
        }
    }
}
```

### Swift Code Bundling

Allows KMP developers to manually write Swift wrappers and bundle them directly into the Kotlin framework:

- Place Swift code in corresponding `swift` source sets
- Follows Kotlin source set hierarchy (e.g., `src/commonMain/swift`)
- Simplifies distribution by keeping everything in a single framework
- Remember to use `public` modifier explicitly in Swift code

### Flows in SwiftUI

To enable SwiftUI flow support:
```kotlin
skie {
    features {
        enableSwiftUIObservingPreview = true
    }
}
```

#### Observing SwiftUI View
With initial values for StateFlows:
```swift
Observing(viewModel.counter.withInitialValue(0), viewModel.toggle) { counter, toggle in
    Text("Counter: \(counter), Toggle: \(toggle)")
}
```

#### collect View Modifier
```swift
Text("Bound counter: \(boundCounter)")
    .collect(flow: viewModel.counter, into: $counter)
```

### Combine Framework Integration

SKIE provides preview support for bridging with Apple's Combine framework:

1. **suspend fun → Combine.Future**
   - Enable: `enableFutureCombineExtensionPreview = true`
   - Usage: `let future = Future(async: helloWorld)`

2. **Flow → Combine.Publisher**
   - Enable: `enableFlowCombineConvertorPreview = true`
   - Usage: `let publisher = helloWorld().toPublisher()`

## Known Issues and Limitations

1. **Gradle Caching Issue**
   - Gradle fails to resolve some SKIE artifacts sometimes and caches the 404 result

2. **Missing Foundation Classes**
   - Frameworks produced by Kotlin/Native transitively export Foundation framework by default

3. **Lambda Type Limitations**
   - SKIE cannot currently generate Swift code that contains types with lambdas used as a type argument for generic Kotlin classes

4. **Cinterop Restrictions**
   - SKIE cannot directly generate code that uses types provided by custom cinterop bindings

## Configuration

SKIE offers two primary configuration methods:

### 1. Gradle Configuration
Global configuration through the `skie` Gradle extension:
```kotlin
skie {
    features {
        group {
            FlowInterop.Enabled(false)
        }
    }
}
```

### 2. Annotation Configuration
Configure directly in source code using SKIE annotations, which override Gradle settings by default.

### Configuration Options

#### Enums Configuration
- **Enabled** (Boolean, default: `true`): Controls Swift enum generation
- **LegacyCaseName** (Boolean, default: `false`): Use original Kotlin naming algorithm

#### Sealed Classes Configuration
- **Enabled** (Boolean, default: `true`): Generate wrapping enum and `onEnum(of:)` function
- **ExportEntireHierarchy** (Boolean, default: `true`): Export all public sealed children
- **Function Configuration**: Customize function name, argument label, parameter name
- **ElseName** (String, default: "else"): Customize else case name

#### Default Arguments Configuration
- **Enabled** (Boolean, default: `false`): Enable default arguments feature
- **MaximumDefaultArgumentCount** (Int, default: `5`): Maximum default arguments per function
- **DefaultArgumentsInExternalLibraries** (Boolean, default: `false`): For external libraries

#### Functions Configuration
- **FileScopeConversion** (Boolean, default: `true`): Generate wrappers for global functions
- **LegacyName** (Boolean, default: `false`): Use original Kotlin naming algorithm

#### Coroutines Configuration
- **CoroutinesInterop** (Boolean, default: `true`): Enable/disable coroutines features
- **SuspendInterop** (Boolean, default: `true`): Configure suspend functions
- **FlowInterop** (Boolean, default: `true`): Configure Flow conversion

#### Warnings Configuration
- **NameCollision** (Boolean, default: `false`): Control name collision warnings

#### Swift Compiler Configuration
- **produceDistributableFramework()**: Enable distribution support
- **enableSwiftLibraryEvolution**: Manual Swift library evolution
- **noClangModuleBreadcrumbsInStaticFrameworks**: For static frameworks
- **enableRelativeSourcePathsInDebugSymbols**: Enable relative source paths

#### Swift Code Bundling Configuration
- **Enabled** (Boolean, default: `true`): Control Swift code bundling

### Complete Disabling
To completely disable SKIE:
```kotlin
skie {
    isEnabled.set(false)
}
```

## Best Practices

1. Start with default configuration and adjust as needed
2. Use annotations for fine-grained control over specific declarations
3. Review migration documentation when updating existing projects
4. Be aware of performance implications with default arguments feature
5. Test thoroughly when enabling preview features (SwiftUI, Combine)
6. Use Swift code bundling for complex Swift-specific wrappers
7. Monitor for name collisions and resolve them appropriately

## Resources

- Features: https://skie.touchlab.co/features/
- Installation: https://skie.touchlab.co/Installation
- Known Issues: https://skie.touchlab.co/category/known-issues-and-limitations
- Configuration: https://skie.touchlab.co/configuration/
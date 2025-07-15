import Foundation

import shared
import Combine

// Helper to collect Kotlin Flows in iOS
class FlowCollector<T>: ObservableObject {
    @Published var value: T?
    @Published var isLoading = false
    @Published var error: Error?

    private var cancellable: Kotlinx_coroutines_coreJob?

    init(flow: Kotlinx_coroutines_coreFlow) {
        isLoading = true

        // Create a simple collector that emits values
        let collector = SimpleFlowCollector<T> { [weak self] value in
            DispatchQueue.main.async {
                self?.value = value
                self?.isLoading = false
                self?.error = nil
            }
        }

        // Collect the flow
        flow.collect(collector: collector) { [weak self] error in
            DispatchQueue.main.async {
                self?.isLoading = false
                if let error = error {
                    print("Flow collection error: \(error)")
                    self?.error = error
                }
            }
        }
    }

    func cancel() {
        // Flow collection doesn't return a cancellable job in this implementation
        // The collection will stop when the FlowCollector is deallocated
    }

    deinit {
        cancel()
    }
}

// Simple Flow Collector implementation
class SimpleFlowCollector<T>: NSObject, Kotlinx_coroutines_coreFlowCollector {
    private let onValue: (T) -> Void

    init(onValue: @escaping (T) -> Void) {
        self.onValue = onValue
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let typedValue = value as? T {
            onValue(typedValue)
        }
        completionHandler(nil)
    }
}

// Extension to make Flow collection easier
extension Kotlinx_coroutines_coreFlow {
    func collect<T>(
        onEach: @escaping (T) -> Void,
        onError: @escaping (Error) -> Void = { _ in },
        onComplete: @escaping () -> Void = {}
    ) -> FlowCollector<T> {
        let collector = FlowCollector<T>(flow: self)
        return collector
    }

    // Helper to create empty flows for mock implementations
    static func createEmptyFlow() -> Kotlinx_coroutines_coreFlow {
        // This is a temporary mock - in a real implementation we'd use proper Kotlin Flow creation
        return EmptyFlow()
    }
}

// Convenience methods for common repository operations
extension FlowCollector {
    // Helper to collect cocktail lists
    static func collectCocktailList(from flow: Kotlinx_coroutines_coreFlow) -> FlowCollector<NSArray> {
        return FlowCollector<NSArray>(flow: flow)
    }

    // Helper to collect single cocktail
    static func collectCocktail(from flow: Kotlinx_coroutines_coreFlow) -> FlowCollector<Cocktail> {
        return FlowCollector<Cocktail>(flow: flow)
    }

    // Helper to collect boolean values
    static func collectBoolean(from flow: Kotlinx_coroutines_coreFlow) -> FlowCollector<KotlinBoolean> {
        return FlowCollector<KotlinBoolean>(flow: flow)
    }

    // Helper to collect cart items
    static func collectCartItems(from flow: Kotlinx_coroutines_coreFlow) -> FlowCollector<NSArray> {
        return FlowCollector<NSArray>(flow: flow)
    }

    // Helper to collect double values
    static func collectDouble(from flow: Kotlinx_coroutines_coreFlow) -> FlowCollector<KotlinDouble> {
        return FlowCollector<KotlinDouble>(flow: flow)
    }
}

// Mock empty flow implementation
class EmptyFlow: NSObject, Kotlinx_coroutines_coreFlow {
    func collect(collector: Kotlinx_coroutines_coreFlowCollector, completionHandler: @escaping (Error?) -> Void) {
        // Empty flow - just complete immediately
        completionHandler(nil)
    }
}
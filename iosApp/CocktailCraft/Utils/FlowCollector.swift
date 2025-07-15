import Foundation

import shared
import Combine

// Helper to collect Kotlin Flows in iOS
class FlowCollector<T> {
    @Published var value: T?
    private var cancellable: Kotlinx_coroutines_coreJob?

    init(flow: Kotlinx_coroutines_coreFlow) {
        // Create a simple collector that emits the first value
        let collector = SimpleFlowCollector<T> { [weak self] value in
            DispatchQueue.main.async {
                self?.value = value
            }
        }

        // Collect the flow
        flow.collect(collector: collector) { error in
            if let error = error {
                print("Flow collection error: \(error)")
            }
        }
    }
    
    func cancel() {
        cancellable?.cancel(cause: nil)
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

// Mock empty flow implementation
class EmptyFlow: NSObject, Kotlinx_coroutines_coreFlow {
    func collect(collector: Kotlinx_coroutines_coreFlowCollector, completionHandler: @escaping (Error?) -> Void) {
        // Empty flow - just complete immediately
        completionHandler(nil)
    }
}
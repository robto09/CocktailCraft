import Foundation
import shared

// Simple FlowCollector implementation for Kotlin Flow integration
class FlowCollector<T>: shared.Kotlinx_coroutines_coreFlowCollector {
    private let onValue: (T) -> Void

    init(_ onValue: @escaping (T) -> Void) {
        self.onValue = onValue
    }

    func __emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let typedValue = value as? T {
            onValue(typedValue)
        }
        completionHandler(nil)
    }
}

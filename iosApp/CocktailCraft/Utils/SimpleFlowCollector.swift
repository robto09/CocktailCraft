import Foundation
import shared

// Simple collector for repository flows (temporary workaround until SKIE fully supports all flows)
class SimpleFlowCollector<T>: NSObject, Kotlinx_coroutines_coreFlowCollector {
    private let onValue: (T?) -> Void

    init(onValue: @escaping (T?) -> Void) {
        self.onValue = onValue
        super.init()
    }

    func __emit(value: Any?) async throws {
        onValue(value as? T)
    }
}

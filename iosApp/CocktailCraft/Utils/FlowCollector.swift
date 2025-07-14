import Foundation
import shared
import Combine

// Helper to collect Kotlin Flows in iOS
class FlowCollector<T> {
    private var cancellable: Kotlinx_coroutines_coreJob?
    
    func collect(
        flow: Kotlinx_coroutines_coreFlow,
        onEach: @escaping (T) -> Void,
        onCompletion: @escaping (Error?) -> Void
    ) {
        cancellable = FlowUtilsKt.collectAsState(
            flow,
            scope: nil,
            started: Kotlinx_coroutines_coreSharingStarted.companion.Eagerly,
            initialValue: nil
        ) as? Kotlinx_coroutines_coreJob
    }
    
    func cancel() {
        cancellable?.cancel(cause: nil)
    }
    
    deinit {
        cancel()
    }
}

// Extension to make Flow collection easier
extension Kotlinx_coroutines_coreFlow {
    func collect<T>(
        onEach: @escaping (T) -> Void,
        onError: @escaping (Error) -> Void = { _ in },
        onComplete: @escaping () -> Void = {}
    ) -> FlowCollector<T> {
        let collector = FlowCollector<T>()
        collector.collect(
            flow: self,
            onEach: onEach,
            onCompletion: { error in
                if let error = error {
                    onError(error)
                } else {
                    onComplete()
                }
            }
        )
        return collector
    }
}
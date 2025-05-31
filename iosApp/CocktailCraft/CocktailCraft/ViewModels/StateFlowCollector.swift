//
//  StateFlowCollector.swift
//  CocktailCraft
//
//  Helper for collecting Kotlin StateFlow in iOS
//

import Foundation
import shared
import Combine

/// Helper class to collect Kotlin StateFlow and convert to Combine Publisher
class StateFlowCollector<T> {
    private var cancellable: Kotlinx_coroutines_coreDisposableHandle?
    
    func collect(flow: Kotlinx_coroutines_coreStateFlow, callback: @escaping (Any?) -> Void) {
        // Create a collector that implements the FlowCollector protocol
        let collector = Collector { value in
            callback(value)
        }
        
        // Start collecting the flow
        cancellable = FlowExtensionsKt.collect(flow, collector: collector) { error in
            if let error = error {
                print("Flow collection error: \(error)")
            }
        }
    }
    
    func cancel() {
        cancellable?.dispose()
    }
    
    deinit {
        cancel()
    }
}

// Helper collector class
private class Collector: Kotlinx_coroutines_coreFlowCollector {
    let callback: (Any?) -> Void
    
    init(callback: @escaping (Any?) -> Void) {
        self.callback = callback
    }
    
    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        callback(value)
        completionHandler(nil)
    }
}
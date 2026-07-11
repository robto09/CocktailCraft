import SwiftUI
import shared
import Observation

/**
 * Generic base for the iOS wrappers around shared KMP ViewModels.
 *
 * Every wrapper does the same three things; they live here exactly once:
 *  - seed `state` synchronously from the shared uiState StateFlow so the
 *    first frame renders current data,
 *  - mirror the uiState and error flows into Observation-tracked
 *    properties via SKIE's AsyncSequence bridging,
 *  - cancel the observation tasks on deinit.
 *
 * Subclasses hold their typed shared ViewModel for method forwarding and
 * call `super.init(uiState:errorFlow:)`. Koin-`single`-backed wrappers
 * must NOT touch the shared instance in deinit; factory-scoped wrappers
 * (CocktailDetail) own theirs and call `onCleared()` in their own deinit.
 */
@MainActor
@Observable
class SharedViewModelWrapper<UiState: AnyObject> {
    // Consolidated UI state mirrored from the shared ViewModel
    private(set) var state: UiState
    // The single error channel from the shared ViewModel base class
    var error: ErrorHandler.UserFriendlyError? = nil

    @ObservationIgnored private var observationTasks: [Task<Void, Never>] = []

    init(uiState: SkieSwiftStateFlow<UiState>,
         errorFlow: SkieSwiftOptionalStateFlow<ErrorHandler.UserFriendlyError>) {
        // Seed synchronously so the first frame renders the current state
        self.state = uiState.value

        // The Tasks capture the flows, not the wrapper — [weak self] breaks
        // the wrapper <-> task retain cycle so deinit can run and cancel them.
        // Both inherit @MainActor, so assignments land on the main thread.
        observationTasks.append(Task { [weak self] in
            for await value in uiState {
                self?.state = value
            }
        })

        observationTasks.append(Task { [weak self] in
            for await value in errorFlow {
                self?.error = value
            }
        })
    }

    deinit {
        observationTasks.forEach { $0.cancel() }
    }

    // MARK: - Forwarded-call error policy

    /// Runs a forwarded shared-ViewModel call under the single error policy:
    /// failures are debug-logged here and nowhere else — user-facing
    /// surfacing already happens through the mirrored `error` StateFlow, so
    /// subclasses never hand-roll do/try/catch around forwarders.
    func run(_ operation: () async throws -> Void) async {
        do {
            try await operation()
        } catch {
            logForwardedFailure(error)
        }
    }

    /// Variant for forwarders that produce a value: returns `fallback` when
    /// the shared call throws.
    func run<T>(fallback: T, _ operation: () async throws -> T) async -> T {
        do {
            return try await operation()
        } catch {
            logForwardedFailure(error)
            return fallback
        }
    }

    private func logForwardedFailure(_ error: Error) {
        #if DEBUG
        print("\(type(of: self)): forwarded shared call failed: \(error)")
        #endif
    }
}

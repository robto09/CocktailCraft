import SwiftUI
import shared
import Observation

/**
 * Generic base for the iOS wrappers around shared KMP ViewModels.
 *
 * Every wrapper does the same things; they live here exactly once:
 *  - seed `state` synchronously from the shared uiState StateFlow so the
 *    first frame renders current data,
 *  - mirror the uiState and the base-class error flows into
 *    Observation-tracked properties via SKIE's AsyncSequence bridging,
 *  - forward `clearError()` (the error is the one consumable one-shot
 *    channel; views clear it once shown),
 *  - cancel the observation tasks on deinit, and clear a factory-scoped
 *    shared instance the wrapper owns.
 *
 * Views read `state.<field>` — the shared UiState is the single state
 * surface. Subclasses only forward intents to their typed shared ViewModel
 * (plus Swift-only presentation formatting) and call
 * `super.init(viewModel:uiState:)`.
 */
@MainActor
@Observable
class SharedViewModelWrapper<UiState: AnyObject> {
    // Consolidated UI state mirrored from the shared ViewModel
    private(set) var state: UiState
    // The single error channel from the shared ViewModel base class
    var error: ErrorHandler.UserFriendlyError? = nil

    @ObservationIgnored private let baseViewModel: SharedViewModel
    @ObservationIgnored private let ownsViewModel: Bool
    @ObservationIgnored private var observationTasks: [Task<Void, Never>] = []

    /// - Parameter ownsViewModel: true only for Koin `factory` instances the
    ///   wrapper owns (CocktailDetail, Review) — their scope is cleared in
    ///   deinit. Koin-`single`-backed wrappers must never clear the shared
    ///   instance, whose scope has to survive any one wrapper.
    init(viewModel: SharedViewModel,
         uiState: SkieSwiftStateFlow<UiState>,
         ownsViewModel: Bool = false) {
        self.baseViewModel = viewModel
        self.ownsViewModel = ownsViewModel
        // Seed synchronously so the first frame renders the current state
        self.state = uiState.value

        let errorFlow = viewModel.error

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
        if ownsViewModel {
            baseViewModel.onCleared()
        }
    }

    func clearError() {
        baseViewModel.clearError()
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

import SwiftUI
import shared
import Observation

/**
 * Generic base for the iOS wrappers around shared KMP ViewModels.
 *
 * Every wrapper does the same things; they live here exactly once:
 *  - seed `state` synchronously from the shared uiState StateFlow so the
 *    first frame renders current data,
 *  - mirror the uiState, the base-class error flow and its one-shot
 *    `events` flow into Observation-tracked properties via SKIE's
 *    AsyncSequence bridging,
 *  - forward `clearError()` (the error is consumable; views clear it once
 *    shown) and offer `clearEvent()` for the mirrored event,
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
    // The latest one-shot outcome from the shared `events` flow (e.g.
    // "password changed"), held until a view shows it and calls clearEvent()
    var event: UiEvent? = nil

    @ObservationIgnored private let baseViewModel: SharedViewModel
    // Set only for a Koin `factory` instance this wrapper owns
    @ObservationIgnored private let ownedViewModel: SharedViewModel?
    @ObservationIgnored private var observationTasks: [Task<Void, Never>] = []

    /// - Parameter ownsViewModel: true only for Koin `factory` instances the
    ///   wrapper owns (CocktailDetail, Review) — they are cleared (scope
    ///   cancelled, onCleared run) after deinit. Koin-`single`-backed
    ///   wrappers must never clear the shared instance, whose scope has to
    ///   survive any one wrapper.
    init(viewModel: SharedViewModel,
         uiState: SkieSwiftStateFlow<UiState>,
         ownsViewModel: Bool = false) {
        self.baseViewModel = viewModel
        self.ownedViewModel = ownsViewModel ? viewModel : nil
        // Seed synchronously so the first frame renders the current state
        self.state = uiState.value

        let errorFlow = viewModel.error
        let eventFlow = viewModel.events

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

        observationTasks.append(Task { [weak self] in
            for await value in eventFlow {
                self?.event = value
            }
        })
    }

    // deinit is nonisolated even on a @MainActor class (an isolated deinit
    // needs iOS 18.4), and the last release isn't guaranteed to happen on
    // the main thread. So it calls nothing main-actor-isolated: it cancels
    // the task handles (Sendable, cancellable from any thread) and sends
    // the owned ViewModel to the main actor to be cleared there — Kotlin's
    // ViewModel clearing is main-thread-only.
    deinit {
        observationTasks.forEach { $0.cancel() }
        if let ownedViewModel {
            Task { @MainActor in
                getSharedKoinHelper().clearScreenViewModel(viewModel: ownedViewModel)
            }
        }
    }

    func clearError() {
        baseViewModel.clearError()
    }

    /// Mark the mirrored `event` as shown (it is Swift-side only: the shared
    /// events flow has no replay, so there is nothing to clear there).
    func clearEvent() {
        event = nil
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

/**
 * Builds a view's ViewModel wrapper exactly once per view identity.
 *
 * `@State var viewModel = SomeViewModelSKIE()` evaluates the initializer
 * every time a parent re-renders and re-creates the view struct. SwiftUI
 * keeps the first instance and discards the rest, but each discarded
 * wrapper has already resolved a shared ViewModel from Koin (a fresh
 * factory instance for the screen-scoped ones) and started its observation
 * tasks. Holding this box in @State instead makes those throwaway copies
 * free: only the box SwiftUI keeps ever runs `make`, on first access.
 *
 *     @State private var model = ViewModelHolder(FavoritesViewModelSKIE.init)
 *     private var viewModel: FavoritesViewModelSKIE { model.wrapper }
 */
@MainActor
final class ViewModelHolder<Wrapper: AnyObject> {
    private let make: @MainActor () -> Wrapper
    private var instance: Wrapper?

    init(_ make: @escaping @MainActor () -> Wrapper) {
        self.make = make
    }

    var wrapper: Wrapper {
        if let instance { return instance }
        let created = make()
        instance = created
        return created
    }
}

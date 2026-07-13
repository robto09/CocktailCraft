import XCTest
@testable import CocktailCraft
import shared

/**
 * SharedViewModelWrapper behavior over a real shared ViewModel — the theme
 * VM is the cheapest Koin single to resolve (settings only, no network).
 * Resolves through the Koin graph the test host already started (TestSetup).
 */
@MainActor
final class SharedViewModelWrapperTests: TestSetup {

    /// Koin `single`: every resolution is the same shared instance, so a
    /// computed accessor avoids setUp/tearDown (whose nonisolated overrides
    /// couldn't touch MainActor state anyway).
    private var sharedViewModel: SharedThemeViewModel {
        getSharedKoinHelper().getSharedThemeViewModel()
    }

    private func makeWrapper() -> SharedViewModelWrapper<ThemeUiState> {
        SharedViewModelWrapper(uiState: sharedViewModel.uiState,
                               errorFlow: sharedViewModel.error)
    }

    /// Polls on the main actor — each sleep suspends, so the wrapper's
    /// observation tasks get to run between checks. Bounded at 2s.
    private func waitUntil(timeout: TimeInterval = 2.0,
                           _ condition: () -> Bool) async -> Bool {
        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline {
            if condition() { return true }
            try? await Task.sleep(nanoseconds: 50_000_000)
        }
        return condition()
    }

    func testStateIsSeededSynchronously() {
        // No await between construction and assertion: whatever the wrapper
        // holds here came from the synchronous uiState.value seed in init,
        // not from the async mirroring task (which hasn't had a chance to
        // run on the main actor yet).
        let wrapper = makeWrapper()
        XCTAssertEqual(wrapper.state, sharedViewModel.uiState.value)
    }

    func testStateMirrorsSharedMutation() async throws {
        let wrapper = makeWrapper()
        let viewModel = sharedViewModel

        let original = viewModel.uiState.value.fontSize
        let target = original == "small" ? "large" : "small"
        // The theme VM is the app's Koin single and persists its settings —
        // put the real value back whatever happens after this point
        // (continueAfterFailure=false aborts the test body on failure).
        addTeardownBlock {
            try await viewModel.setFontSize(size: original)
        }

        try await viewModel.setFontSize(size: target)

        let mirrored = await waitUntil { wrapper.state.fontSize == target }
        XCTAssertTrue(mirrored,
                      "wrapper.state should mirror the shared mutation within 2s")
    }

    func testWrapperDeinitsWhenReleased() async {
        var wrapper: SharedViewModelWrapper<ThemeUiState>? = makeWrapper()
        weak var weakWrapper = wrapper
        XCTAssertNotNil(weakWrapper)

        wrapper = nil

        // The observation tasks capture [weak self]; nothing else should be
        // keeping the wrapper alive once the last strong reference is gone.
        let released = await waitUntil { weakWrapper == nil }
        XCTAssertTrue(released, "wrapper should deinit promptly after release")
    }
}

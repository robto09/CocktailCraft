import XCTest
import SwiftUI
@testable import CocktailCraft
import shared

/// Full-screen forms that are driven by closures rather than ViewModels, so
/// they render with no Koin/network dependencies. The ViewModel-bound tab
/// screens (Home, Cart, Favorites, Orders, Profile, Detail) are covered
/// through the components they are built from.
final class ScreenSnapshotTests: SnapshotTestCase {

    /// iPhone 17 Pro screen height in points
    private static let screenHeight: CGFloat = 874

    func testSignInView() {
        assertSnapshots(
            of: SignInView(onDismiss: {}, onSignIn: { _, _ in }),
            height: Self.screenHeight
        )
    }

    func testEditProfileView() {
        assertSnapshots(
            of: EditProfileView(
                currentName: "Alex",
                currentEmail: "alex@example.com",
                onDismiss: {},
                onSave: { _, _ in },
                isValidEmail: { _ in true }
            ),
            height: Self.screenHeight
        )
    }

    func testWriteReviewView() {
        assertSnapshots(
            of: WriteReviewView(
                submissionError: nil,
                onDismiss: {},
                onSubmit: { _, _, _ in },
                onClearError: {}
            ),
            height: Self.screenHeight
        )
    }

    func testWriteReviewViewWithError() {
        assertSnapshots(
            of: WriteReviewView(
                submissionError: "Comment is too short.",
                onDismiss: {},
                onSubmit: { _, _, _ in },
                onClearError: {}
            ),
            height: Self.screenHeight
        )
    }
}

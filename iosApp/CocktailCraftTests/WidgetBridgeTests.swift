import XCTest
@testable import CocktailCraft

/// Deep-link format shared by the widgets (writers) and the app (reader) —
/// pure Foundation logic, no Koin needed.
final class WidgetDeepLinkTests: XCTestCase {

    func testCocktailURLRoundTrip() throws {
        let url = try XCTUnwrap(WidgetDeepLink.cocktailURL(id: "11007"))
        XCTAssertEqual(url.absoluteString, "cocktailcraft://cocktail/11007")
        XCTAssertEqual(WidgetDeepLink.cocktailId(from: url), "11007")
    }

    func testWrongSchemeIsRejected() throws {
        let url = try XCTUnwrap(URL(string: "https://cocktail/11007"))
        XCTAssertNil(WidgetDeepLink.cocktailId(from: url))
    }

    func testWrongHostIsRejected() throws {
        let url = try XCTUnwrap(URL(string: "cocktailcraft://order/11007"))
        XCTAssertNil(WidgetDeepLink.cocktailId(from: url))
    }

    func testMissingOrEmptyIdIsRejected() throws {
        // No path at all, and a bare trailing slash.
        XCTAssertNil(WidgetDeepLink.cocktailId(from: try XCTUnwrap(URL(string: "cocktailcraft://cocktail"))))
        XCTAssertNil(WidgetDeepLink.cocktailId(from: try XCTUnwrap(URL(string: "cocktailcraft://cocktail/"))))
    }
}

/// Snapshot round-trip through the real App Group container. The container
/// is shared with the app and the widget extension, so setUp preserves
/// whatever snapshot was there and tearDown puts it back — a test run must
/// not clobber manual QA state.
final class WidgetSnapshotStoreTests: XCTestCase {

    private var previousSnapshot: WidgetFavoritesSnapshot?

    override func setUpWithError() throws {
        continueAfterFailure = false
        guard FileManager.default.containerURL(
            forSecurityApplicationGroupIdentifier: WidgetSnapshotStore.appGroupId
        ) != nil else {
            throw XCTSkip("App Group container unavailable in this environment")
        }
        previousSnapshot = WidgetSnapshotStore.read()
    }

    override func tearDown() {
        if let previousSnapshot {
            WidgetSnapshotStore.write(previousSnapshot)
        } else if let url = FileManager.default
            .containerURL(forSecurityApplicationGroupIdentifier: WidgetSnapshotStore.appGroupId)?
            .appendingPathComponent("widget-favorites-snapshot.json") {
            // Nothing was there before: remove the test file rather than
            // leaving a fabricated snapshot behind. Keep the filename in
            // sync with WidgetSnapshotStore.filename (private on purpose).
            try? FileManager.default.removeItem(at: url)
        }
        previousSnapshot = nil
        super.tearDown()
    }

    func testWriteReadRoundTrip() throws {
        let written = WidgetFavoritesSnapshot(
            favorites: [
                WidgetCocktail(id: "test-11007", name: "Unit Test Margarita", imageUrl: nil, price: 12.5)
            ],
            updatedAt: Date()
        )

        // The test host's WidgetDataBridge writes one launch-time snapshot
        // from an async Task; retry once so that single concurrent write
        // cannot flake the round-trip.
        var restored: WidgetFavoritesSnapshot?
        for _ in 0..<2 {
            WidgetSnapshotStore.write(written)
            restored = WidgetSnapshotStore.read()
            if restored?.favorites.first?.id == "test-11007" { break }
        }

        let snapshot = try XCTUnwrap(restored)
        XCTAssertEqual(snapshot.favorites.count, 1)
        let favorite = try XCTUnwrap(snapshot.favorites.first)
        XCTAssertEqual(favorite.id, "test-11007")
        XCTAssertEqual(favorite.name, "Unit Test Margarita")
        XCTAssertNil(favorite.imageUrl)
        XCTAssertEqual(favorite.price, 12.5, accuracy: 0.0001)
        XCTAssertEqual(snapshot.updatedAt.timeIntervalSince1970,
                       written.updatedAt.timeIntervalSince1970,
                       accuracy: 0.001)
    }
}

import XCTest
import SwiftUI
import SnapshotTesting
@testable import CocktailCraft
import shared

/// Appearance a snapshot is rendered in. The raw value becomes the snapshot
/// name suffix (`testName.<appearance>.png`), so renaming a case orphans its
/// reference images.
enum SnapshotAppearance: String {
    case light
    case dark
    /// Dark mode with the app's High Contrast setting on
    case darkHighContrast
    /// The app's "Extra Large" font setting (.xxLarge Dynamic Type)
    case largeFont
    /// The largest system accessibility text size (AX5)
    case largestFont
    /// A non-default accent color (blue) in light mode
    case accentBlue

    static let lightAndDark: [SnapshotAppearance] = [.light, .dark]
    static let accessibility: [SnapshotAppearance] = [.darkHighContrast, .largeFont, .largestFont, .accentBlue]

    var isDarkMode: Bool { self == .dark || self == .darkHighContrast }
    var isHighContrast: Bool { self == .darkHighContrast }
    var accentName: String { self == .accentBlue ? "blue" : AccentColorTokens.shared.DEFAULT }
    var dynamicTypeSize: DynamicTypeSize {
        switch self {
        case .largeFont: return .xxLarge
        case .largestFont: return .accessibility5
        default: return .large
        }
    }

    var contentSizeCategory: UIContentSizeCategory {
        switch self {
        case .largeFont: return .extraExtraLarge
        case .largestFont: return .accessibilityExtraExtraExtraLarge
        default: return .large
        }
    }
}

/// Base class for SwiftUI snapshot tests (pointfreeco/swift-snapshot-testing).
///
/// Every snapshot is rendered at a fixed width, @3x scale, phone idiom and a
/// pinned Dynamic Type size, so output only depends on the iOS runtime's
/// text rendering. Reference images were recorded on the iPhone 17 Pro
/// simulator running iOS 26.5, a runtime GitHub's macos-26 runners have, so
/// CI verifies them; on any other runtime the tests are reported
/// as skipped (XCTSkip, with the runtime mismatch as the reason) rather than
/// failing on font-rasterization noise — or, with
/// `TEST_RUNNER_SNAPSHOT_REQUIRE_REFERENCE_RUNTIME=1`, fail, for runs that
/// must verify the snapshots (CI).
///
/// Record new/changed references with
/// `TEST_RUNNER_SNAPSHOT_TESTING_RECORD=all xcodebuild test ...` (see README →
/// Testing). Without it, missing references are recorded (and the test fails
/// once) and existing ones are verified.
@MainActor
class SnapshotTestCase: XCTestCase {

    /// The runtime the committed reference images were recorded on.
    static let referenceOSVersion = (major: 26, minor: 5)

    /// iPhone 17 Pro point width — wide enough for every component.
    /// nonisolated: it is a default argument, evaluated outside the main actor.
    nonisolated static let defaultWidth: CGFloat = 402

    override func setUpWithError() throws {
        try super.setUpWithError()
        let os = ProcessInfo.processInfo.operatingSystemVersion
        let reference = Self.referenceOSVersion
        guard os.majorVersion != reference.major || os.minorVersion != reference.minor else { return }

        let reason = "Snapshot test NOT run: references were recorded on iOS "
            + "\(reference.major).\(reference.minor) (iPhone 17 Pro simulator), this runtime is "
            + "iOS \(os.majorVersion).\(os.minorVersion). Run on an iOS \(reference.major).\(reference.minor) "
            + "simulator to verify snapshots."
        if ProcessInfo.processInfo.environment["SNAPSHOT_REQUIRE_REFERENCE_RUNTIME"] != nil {
            throw SnapshotRuntimeMismatch(description: reason)
        }
        throw XCTSkip(reason)
    }

    /// Fails the test (instead of skipping it) under SNAPSHOT_REQUIRE_REFERENCE_RUNTIME.
    private struct SnapshotRuntimeMismatch: Error, CustomStringConvertible {
        let description: String
    }

    /// Snapshots `content` once per appearance, sized to fit at `width`.
    /// Pass a `height` for full-screen views that expand to fill their frame.
    func assertSnapshots<Content: View>(
        of content: @autoclosure () -> Content,
        in appearances: [SnapshotAppearance] = SnapshotAppearance.lightAndDark,
        width: CGFloat = SnapshotTestCase.defaultWidth,
        height: CGFloat? = nil,
        file: StaticString = #filePath,
        testName: String = #function,
        line: UInt = #line
    ) {
        for appearance in appearances {
            let view = content()
                .padding(height == nil ? 8 : 0)
                .frame(width: width, height: height)
                .background(AppColors.background(isDarkMode: appearance.isDarkMode))
                // What ContentView publishes from the shared theme state
                .environment(\.appearance, AppAppearance(
                    isDarkMode: appearance.isDarkMode,
                    accentName: appearance.accentName,
                    isHighContrast: appearance.isHighContrast
                ))
                .environment(\.colorScheme, appearance.isDarkMode ? .dark : .light)
                .dynamicTypeSize(appearance.dynamicTypeSize)
                // Entrance animations (onAppear + withAnimation) jump straight
                // to their end state
                .transaction { transaction in
                    transaction.animation = nil
                    transaction.disablesAnimations = true
                }

            let host = UIHostingController(rootView: view)
            let size = height.map { CGSize(width: width, height: $0) }
                ?? host.sizeThatFits(in: CGSize(width: width, height: .greatestFiniteMagnitude))
            Self.settle(host, size: size)

            assertSnapshot(
                of: host,
                as: .image(size: size, traits: Self.traits(for: appearance)),
                named: appearance.rawValue,
                file: file,
                testName: testName,
                line: line
            )
        }
    }

    /// Shows the host in a window for one run-loop pass so onAppear state
    /// changes (e.g. SearchEmptyStateView's fade-in) are applied before the
    /// capture; SnapshotTesting alone renders the very first frame.
    private static func settle(_ host: UIViewController, size: CGSize) {
        let window = UIWindow(frame: CGRect(origin: .zero, size: size))
        window.rootViewController = host
        window.isHidden = false
        RunLoop.main.run(until: Date().addingTimeInterval(0.05))
        window.rootViewController = nil
        window.isHidden = true
    }

    private static func traits(for appearance: SnapshotAppearance) -> UITraitCollection {
        UITraitCollection { traits in
            traits.userInterfaceStyle = appearance.isDarkMode ? .dark : .light
            traits.displayScale = 3
            traits.userInterfaceIdiom = .phone
            traits.horizontalSizeClass = .compact
            traits.verticalSizeClass = .regular
            traits.layoutDirection = .leftToRight
            traits.preferredContentSizeCategory = appearance.contentSizeCategory
        }
    }
}

/// Fixed sample data for snapshots. Every image URL is nil, so
/// CocktailImageView shows its static fallback and Kingfisher never hits
/// the network.
enum SnapshotFixtures {

    static func cocktail(
        id: String = "11007",
        name: String = "Margarita",
        category: String = "Ordinary Drink",
        ingredients: [CocktailIngredient] = margaritaIngredients,
        price: Money = Money(cents: 1250),
        stockCount: Int32 = 8
    ) -> Cocktail {
        Cocktail(
            id: id,
            name: name,
            alternateName: nil,
            tags: nil,
            category: category,
            iba: nil,
            alcoholic: "Alcoholic",
            glass: "Cocktail glass",
            instructions: "Rub the rim of the glass with the lime slice to make the salt stick to it. "
                + "Shake the other ingredients with ice, then carefully pour into the glass.",
            imageUrl: nil,
            ingredients: ingredients,
            imageSource: nil,
            imageAttribution: nil,
            creativeCommonsConfirmed: nil,
            dateModified: nil,
            price: price,
            inStock: stockCount > 0,
            stockCount: stockCount,
            rating: 4.5,
            popularity: 0,
            dateAdded: 0,
            hasFullDetails: true
        )
    }

    static let margaritaIngredients = [
        CocktailIngredient(name: "Tequila", measure: "1 1/2 oz"),
        CocktailIngredient(name: "Triple sec", measure: "1/2 oz"),
        CocktailIngredient(name: "Lime juice", measure: "1 oz"),
        CocktailIngredient(name: "Salt", measure: "pinch")
    ]

    static let margarita = cocktail()
    static let outOfStock = cocktail(id: "11008", name: "Manhattan", stockCount: 0)
    static let mojito = cocktail(
        id: "11009",
        name: "Mojito",
        category: "Cocktail",
        ingredients: [
            CocktailIngredient(name: "Light rum", measure: "2-3 oz"),
            CocktailIngredient(name: "Lime", measure: "Juice of 1")
        ],
        price: Money(cents: 900)
    )

    static let cartItem = CocktailCartItem(cocktail: margarita, quantity: 2)

    static let order = Order(
        id: "ORD-42052",
        date: "2026-07-02",
        items: [
            OrderItem(name: "Margarita", quantity: 2, price: Money(cents: 1250), cocktailId: "11007"),
            OrderItem(name: "Mojito", quantity: 1, price: Money(cents: 900), cocktailId: "11009")
        ],
        total: Money(cents: 3999),
        status: .processing
    )

    static let categories = ["Cocktail", "Ordinary Drink", "Shot", "Punch / Party Drink"]

    static let activeFilters = SearchFilters(
        query: "",
        category: "Cocktail",
        ingredient: "Tequila",
        alcoholic: KotlinBoolean(bool: true)
    )

    static let reviews = [
        Review(
            id: "snapshot-1",
            cocktailId: margarita.id,
            userName: "Alex",
            rating: 4.5,
            comment: "Perfectly balanced — the fresh lime makes all the difference.",
            date: "2026-06-30"
        ),
        Review(
            id: "snapshot-2",
            cocktailId: margarita.id,
            userName: "",
            rating: 3,
            comment: "",
            date: "2026-07-02"
        )
    ]
}

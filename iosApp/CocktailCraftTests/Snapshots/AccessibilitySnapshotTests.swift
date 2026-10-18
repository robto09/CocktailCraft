import XCTest
import SwiftUI
@testable import CocktailCraft
import shared

/// Key components under the in-app appearance settings: High Contrast
/// (dark), the "Extra Large" font size, the largest system accessibility
/// text size (AX5), and a non-default accent color.
final class AccessibilitySnapshotTests: SnapshotTestCase {

    func testCocktailCard() {
        assertSnapshots(of: VStack(spacing: 12) {
            CocktailCard(cocktail: SnapshotFixtures.margarita, isFavorite: true, onFavoriteToggle: {}, onAddToCart: {})
            CocktailCard(cocktail: SnapshotFixtures.outOfStock, onFavoriteToggle: {}, onAddToCart: {})
        }, in: SnapshotAppearance.accessibility)
    }

    func testCartItemAndCheckout() {
        assertSnapshots(of: VStack(spacing: 12) {
            CartItemCard(
                item: SnapshotFixtures.cartItem,
                onIncrementQuantity: {},
                onDecrementQuantity: {},
                onRemoveFromCart: {},
                onToggleFavorite: nil
            )
            CheckoutButton(action: {})
        }, in: SnapshotAppearance.accessibility)
    }

    func testCartSummaryCard() {
        assertSnapshots(
            of: CartSummaryCard(subtotal: Money(cents: 3400), deliveryFee: Money(cents: 599), total: Money(cents: 3999)),
            in: SnapshotAppearance.accessibility
        )
    }

    func testCategoryChipRow() {
        assertSnapshots(of: CategoryChipRow(
            categories: SnapshotFixtures.categories,
            selectedCategory: "Cocktail",
            onSelectAll: {},
            onSelect: { _ in }
        ), in: SnapshotAppearance.accessibility)
    }

    func testSettingsRows() {
        assertSnapshots(of: VStack(spacing: 0) {
            SettingsRow(icon: "clock", title: "Order History", action: {})
            ThemeToggleRow(
                title: "Follow System Theme",
                subtitle: "On",
                icon: "circle.lefthalf.filled",
                isChecked: true,
                onToggle: { _ in }
            )
            ThemeToggleRow(
                title: "Dark Mode",
                subtitle: "Controlled by system",
                icon: "moon",
                isChecked: false,
                onToggle: { _ in },
                enabled: false
            )
        }, in: SnapshotAppearance.accessibility)
    }
}

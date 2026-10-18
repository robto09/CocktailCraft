import XCTest
import SwiftUI
@testable import CocktailCraft
import shared

/// Reusable components from CocktailCraft/Components, in light and dark.
final class ComponentSnapshotTests: SnapshotTestCase {

    // MARK: - Cards

    func testCocktailCard() {
        assertSnapshots(of: VStack(spacing: 12) {
            CocktailCard(cocktail: SnapshotFixtures.margarita, isFavorite: true, onFavoriteToggle: {}, onAddToCart: {})
            CocktailCard(cocktail: SnapshotFixtures.outOfStock, onFavoriteToggle: {}, onAddToCart: {})
        })
    }

    func testCartItemCard() {
        assertSnapshots(of: CartItemCard(
            item: SnapshotFixtures.cartItem,
            onIncrementQuantity: {},
            onDecrementQuantity: {},
            onRemoveFromCart: {},
            onToggleFavorite: {}
        ))
    }

    func testCartSummaryCard() {
        assertSnapshots(of: CartSummaryCard(subtotal: 34, deliveryFee: 5.99, total: 39.99))
    }

    func testRelatedCocktailCard() {
        assertSnapshots(of: HStack(spacing: 12) {
            RelatedCocktailCard(cocktail: SnapshotFixtures.margarita)
            RelatedCocktailCard(cocktail: SnapshotFixtures.mojito)
        })
    }

    // MARK: - Buttons

    func testPrimaryButton() {
        assertSnapshots(of: VStack(spacing: 12) {
            PrimaryButton(title: "Add to Cart", icon: "cart.fill", action: {})
            PrimaryButton(title: "Add to Cart", isDisabled: true, action: {})
            PrimaryButton(title: "Clear Cache", icon: "trash", isDestructive: true, action: {})
        })
    }

    func testCheckoutButton() {
        assertSnapshots(of: VStack(spacing: 12) {
            CheckoutButton(action: {})
            CheckoutButton(isEnabled: false, action: {})
        })
    }

    // MARK: - Chips and search

    func testCategoryChipRow() {
        assertSnapshots(of: VStack(spacing: 12) {
            CategoryChipRow(
                categories: SnapshotFixtures.categories,
                selectedCategory: "Cocktail",
                onSelectAll: {},
                onSelect: { _ in }
            )
            CategoryChipRow(
                categories: SnapshotFixtures.categories,
                selectedCategory: nil,
                onSelectAll: {},
                onSelect: { _ in }
            )
        })
    }

    func testHomeSearchBar() {
        assertSnapshots(of: VStack(spacing: 12) {
            HomeSearchBar(searchText: .constant(""), onSubmit: {}, onClear: {}, onAdvancedSearch: {})
            HomeSearchBar(searchText: .constant("Margarita"), onSubmit: {}, onClear: {}, onAdvancedSearch: {})
        })
    }

    func testActiveFiltersCard() {
        assertSnapshots(of: ActiveFiltersCard(
            filters: SnapshotFixtures.activeFilters,
            onApply: { _ in },
            onClearAll: {}
        ))
    }

    // MARK: - Empty, error and loading states

    func testEmptyStateView() {
        assertSnapshots(of: EmptyStateView(
            icon: "cart",
            title: "Your cart is empty",
            message: "Add some cocktails to get started.",
            actionTitle: "Start Shopping",
            action: {}
        ), height: 600)
    }

    func testSearchEmptyStateForQuery() {
        assertSnapshots(of: searchEmptyState(query: "Zombie"), height: 600)
    }

    func testSearchEmptyStateForCategory() {
        assertSnapshots(of: searchEmptyState(category: "Punch / Party Drink"), height: 600)
    }

    func testSearchEmptyStateForFilters() {
        assertSnapshots(of: searchEmptyState(hasActiveFilters: true), height: 600)
    }

    func testErrorViewNetwork() {
        assertSnapshots(of: ErrorView(
            error: ErrorHandler.UserFriendlyError(
                title: RawStringDesc(string: "No Internet Connection"),
                message: RawStringDesc(string: "Check your connection and try again."),
                category: .network,
                recoveryAction: nil,
                originalException: nil,
                errorCode: .unknown
            ),
            onRetry: {}
        ), height: 500)
    }

    func testErrorViewServer() {
        assertSnapshots(of: ErrorView(
            error: ErrorHandler.UserFriendlyError(
                title: RawStringDesc(string: "Server Error"),
                message: RawStringDesc(string: "Something went wrong on our end."),
                category: .server,
                recoveryAction: nil,
                originalException: nil,
                errorCode: .unknown
            ),
            onRetry: nil
        ), height: 500)
    }

    func testCocktailListSkeleton() {
        assertSnapshots(of: CocktailListSkeleton(rowCount: 3), height: 600)
    }

    func testCocktailDetailSkeleton() {
        assertSnapshots(of: CocktailDetailSkeleton(), height: 800)
    }

    func testOrderListSkeleton() {
        assertSnapshots(of: OrderListSkeleton(cardCount: 2), height: 600)
    }

    func testToast() {
        assertSnapshots(of: VStack(spacing: 12) {
            Toast(message: "Added to cart", type: .success)
            Toast(message: "Couldn't place order", type: .error)
            Toast(message: "Showing cached cocktails", type: .info)
        })
    }

    func testToastWithUndo() {
        assertSnapshots(of: Toast(
            message: "Removed Margarita from cart",
            type: .success,
            actionLabel: "Undo",
            onAction: {}
        ))
    }

    // MARK: - Orders

    func testOrderDetailContent() {
        assertSnapshots(of: OrderDetailContent(
            order: SnapshotFixtures.order,
            subtotal: 34,
            deliveryFee: 5.99,
            deliveryEstimate: "15-25 minutes",
            canCancel: true
        ))
    }

    // MARK: - Profile and reviews

    func testSettingsRows() {
        assertSnapshots(of: VStack(spacing: 0) {
            SettingsRow(icon: "person", title: "Edit Profile", action: {})
            SettingsRow(icon: "lock", title: "Change Password", action: {})
            ThemeToggleRow(title: "Dark Mode", subtitle: "On", icon: "moon.fill", isChecked: true, onToggle: { _ in })
            ThemeToggleRow(
                title: "Dark Mode",
                subtitle: "Controlled by system",
                icon: "moon",
                isChecked: false,
                onToggle: { _ in },
                enabled: false
            )
            SettingsRow(icon: "rectangle.portrait.and.arrow.right", title: "Logout", action: {}, textColor: AppColors.error)
        })
    }

    func testPasswordStrengthMeter() {
        assertSnapshots(of: VStack(spacing: 12) {
            ForEach(0...5, id: \.self) { PasswordStrengthMeter(strength: $0) }
        })
    }

    func testReviewRows() {
        assertSnapshots(of: VStack(alignment: .leading, spacing: 16) {
            ForEach(SnapshotFixtures.reviews, id: \.id) { ReviewRow(review: $0) }
            StarRatingView(rating: 3.5, starSize: 20)
        })
    }

    private func searchEmptyState(
        query: String = "",
        category: String? = nil,
        hasActiveFilters: Bool = false
    ) -> SearchEmptyStateView {
        SearchEmptyStateView(
            searchQuery: query,
            selectedCategory: category,
            hasActiveFilters: hasActiveFilters,
            onClearSearch: {},
            onClearCategory: {},
            onClearFilters: {},
            onRefresh: {}
        )
    }
}

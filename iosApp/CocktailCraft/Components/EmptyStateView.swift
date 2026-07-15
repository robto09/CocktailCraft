import SwiftUI

/// Unified empty state (icon / title / message, plus an optional PrimaryButton
/// action) shared by Home, Cart, Favorites, and Orders — mirroring Android's
/// single empty-state component.
struct EmptyStateView: View {
    let icon: String
    let title: String
    let message: String
    var actionTitle: String? = nil
    var action: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: icon)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 80, height: 80)
                .foregroundColor(.gray)

            Text(title)
                .font(.title2)
                .fontWeight(.semibold)
                // Stable hook for UI tests, so they don't key on display copy
                .accessibilityIdentifier("emptyState.title")

            Text(message)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)

            if let actionTitle, let action {
                PrimaryButton(title: actionTitle, action: action)
                    .padding(.horizontal, 40)
                    .padding(.top, 8)
                    .accessibilityIdentifier("emptyState.actionButton")
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// Context-aware no-results state for the Home search, mirroring Android's
/// EmptySearchResultsMessage: icon, title, subtitle, and actions vary with
/// the active search query, category, and advanced-search filters.
struct SearchEmptyStateView: View {
    let searchQuery: String
    let selectedCategory: String?
    let hasActiveFilters: Bool
    let onClearSearch: () -> Void
    let onClearCategory: () -> Void
    let onClearFilters: () -> Void
    let onRefresh: () -> Void

    @State private var appeared = false
    @Environment(\.isDarkMode) private var isDarkMode

    private var primaryColor: Color { AppColors.primary(isDarkMode: isDarkMode) }
    private var hasQuery: Bool { !searchQuery.isEmpty }

    var body: some View {
        VStack(spacing: 0) {
            // Layered brand-color icon matching Android's EmptyStateIcon
            // (a large low-opacity copy behind a small full-opacity one)
            ZStack {
                Image(systemName: icon)
                    .font(.system(size: 64))
                    .foregroundColor(primaryColor.opacity(0.2))
                Image(systemName: icon)
                    .font(.system(size: 32))
                    .foregroundColor(primaryColor)
            }
            .frame(width: 80, height: 80)
            .accessibilityLabel("No results")

            Spacer().frame(height: 24)

            Text(title)
                .font(.title3.weight(.bold))
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .multilineTextAlignment(.center)
                // Stable hook for UI tests, so they don't key on display copy
                .accessibilityIdentifier("emptyState.title")

            Spacer().frame(height: 12)

            Text(subtitle)
                .font(.callout)
                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                .multilineTextAlignment(.center)

            Spacer().frame(height: 32)

            VStack(spacing: 8) {
                actions
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity)
        .opacity(appeared ? 1 : 0)
        .offset(y: appeared ? 0 : 40)
        .onAppear {
            withAnimation(.spring(response: 0.55, dampingFraction: 0.7)) { appeared = true }
        }
    }

    // MARK: - Context-dependent content (same branching as Android)

    private var icon: String {
        if hasActiveFilters { return "line.3.horizontal.decrease" }
        if selectedCategory != nil { return "square.grid.2x2" }
        return "magnifyingglass"
    }

    private var title: String {
        if hasActiveFilters && hasQuery {
            return String(localized: "No cocktails match \"\(searchQuery)\" with your active filters")
        }
        if hasActiveFilters {
            return String(localized: "No cocktails match your active filters")
        }
        if let selectedCategory, hasQuery {
            return String(localized: "No cocktails found matching \"\(searchQuery)\" in \"\(selectedCategory)\"")
        }
        if let selectedCategory {
            return String(localized: "No cocktails found in category \"\(selectedCategory)\"")
        }
        if hasQuery {
            return String(localized: "No cocktails found matching \"\(searchQuery)\"")
        }
        return String(localized: "No cocktails found")
    }

    private var subtitle: String {
        if hasActiveFilters {
            return String(localized: "Try adjusting or clearing your filters")
        }
        if selectedCategory != nil && hasQuery {
            return String(localized: "Try a different search term or category")
        }
        if selectedCategory != nil {
            return String(localized: "This category doesn't have any cocktails yet")
        }
        if hasQuery {
            return String(localized: "Try a different search term or check your spelling")
        }
        return String(localized: "We couldn't find any cocktails")
    }

    @ViewBuilder
    private var actions: some View {
        if hasActiveFilters && hasQuery {
            filledButton(String(localized: "Clear Search"), action: onClearSearch)
            outlinedButton(String(localized: "Clear Filters"), action: onClearFilters)
        } else if hasActiveFilters {
            filledButton(String(localized: "Clear Filters"), action: onClearFilters)
        } else if selectedCategory != nil && hasQuery {
            filledButton(String(localized: "Clear Search"), action: onClearSearch)
            outlinedButton(String(localized: "Clear Category Filter"), action: onClearCategory)
        } else if selectedCategory != nil {
            filledButton(String(localized: "Browse All Cocktails"), action: onClearCategory)
        } else if hasQuery {
            filledButton(String(localized: "Clear Search"), action: onClearSearch)
        } else {
            filledButton(String(localized: "Refresh"), action: onRefresh)
        }
    }

    private func filledButton(_ title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline.weight(.medium))
                .foregroundColor(.white)
                .padding(.horizontal, 24)
                .padding(.vertical, 10)
                .background(Capsule().fill(primaryColor))
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier("emptyState.actionButton")
    }

    private func outlinedButton(_ title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline.weight(.medium))
                .foregroundColor(primaryColor)
                .padding(.horizontal, 24)
                .padding(.vertical, 10)
                .background(Capsule().stroke(primaryColor, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

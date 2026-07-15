import SwiftUI
import shared

/// Home search field with clear button and the advanced-search toggle.
struct HomeSearchBar: View {
    @Binding var searchText: String
    let onSubmit: () -> Void
    let onClear: () -> Void
    let onAdvancedSearch: () -> Void
    @Environment(\.isDarkMode) private var isDarkMode

    private var primaryColor: Color { AppColors.primary(isDarkMode: isDarkMode) }
    private var surfaceColor: Color { AppColors.surface(isDarkMode: isDarkMode) }
    private var textSecondary: Color { AppColors.textSecondary(isDarkMode: isDarkMode) }

    var body: some View {
        HStack(spacing: 8) {
            // Main Search Bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(textSecondary)
                    .accessibilityHidden(true)

                TextField("Search cocktails...", text: $searchText)
                    .textFieldStyle(.plain)
                    .foregroundColor(.primary)
                    .onSubmit(onSubmit)
                    .accessibilityIdentifier("home.searchField")

                if !searchText.isEmpty {
                    Button(action: onClear) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(textSecondary)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel("Clear search")
                    .accessibilityIdentifier("home.clearSearchButton")
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(surfaceColor)
            .cornerRadius(10)
            .shadow(color: Color.black.opacity(0.05), radius: 1, x: 0, y: 1)

            // Advanced Search Button
            Button(action: onAdvancedSearch) {
                Image(systemName: "slider.horizontal.3")
                    .font(.headline)
                    .foregroundColor(primaryColor)
                    .padding(8)
                    .background(surfaceColor)
                    .cornerRadius(8)
                    .shadow(color: Color.black.opacity(0.15), radius: 2, x: 0, y: 1)
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Advanced search")
            .accessibilityIdentifier("home.advancedSearchButton")
        }
    }
}

/// Card of active advanced-search filters, mirroring Android's
/// SearchFilterChips: a header row (filter icon, "Active Filters", Clear All)
/// above wrapping chips. Tapping a chip removes just that filter by applying
/// the reduced `SearchFilters`; Clear All resets everything.
struct ActiveFiltersCard: View {
    let filters: SearchFilters
    let onApply: (SearchFilters) -> Void
    let onClearAll: () -> Void
    @Environment(\.isDarkMode) private var isDarkMode

    private var primaryColor: Color { AppColors.primary(isDarkMode: isDarkMode) }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 4) {
                Image(systemName: "line.3.horizontal.decrease")
                    .font(.caption.weight(.semibold))
                    .foregroundColor(primaryColor)
                    .accessibilityHidden(true)

                Text("Active Filters")
                    .font(.caption)
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))

                Spacer()

                Button(action: onClearAll) {
                    Text("Clear All")
                        .font(.caption)
                        .foregroundColor(primaryColor)
                }
                .buttonStyle(.plain)
            }

            FlowLayout(spacing: 8) {
                if let category = filters.category {
                    chip("Category: \(category)") {
                        onApply(SearchFilters(
                            query: filters.query,
                            category: nil,
                            ingredient: filters.ingredient,
                            alcoholic: filters.alcoholic
                        ))
                    }
                }

                if let ingredient = filters.ingredient {
                    chip("Ingredient: \(ingredient)") {
                        onApply(SearchFilters(
                            query: filters.query,
                            category: filters.category,
                            ingredient: nil,
                            alcoholic: filters.alcoholic
                        ))
                    }
                }

                if let alcoholic = filters.alcoholic?.boolValue {
                    chip(alcoholic ? "Alcoholic" : "Non-Alcoholic") {
                        onApply(SearchFilters(
                            query: filters.query,
                            category: filters.category,
                            ingredient: filters.ingredient,
                            alcoholic: nil
                        ))
                    }
                }
            }
        }
        .padding(8)
        .background(AppColors.surface(isDarkMode: isDarkMode))
        .cornerRadius(8)
    }

    /// A removable filter chip: whole chip tappable, trailing X as the
    /// visual affordance (matching Android's InputChip).
    private func chip(_ label: String, onClear: @escaping () -> Void) -> some View {
        Button(action: onClear) {
            HStack(spacing: 4) {
                Text(label)
                    .font(.caption)
                    .lineLimit(1)
                Image(systemName: "xmark")
                    .font(.caption2.weight(.semibold))
            }
            .foregroundColor(primaryColor)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(Capsule().fill(primaryColor.opacity(0.1)))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
        .accessibilityHint("Removes this filter")
    }
}

/// Minimal leading-aligned wrapping layout for the filter chips
/// (the SwiftUI counterpart of Compose's FlowRow).
struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var x: CGFloat = 0, y: CGFloat = 0, rowHeight: CGFloat = 0, widest: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x > 0 && x + size.width > maxWidth {
                x = 0
                y += rowHeight + spacing
                rowHeight = 0
            }
            x += size.width + spacing
            widest = max(widest, x - spacing)
            rowHeight = max(rowHeight, size.height)
        }

        return CGSize(width: proposal.width ?? widest, height: y + rowHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX, y = bounds.minY, rowHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x > bounds.minX && x + size.width > bounds.maxX {
                x = bounds.minX
                y += rowHeight + spacing
                rowHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), proposal: .unspecified)
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}

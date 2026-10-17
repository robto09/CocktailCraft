import SwiftUI
import shared

/// Home search field with clear button and the advanced-search toggle.
struct HomeSearchBar: View {
    @Binding var searchText: String
    let onSubmit: () -> Void
    let onClear: () -> Void
    let onAdvancedSearch: () -> Void
    /// Filters applied or the filter sheet open: the button turns solid
    /// orange, as on Android
    var isFilterActive: Bool = false
    @Environment(\.isDarkMode) private var isDarkMode
    @FocusState private var isFieldFocused: Bool

    private var primaryColor: Color { AppColors.primary(isDarkMode: isDarkMode) }
    private var textSecondary: Color { AppColors.textSecondary(isDarkMode: isDarkMode) }

    var body: some View {
        HStack(spacing: 8) {
            // Main Search Bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(isFieldFocused ? primaryColor : textSecondary)
                    .accessibilityHidden(true)

                TextField(L(\.search_cocktails_placeholder), text: $searchText)
                    .textFieldStyle(.plain)
                    .foregroundColor(.primary)
                    .tint(primaryColor)
                    .focused($isFieldFocused)
                    .onSubmit(onSubmit)
                    .accessibilityIdentifier("home.searchField")

                if !searchText.isEmpty {
                    Button(action: onClear) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(textSecondary)
                            // ~44 pt tap area around the small glyph, without
                            // changing the field's layout
                            .contentShape(Rectangle().inset(by: -12))
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(L(\.search_clear_search))
                    .accessibilityIdentifier("home.clearSearchButton")
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            // Same height and look as the 48pt filter button beside it, and as
            // Android's field: white, thin light border, orange while typing
            .frame(minHeight: 48)
            .background(
                RoundedRectangle(cornerRadius: AppTheme.CornerRadius.sm)
                    .fill(AppColors.surfaceRaised(isDarkMode: isDarkMode))
            )
            .overlay(
                RoundedRectangle(cornerRadius: AppTheme.CornerRadius.sm)
                    .stroke(isFieldFocused ? primaryColor : AppColors.neutralSubtle(isDarkMode: isDarkMode), lineWidth: 1)
            )

            // Advanced Search Button, as Android's: a 48pt square with the
            // funnel; white with an orange funnel at rest, solid orange with
            // a white funnel while filters are active
            Button(action: onAdvancedSearch) {
                FunnelIcon()
                    .fill(isFilterActive ? AppColors.onPrimary(isDarkMode: isDarkMode) : primaryColor)
                    .frame(width: 24, height: 24)
                    .frame(width: 48, height: 48)
                    .background(
                        RoundedRectangle(cornerRadius: AppTheme.CornerRadius.sm)
                            .fill(isFilterActive ? primaryColor : AppColors.surfaceRaised(isDarkMode: isDarkMode))
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: AppTheme.CornerRadius.sm)
                            .stroke(isFilterActive ? Color.clear : AppColors.neutralSubtle(isDarkMode: isDarkMode), lineWidth: 1)
                    )
            }
            .buttonStyle(.plain)
            .accessibilityLabel(L(\.search_advanced_search_ios))
            .accessibilityValue(isFilterActive ? L(\.filters_active_a11y) : "")
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
                FunnelIcon()
                    .fill(primaryColor)
                    .frame(width: 16, height: 16)
                    .accessibilityHidden(true)

                Text(L(\.filter_active_filters))
                    .font(.caption)
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .accessibilityAddTraits(.isHeader)

                Spacer()

                Button(action: onClearAll) {
                    Text(L(\.filter_clear_all))
                        .font(.caption)
                        .foregroundColor(primaryColor)
                        // 44 pt tall tap area without growing the header row
                        .contentShape(Rectangle().inset(by: -14))
                }
                .buttonStyle(.plain)
            }

            FlowLayout(spacing: 8) {
                if let category = filters.category {
                    chip(L(\.category_label, category)) {
                        onApply(SearchFilters(
                            query: filters.query,
                            category: nil,
                            ingredient: filters.ingredient,
                            alcoholic: filters.alcoholic
                        ))
                    }
                }

                if let ingredient = filters.ingredient {
                    chip(L(\.filter_ingredient_label, ingredient)) {
                        onApply(SearchFilters(
                            query: filters.query,
                            category: filters.category,
                            ingredient: nil,
                            alcoholic: filters.alcoholic
                        ))
                    }
                }

                if let alcoholic = filters.alcoholic?.boolValue {
                    chip(alcoholic ? L(\.alcoholic) : L(\.non_alcoholic)) {
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
        .accessibilityHint(L(\.filter_remove_hint))
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

/// The filter funnel, drawn from Material's FilterAlt path (24x24 viewport) so
/// both platforms show the same glyph; SF Symbols has no funnel.
struct FunnelIcon: Shape {
    func path(in rect: CGRect) -> Path {
        let scale = min(rect.width, rect.height) / 24
        var path = Path()
        path.move(to: CGPoint(x: 4.25, y: 5.61))
        path.addCurve(to: CGPoint(x: 10, y: 13), control1: CGPoint(x: 6.27, y: 8.2), control2: CGPoint(x: 10, y: 13))
        path.addLine(to: CGPoint(x: 10, y: 19))
        path.addCurve(to: CGPoint(x: 11, y: 20), control1: CGPoint(x: 10, y: 19.55), control2: CGPoint(x: 10.45, y: 20))
        path.addLine(to: CGPoint(x: 13, y: 20))
        path.addCurve(to: CGPoint(x: 14, y: 19), control1: CGPoint(x: 13.55, y: 20), control2: CGPoint(x: 14, y: 19.55))
        path.addLine(to: CGPoint(x: 14, y: 13))
        path.addCurve(to: CGPoint(x: 19.74, y: 5.61), control1: CGPoint(x: 14, y: 13), control2: CGPoint(x: 17.72, y: 8.2))
        path.addCurve(to: CGPoint(x: 18.95, y: 4), control1: CGPoint(x: 20.25, y: 4.95), control2: CGPoint(x: 19.78, y: 4))
        path.addLine(to: CGPoint(x: 5.04, y: 4))
        path.addCurve(to: CGPoint(x: 4.25, y: 5.61), control1: CGPoint(x: 4.21, y: 4), control2: CGPoint(x: 3.74, y: 4.95))
        path.closeSubpath()
        return path
            .applying(CGAffineTransform(scaleX: scale, y: scale))
            .offsetBy(dx: rect.minX + (rect.width - 24 * scale) / 2, dy: rect.minY + (rect.height - 24 * scale) / 2)
    }
}

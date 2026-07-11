import SwiftUI

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

/// Horizontal strip of active advanced-search filter labels with a
/// Clear All action.
struct ActiveFiltersRow: View {
    let labels: [String]
    let onClearAll: () -> Void
    @Environment(\.isDarkMode) private var isDarkMode

    private var primaryColor: Color { AppColors.primary(isDarkMode: isDarkMode) }

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(labels, id: \.self) { label in
                    HStack(spacing: 4) {
                        Text(label)
                            .font(.caption)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(primaryColor.opacity(0.1))
                    .foregroundColor(primaryColor)
                    .cornerRadius(8)
                }

                Button(action: onClearAll) {
                    HStack(spacing: 4) {
                        Text("Clear All")
                            .font(.caption)
                        Image(systemName: "xmark")
                            .font(.caption2)
                    }
                }
                .buttonStyle(.plain)
                .padding(.horizontal, 8)
                .padding(.vertical, 2)
                .background(primaryColor.opacity(0.1))
                .foregroundColor(primaryColor)
                .cornerRadius(8)
            }
            .padding(.horizontal, 16)
        }
    }
}

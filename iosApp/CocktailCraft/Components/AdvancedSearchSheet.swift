import SwiftUI
import shared

/// Tri-state selection for the alcoholic filter chips, bridging to the
/// shared `SearchFilters.alcoholic` (`KotlinBoolean?`, where nil means "any").
private enum AlcoholicFilter: Hashable, CaseIterable {
    case any
    case alcoholic
    case nonAlcoholic

    var label: String {
        switch self {
        case .any: return L(\.filter_any)
        case .alcoholic: return L(\.alcoholic)
        case .nonAlcoholic: return L(\.non_alcoholic)
        }
    }

    var kotlinValue: KotlinBoolean? {
        switch self {
        case .any: return nil
        case .alcoholic: return KotlinBoolean(bool: true)
        case .nonAlcoholic: return KotlinBoolean(bool: false)
        }
    }

    static func from(_ value: KotlinBoolean?) -> AlcoholicFilter {
        guard let value = value?.boolValue else { return .any }
        return value ? .alcoholic : .nonAlcoholic
    }
}

/// Advanced-search filter sheet driven by the shared HomeViewModel, mirroring
/// Android's AdvancedSearchBottomSheet: a surface-colored bottom sheet with a
/// Clear / title / Apply header row and three collapsible filter sections —
/// Category (dropdown), Ingredient (searchable picker sheet), Alcoholic
/// (chips). Apply forwards a `SearchFilters` to the shared `applyFilters`,
/// Clear resets.
struct AdvancedSearchSheet: View {
    let viewModel: HomeViewModelSKIE
    @Environment(\.dismiss) private var dismiss
    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.appearance) private var appearance

    @State private var selectedCategory: String?
    @State private var selectedIngredient: String?
    @State private var alcoholic: AlcoholicFilter
    @State private var showingIngredientPicker = false

    private var primaryColor: Color { AppColors.primary(appearance) }
    private var surfaceColor: Color { AppColors.surface(isDarkMode: isDarkMode) }
    private var textPrimary: Color { AppColors.textPrimary(isDarkMode: isDarkMode) }
    private var textSecondary: Color { AppColors.textSecondary(appearance) }

    init(viewModel: HomeViewModelSKIE) {
        self.viewModel = viewModel
        let filters = viewModel.state.searchFilters
        _selectedCategory = State(initialValue: filters.category)
        _selectedIngredient = State(initialValue: filters.ingredient)
        _alcoholic = State(initialValue: AlcoholicFilter.from(filters.alcoholic))
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                header

                Spacer().frame(height: 8)

                FilterSection(title: L(\.filter_category)) {
                    categorySelector
                }

                FilterSection(title: L(\.filter_ingredient)) {
                    ingredientSelector
                }

                FilterSection(title: L(\.alcoholic)) {
                    alcoholicChips
                }

                Spacer().frame(height: 24)
            }
            .padding(.horizontal, 16)
        }
        .background(surfaceColor)
        .presentationBackground(surfaceColor)
        .presentationDragIndicator(.visible)
        .sheet(isPresented: $showingIngredientPicker) {
            IngredientPickerSheet(
                ingredients: viewModel.state.filterIngredients,
                selectedIngredient: selectedIngredient,
                onIngredientSelected: { ingredient in
                    selectedIngredient = ingredient
                }
            )
        }
        .task {
            // Populate the option lists from the shared API-backed state.
            await viewModel.loadFilterOptions()
        }
    }

    // MARK: - Header (Clear | title | Apply, matching the Android sheet)

    private var header: some View {
        HStack {
            Button(L(\.filter_clear)) {
                viewModel.clearSearchFilters()
                dismiss()
            }
            .font(.subheadline.weight(.medium))
            .foregroundColor(primaryColor)

            Spacer()

            Text(L(\.search_advanced_search))
                .font(.headline.weight(.bold))
                .foregroundColor(textPrimary)

            Spacer()

            Button {
                let filters = SearchFilters(
                    query: viewModel.state.searchFilters.query,
                    category: selectedCategory,
                    ingredient: selectedIngredient,
                    alcoholic: alcoholic.kotlinValue
                )
                // Close immediately; the rate-limited search runs in
                // the background and the list updates via shared state.
                dismiss()
                Task {
                    await viewModel.applyFilters(filters)
                }
            } label: {
                Text(L(\.filter_apply))
                    .font(.subheadline.weight(.medium))
                    .foregroundColor(AppColors.onPrimary(isDarkMode: isDarkMode))
                    .padding(.horizontal, 20)
                    .padding(.vertical, 8)
                    .background(Capsule().fill(primaryColor))
            }
            .buttonStyle(.plain)
        }
        .padding(.top, 16)
    }

    // MARK: - Selectors

    private var categorySelector: some View {
        Menu {
            // An embedded Picker (rather than plain Buttons) so the open menu
            // checkmarks the current selection and reads as a picker to
            // VoiceOver, while keeping the custom outlined label.
            Picker(L(\.filter_category), selection: $selectedCategory) {
                Text(L(\.filter_all_categories)).tag(String?.none)
                ForEach(viewModel.state.filterCategories, id: \.self) { category in
                    Text(category).tag(String?.some(category))
                }
            }
        } label: {
            SelectorRow(selection: selectedCategory, placeholder: L(\.filter_select_category))
        }
        .buttonStyle(.plain)
    }

    private var ingredientSelector: some View {
        Button {
            showingIngredientPicker = true
        } label: {
            SelectorRow(selection: selectedIngredient, placeholder: L(\.filter_select_an_ingredient))
        }
        .buttonStyle(.plain)
    }

    private var alcoholicChips: some View {
        // Chip styling mirrors the Android FilterChip: selected = brand-color
        // fill with white text, unselected = brand-color text on the surface.
        HStack(spacing: 8) {
            ForEach(AlcoholicFilter.allCases, id: \.self) { option in
                Button(action: { alcoholic = option }) {
                    Text(option.label)
                        .font(.subheadline.weight(alcoholic == option ? .bold : .medium))
                        .foregroundColor(alcoholic == option ? AppColors.onPrimary(isDarkMode: isDarkMode) : primaryColor)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 6)
                        .background(alcoholic == option ? primaryColor : surfaceColor)
                        .cornerRadius(16)
                }
                .buttonStyle(.plain)
                .accessibilityAddTraits(alcoholic == option ? .isSelected : [])
            }
            Spacer()
        }
    }
}

// MARK: - Filter section (collapsible title + divider, matching Android)

private struct FilterSection<Content: View>: View {
    let title: String
    @ViewBuilder let content: Content

    @State private var expanded = true
    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.appearance) private var appearance

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button {
                withAnimation(AppTheme.Animation.quick) { expanded.toggle() }
            } label: {
                HStack {
                    Text(title)
                        .font(.subheadline.weight(.bold))
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                    Spacer()

                    Image(systemName: expanded ? "chevron.up" : "chevron.down")
                        .font(.footnote.weight(.semibold))
                        .foregroundColor(AppColors.textSecondary(appearance))
                }
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(title)
            .accessibilityValue(expanded ? L(\.filter_section_expanded_a11y) : L(\.filter_section_collapsed_a11y))
            .accessibilityHint(expanded ? L(\.filter_collapse) : L(\.filter_expand))
            .accessibilityAddTraits(.isHeader)

            if expanded {
                content
                    .padding(.top, 8)
                    .padding(.bottom, 4)
                    .transition(.opacity)
            }

            Divider()
                .padding(.top, expanded ? 8 : 16)
        }
        .padding(.vertical, 8)
    }
}

// MARK: - Dropdown-styled selector row (outlined box, matching Android)

private struct SelectorRow: View {
    let selection: String?
    let placeholder: String

    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.appearance) private var appearance

    var body: some View {
        HStack {
            if let selection {
                Text(selection)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
            } else {
                Text(placeholder)
                    .foregroundColor(AppColors.textSecondary(appearance))
            }

            Spacer()

            Image(systemName: "chevron.down")
                .font(.footnote.weight(.semibold))
                .foregroundColor(AppColors.textSecondary(appearance))
        }
        .font(.subheadline)
        .padding(16)
        .contentShape(Rectangle())
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(AppColors.neutralSubtle(isDarkMode: isDarkMode), lineWidth: 1)
        )
    }
}

// MARK: - Ingredient picker (native searchable list sheet)

/// Single-select ingredient picker following the iOS convention for choosing
/// one item from a long list: a sheet with a searchable List and a checkmark
/// on the selected row. Selecting a row (or "Any ingredient") applies the
/// choice immediately and dismisses; Cancel closes without changes.
private struct IngredientPickerSheet: View {
    let ingredients: [String]
    let selectedIngredient: String?
    let onIngredientSelected: (String?) -> Void

    @State private var searchQuery = ""
    @Environment(\.dismiss) private var dismiss
    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.appearance) private var appearance

    private var filteredIngredients: [String] {
        guard !searchQuery.isEmpty else { return ingredients }
        return ingredients.filter { $0.localizedCaseInsensitiveContains(searchQuery) }
    }

    var body: some View {
        NavigationStack {
            List {
                ingredientRow(label: nil)
                ForEach(filteredIngredients, id: \.self) { ingredient in
                    ingredientRow(label: ingredient)
                }
            }
            .searchable(
                text: $searchQuery,
                placement: .navigationBarDrawer(displayMode: .always),
                prompt: L(\.search_ingredients_placeholder)
            )
            .brandedNavigationBar(title: L(\.filter_select_ingredient_title))
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(L(\.cancel)) { dismiss() }
                        .tint(.white)
                }
            }
        }
    }

    /// A selectable row with the trailing checkmark; nil = "Any ingredient".
    private func ingredientRow(label: String?) -> some View {
        let isSelected = selectedIngredient == label
        return Button {
            onIngredientSelected(label)
            dismiss()
        } label: {
            HStack {
                if let label {
                    Text(label)
                } else {
                    Text(L(\.filter_any_ingredient))
                }

                Spacer()

                if isSelected {
                    Image(systemName: "checkmark")
                        .font(.body.weight(.semibold))
                        .foregroundColor(AppColors.primary(appearance))
                }
            }
            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
        }
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

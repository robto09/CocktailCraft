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
        case .any: return "Any"
        case .alcoholic: return "Alcoholic"
        case .nonAlcoholic: return "Non-Alcoholic"
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

/// Advanced-search filter form driven by the shared HomeViewModel. Categories,
/// ingredients and glasses come from the shared API-backed option lists; Apply
/// forwards a `SearchFilters` to the shared `applyFilters`, Clear resets them.
struct AdvancedSearchSheet: View {
    let viewModel: HomeViewModelSKIE
    @Environment(\.dismiss) private var dismiss
    @Environment(\.isDarkMode) private var isDarkMode

    @State private var selectedCategory: String?
    @State private var selectedIngredient: String?
    @State private var alcoholic: AlcoholicFilter

    private var primaryColor: Color { AppColors.primary(isDarkMode: isDarkMode) }

    init(viewModel: HomeViewModelSKIE) {
        self.viewModel = viewModel
        let filters = viewModel.state.searchFilters
        _selectedCategory = State(initialValue: filters.category)
        _selectedIngredient = State(initialValue: filters.ingredient)
        _alcoholic = State(initialValue: AlcoholicFilter.from(filters.alcoholic))
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Category") {
                    Picker("Category", selection: $selectedCategory) {
                        Text("Any").tag(String?.none)
                        ForEach(viewModel.state.filterCategories, id: \.self) { category in
                            Text(category).tag(String?.some(category))
                        }
                    }
                }

                Section("Ingredient") {
                    Picker("Ingredient", selection: $selectedIngredient) {
                        Text("Any").tag(String?.none)
                        ForEach(viewModel.state.filterIngredients, id: \.self) { ingredient in
                            Text(ingredient).tag(String?.some(ingredient))
                        }
                    }
                }

                Section("Alcoholic") {
                    // Chip styling mirrors the Android FilterChip: selected =
                    // brand-color fill with white text, unselected = brand-color
                    // text on the row background.
                    HStack(spacing: 8) {
                        ForEach(AlcoholicFilter.allCases, id: \.self) { option in
                            Button(action: { alcoholic = option }) {
                                Text(option.label)
                                    .font(.subheadline.weight(alcoholic == option ? .bold : .medium))
                                    .foregroundColor(alcoholic == option ? .white : primaryColor)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 6)
                                    .background(alcoholic == option ? primaryColor : Color.clear)
                                    .cornerRadius(16)
                            }
                            .buttonStyle(.plain)
                        }
                        Spacer()
                    }
                }
            }
            .navigationTitle("Advanced Search")
            .brandedNavigationBar()
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Clear") {
                        viewModel.clearSearchFilters()
                        dismiss()
                    }
                    .tint(.white)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Apply") {
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
                    }
                    // White pill with brand-color label so it stays visible
                    // on the branded coral navigation bar
                    .buttonStyle(.borderedProminent)
                    .tint(.white)
                    .foregroundStyle(primaryColor)
                }
            }
            .task {
                // Populate the option lists from the shared API-backed state.
                await viewModel.loadFilterOptions()
            }
        }
    }
}

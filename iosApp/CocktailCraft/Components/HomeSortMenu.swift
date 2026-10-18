import SwiftUI
import shared

/// Home's sort control for the navigation bar, the counterpart of Android's
/// HomeSortButton: a menu of every shared HomeSortOption with the current
/// one checked. The icon fills in while a non-default sort is applied, and
/// VoiceOver reads "Sort cocktails, Price: low to high".
struct HomeSortMenu: View {
    let selection: HomeSortOption
    let onSelect: (HomeSortOption) -> Void

    var body: some View {
        Menu {
            // An inline picker gives each option a checkmark and the
            // selected trait
            Picker(L(\.home_sort_menu_title), selection: Binding(get: { selection }, set: onSelect)) {
                ForEach(HomeSortOption.allCases, id: \.self) { option in
                    Text(option.title).tag(option)
                }
            }
            .pickerStyle(.inline)
        } label: {
            Image(systemName: selection == .default
                  ? "arrow.up.arrow.down.circle"
                  : "arrow.up.arrow.down.circle.fill")
                .font(.title3)
                .minimumHitTarget()
        }
        .accessibilityLabel(L(\.home_sort_a11y))
        .accessibilityValue(selection.title)
        .accessibilityIdentifier("home.sortMenu")
    }
}

extension HomeSortOption {
    /// The option's shared, localized name ("Price: low to high").
    var title: String { label.desc().localized() }
}

import SwiftUI

struct SearchBar: View {
    @Binding var text: String
    let hasActiveFilters: Bool
    let onFilterTap: () -> Void
    
    var body: some View {
        HStack(spacing: 12) {
            // Search Field
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.secondary)
                
                TextField("Search cocktails...", text: $text)
                    .textFieldStyle(.plain)
                
                if !text.isEmpty {
                    Button(action: {
                        text = ""
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(Color(.systemGray6))
            .cornerRadius(10)
            
            // Filter Button
            Button(action: onFilterTap) {
                Image(systemName: "slider.horizontal.3")
                    .foregroundColor(hasActiveFilters ? .white : .primary)
                    .padding(8)
                    .background(hasActiveFilters ? Color.accentColor : Color(.systemGray6))
                    .cornerRadius(10)
            }
        }
    }
}
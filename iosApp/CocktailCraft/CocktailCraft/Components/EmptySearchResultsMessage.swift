import SwiftUI

struct EmptySearchResultsMessage: View {
    let searchQuery: String
    let hasFilters: Bool
    let onClearSearch: () -> Void
    let onClearFilters: () -> Void
    
    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            
            // Empty State Icon
            Image(systemName: "magnifyingglass")
                .font(.system(size: 60))
                .foregroundColor(.secondary)
            
            // Title
            Text("No Results Found")
                .font(.title2)
                .fontWeight(.semibold)
            
            // Message
            if !searchQuery.isEmpty {
                Text("No cocktails found for \"\(searchQuery)\"")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 40)
            } else if hasFilters {
                Text("No cocktails match your current filters")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 40)
            } else {
                Text("No cocktails available")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 40)
            }
            
            // Action Buttons
            VStack(spacing: 12) {
                if !searchQuery.isEmpty {
                    Button(action: onClearSearch) {
                        Text("Clear Search")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: 200)
                            .padding(.vertical, 12)
                            .background(Color.accentColor)
                            .cornerRadius(10)
                    }
                }
                
                if hasFilters {
                    Button(action: onClearFilters) {
                        Text("Clear Filters")
                            .font(.headline)
                            .foregroundColor(.accentColor)
                            .frame(maxWidth: 200)
                            .padding(.vertical, 12)
                            .background(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color.accentColor, lineWidth: 2)
                            )
                    }
                }
            }
            .padding(.top, 20)
            
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemBackground))
    }
}
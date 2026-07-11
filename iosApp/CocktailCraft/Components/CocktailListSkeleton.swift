import SwiftUI

/// Redacted placeholder list matching the cocktail card layout, shown while
/// the first page loads.
struct CocktailListSkeleton: View {
    var rowCount: Int = 6
    @Environment(\.isDarkMode) private var isDarkMode

    private var surfaceColor: Color { AppColors.surface(isDarkMode: isDarkMode) }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(0..<rowCount, id: \.self) { _ in
                    skeletonRow
                }
            }
            .padding(16)
        }
    }

    private var skeletonRow: some View {
        HStack(spacing: 16) {
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.gray.opacity(0.2))
                .frame(width: 120, height: 120)

            VStack(alignment: .leading, spacing: 6) {
                // Title placeholder
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color.gray.opacity(0.2))
                    .frame(height: 22)

                // Subtitle placeholder
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color.gray.opacity(0.2))
                    .frame(width: 140, height: 18)

                // Ingredients placeholder
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color.gray.opacity(0.2))
                    .frame(width: 120, height: 16)

                Spacer(minLength: 8)

                // Bottom row placeholder
                HStack {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(Color.gray.opacity(0.2))
                        .frame(width: 70, height: 24)

                    Spacer()

                    HStack(spacing: 12) {
                        Circle()
                            .fill(Color.gray.opacity(0.2))
                            .frame(width: 40, height: 40)

                        Circle()
                            .fill(Color.gray.opacity(0.2))
                            .frame(width: 40, height: 40)
                    }
                }
            }
            .padding(.vertical, 8)
        }
        .padding(16)
        .background(surfaceColor)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.12), radius: 6, x: 0, y: 3)
        .redacted(reason: .placeholder)
    }
}

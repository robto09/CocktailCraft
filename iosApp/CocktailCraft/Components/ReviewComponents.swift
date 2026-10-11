import SwiftUI
import shared

/// Read-only row of stars mirroring Android's RatingBar: whole stars are
/// filled, a fractional star is drawn at partial opacity, the rest at 30%.
struct StarRatingView: View {
    let rating: Float
    var maxRating: Int = 5
    var starSize: CGFloat = 12
    var spacing: CGFloat = 2

    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        let clamped = min(max(rating, 0), Float(maxRating))
        HStack(spacing: spacing) {
            ForEach(0..<maxRating, id: \.self) { index in
                Image(systemName: "star.fill")
                    .font(.system(size: starSize))
                    .foregroundColor(AppColors.secondary(isDarkMode: isDarkMode)
                        .opacity(Self.starOpacity(index: index, rating: clamped)))
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(String(localized: "\(Int(clamped.rounded())) out of \(maxRating) stars"))
    }

    static func starOpacity(index: Int, rating: Float) -> Double {
        let whole = Int(rating)
        let fraction = rating - Float(whole)
        if index < whole { return 1.0 }
        if index == whole && fraction > 0 { return Double(fraction) }
        return 0.3
    }
}

/// A single review in the detail screen's Reviews card, mirroring Android's
/// CocktailReviewItem: initial avatar, name, stars + date, then the comment.
struct ReviewRow: View {
    let review: Review

    @Environment(\.isDarkMode) private var isDarkMode

    private static let avatarSize: CGFloat = 40

    private var displayName: String { Self.displayName(for: review.userName) }
    private var displayDate: String { Self.displayDate(for: review.date) }

    /// Blank names fall back to "Anonymous", like Android's review item.
    static func displayName(for userName: String) -> String {
        let name = userName.trimmingCharacters(in: .whitespacesAndNewlines)
        return name.isEmpty ? String(localized: "Anonymous") : name
    }

    /// Missing dates fall back to "Unknown date", like Android's review item.
    static func displayDate(for date: String) -> String {
        date.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? String(localized: "Unknown date")
            : date
    }

    /// Avatar letter: first character of the display name, uppercased.
    static func initial(for userName: String) -> String {
        displayName(for: userName).prefix(1).uppercased()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
            HStack(spacing: AppTheme.Spacing.md) {
                Text(Self.initial(for: review.userName))
                    .font(.headline)
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                    .frame(width: Self.avatarSize, height: Self.avatarSize)
                    .background(AppColors.primary(isDarkMode: isDarkMode).opacity(0.2))
                    .clipShape(Circle())
                    .accessibilityHidden(true)

                VStack(alignment: .leading, spacing: AppTheme.Spacing.xs) {
                    Text(displayName)
                        .font(.body)
                        .fontWeight(.semibold)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                    HStack(spacing: AppTheme.Spacing.sm) {
                        StarRatingView(rating: review.rating)
                        Text(displayDate)
                            .font(.caption)
                            .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    }
                }
            }

            if !review.comment.isEmpty {
                Text(review.comment)
                    .font(.subheadline)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .fixedSize(horizontal: false, vertical: true)
                    .padding(.leading, Self.avatarSize + AppTheme.Spacing.md)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .combine)
    }
}

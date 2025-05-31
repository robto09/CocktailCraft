import SwiftUI

struct RatingDisplay: View {
    let rating: Float
    let reviewCount: Int
    var showReviewCount: Bool = true
    var starSize: CGFloat = 16
    var ratingTextSize: CGFloat = 14
    var reviewCountTextSize: CGFloat = 14
    var starsColor: Color = .orange
    var textColor: Color = .secondary
    var useHalfStars: Bool = true
    
    var body: some View {
        HStack(spacing: 4) {
            Text(String(format: "%.1f", rating))
                .font(.system(size: ratingTextSize, weight: .bold))
                .foregroundColor(textColor)
            
            RatingBar(
                rating: rating,
                starColor: starsColor,
                starSize: starSize,
                useHalfStars: useHalfStars
            )
            
            if showReviewCount && reviewCount > 0 {
                Text("(\(reviewCount) \(reviewCount == 1 ? "review" : "reviews"))")
                    .font(.system(size: reviewCountTextSize))
                    .foregroundColor(textColor)
            }
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        RatingDisplay(rating: 4.5, reviewCount: 127)
        RatingDisplay(rating: 3.2, reviewCount: 45, useHalfStars: false)
        RatingDisplay(rating: 5.0, reviewCount: 1)
        RatingDisplay(rating: 4.0, reviewCount: 0)
    }
    .padding()
}
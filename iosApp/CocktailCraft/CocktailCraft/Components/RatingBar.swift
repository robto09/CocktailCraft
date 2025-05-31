import SwiftUI

struct RatingBar: View {
    let rating: Float
    var stars: Int = 5
    var starColor: Color = .orange
    var starSize: CGFloat = 16
    var spaceBetween: CGFloat = 2
    var useHalfStars: Bool = false
    
    var body: some View {
        HStack(spacing: spaceBetween) {
            if useHalfStars {
                ForEach(0..<stars, id: \.self) { index in
                    let starValue = Float(index + 1)
                    Image(systemName: starIcon(for: starValue))
                        .foregroundColor(starColor)
                        .font(.system(size: starSize))
                }
            } else {
                ForEach(0..<stars, id: \.self) { index in
                    Image(systemName: "star.fill")
                        .foregroundColor(starColor.opacity(starOpacity(for: index)))
                        .font(.system(size: starSize))
                }
            }
        }
    }
    
    private func starIcon(for starValue: Float) -> String {
        if starValue <= rating {
            return "star.fill"
        } else if starValue - 0.5 <= rating {
            return "star.leadinghalf.filled"
        } else {
            return "star"
        }
    }
    
    private func starOpacity(for index: Int) -> Double {
        let floatIndex = Float(index)
        if floatIndex < floor(rating) {
            return 1.0
        } else if floatIndex == floor(rating) && rating.truncatingRemainder(dividingBy: 1) != 0 {
            return Double(rating.truncatingRemainder(dividingBy: 1))
        } else {
            return 0.3
        }
    }
}

struct InteractiveRatingBar: View {
    @Binding var rating: Float
    var stars: Int = 5
    var starColor: Color = .orange
    var starSize: CGFloat = 24
    var spaceBetween: CGFloat = 4
    var onChange: ((Float) -> Void)?
    
    var body: some View {
        HStack(spacing: spaceBetween) {
            ForEach(1...stars, id: \.self) { star in
                Image(systemName: star <= Int(rating) ? "star.fill" : "star")
                    .foregroundColor(star <= Int(rating) ? starColor : starColor.opacity(0.3))
                    .font(.system(size: starSize))
                    .onTapGesture {
                        withAnimation(.easeInOut(duration: 0.2)) {
                            rating = Float(star)
                            onChange?(rating)
                        }
                    }
            }
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        RatingBar(rating: 4.5, useHalfStars: true)
        RatingBar(rating: 3.7, useHalfStars: false)
        
        InteractiveRatingBar(rating: .constant(3))
    }
    .padding()
}
import SwiftUI
import Shared

struct ReviewsSection: View {
    let cocktailId: String
    let reviews: [Review]
    let onWriteReview: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Header
            HStack {
                Text("Reviews (\(reviews.count))")
                    .font(.headline)
                
                Spacer()
                
                Button(action: onWriteReview) {
                    HStack(spacing: 4) {
                        Image(systemName: "square.and.pencil")
                            .font(.caption)
                        Text("Write a Review")
                            .font(.caption)
                            .fontWeight(.medium)
                    }
                }
                .foregroundColor(.accentColor)
            }
            
            if reviews.isEmpty {
                // Empty state
                VStack(spacing: 12) {
                    Image(systemName: "text.bubble")
                        .font(.system(size: 40))
                        .foregroundColor(.secondary)
                    
                    Text("Be the first to review!")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 20)
            } else {
                // Review list
                VStack(spacing: 16) {
                    ForEach(reviews.prefix(3), id: \.id) { review in
                        ReviewCard(review: review)
                    }
                    
                    if reviews.count > 3 {
                        Button(action: {
                            // TODO: Navigate to all reviews
                        }) {
                            Text("View all \(reviews.count) reviews")
                                .font(.caption)
                                .fontWeight(.medium)
                                .foregroundColor(.accentColor)
                        }
                        .frame(maxWidth: .infinity)
                    }
                }
            }
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
    }
}

struct ReviewCard: View {
    let review: Review
    
    var userInitial: String {
        if let firstChar = review.userName.first {
            return String(firstChar).uppercased()
        }
        return "?"
    }
    
    var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter.string(from: Date(timeIntervalSince1970: TimeInterval(review.date / 1000)))
    }
    
    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // User Avatar
            Circle()
                .fill(Color.accentColor.opacity(0.2))
                .frame(width: 40, height: 40)
                .overlay(
                    Text(userInitial)
                        .font(.headline)
                        .foregroundColor(.accentColor)
                )
            
            VStack(alignment: .leading, spacing: 6) {
                // User name and date
                HStack {
                    Text(review.userName.isEmpty ? "Anonymous" : review.userName)
                        .font(.subheadline)
                        .fontWeight(.medium)
                    
                    Spacer()
                    
                    Text(formattedDate)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                // Rating stars
                HStack(spacing: 2) {
                    ForEach(0..<5) { index in
                        Image(systemName: index < Int(review.rating) ? "star.fill" : "star")
                            .font(.caption)
                            .foregroundColor(.yellow)
                    }
                }
                
                // Review comment
                if !review.comment.isEmpty {
                    Text(review.comment)
                        .font(.body)
                        .foregroundColor(.secondary)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
        }
        .padding()
        .background(Color(.tertiarySystemBackground))
        .cornerRadius(10)
    }
}
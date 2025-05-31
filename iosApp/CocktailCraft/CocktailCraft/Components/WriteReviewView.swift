import SwiftUI
import Shared

struct WriteReviewView: View {
    let cocktail: Cocktail
    let onSubmit: (Review) -> Void
    let onCancel: () -> Void
    
    @State private var userName = ""
    @State private var rating = 0
    @State private var comment = ""
    @State private var showingNameError = false
    
    var body: some View {
        NavigationView {
            Form {
                Section("Your Information") {
                    TextField("Your Name", text: $userName)
                        .textContentType(.name)
                    
                    if showingNameError {
                        Text("Please enter your name")
                            .font(.caption)
                            .foregroundColor(.red)
                    }
                }
                
                Section("Rating") {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("How would you rate \(cocktail.strDrink)?")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        
                        HStack(spacing: 8) {
                            ForEach(1...5, id: \.self) { star in
                                Button(action: {
                                    rating = star
                                }) {
                                    Image(systemName: star <= rating ? "star.fill" : "star")
                                        .font(.title2)
                                        .foregroundColor(star <= rating ? .yellow : .gray)
                                }
                            }
                        }
                    }
                }
                
                Section("Comments (Optional)") {
                    TextEditor(text: $comment)
                        .frame(minHeight: 100)
                }
            }
            .navigationTitle("Write Review")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel", action: onCancel)
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Submit") {
                        submitReview()
                    }
                    .fontWeight(.semibold)
                    .disabled(rating == 0)
                }
            }
        }
    }
    
    private func submitReview() {
        // Validate name
        guard !userName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            showingNameError = true
            return
        }
        
        // Create review
        let review = Review(
            id: UUID().uuidString,
            cocktailId: cocktail.id,
            userName: userName.trimmingCharacters(in: .whitespacesAndNewlines),
            rating: Int32(rating),
            comment: comment.trimmingCharacters(in: .whitespacesAndNewlines),
            date: Int64(Date().timeIntervalSince1970 * 1000)
        )
        
        onSubmit(review)
    }
}
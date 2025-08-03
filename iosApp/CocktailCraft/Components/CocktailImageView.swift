import SwiftUI

struct CocktailImageView: View {
    let imageUrl: String?
    var height: CGFloat = 300
    var cornerRadius: CGFloat = 0
    
    var body: some View {
        AsyncImage(url: URL(string: imageUrl ?? "")) { image in
            image
                .resizable()
                .aspectRatio(contentMode: .fill)
        } placeholder: {
            ProgressView()
                .frame(height: height)
        }
        .frame(maxHeight: height)
        .clipped()
        .cornerRadius(cornerRadius)
    }
}

#Preview {
    CocktailImageView(
        imageUrl: "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg",
        height: 200,
        cornerRadius: 12
    )
}

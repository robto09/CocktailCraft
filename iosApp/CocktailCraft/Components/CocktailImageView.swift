import SwiftUI
import Kingfisher

struct CocktailImageView: View {
    let imageUrl: String?
    var height: CGFloat = 300
    var cornerRadius: CGFloat = 0
    /// Fixed width for card/grid layouts; nil keeps the flexible-width
    /// hero behavior.
    var width: CGFloat? = nil

    var body: some View {
        Group {
            if let urlString = imageUrl, !urlString.isEmpty, let url = URL(string: urlString) {
                KFImage(url)
                    .placeholder {
                        ProgressView()
                            .frame(height: height)
                    }
                    .retry(maxCount: 3, interval: .seconds(2))
                    .onFailureImage(UIImage(systemName: "photo"))
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } else {
                // No URL to load: show a static fallback, never a spinner
                Image(systemName: "photo")
                    .font(.largeTitle)
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, minHeight: min(height, 80))
            }
        }
        .frame(width: width)
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

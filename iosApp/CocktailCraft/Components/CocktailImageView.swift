import SwiftUI
import Kingfisher

struct CocktailImageView: View {
    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.displayScale) private var displayScale
    let imageUrl: String?
    var height: CGFloat = 300
    var cornerRadius: CGFloat = 0
    /// Fixed width for card/grid layouts; nil keeps the flexible-width
    /// hero behavior.
    var width: CGFloat? = nil

    var body: some View {
        Group {
            if let urlString = imageUrl, !urlString.isEmpty,
               let url = URL(string: thumbnailSide == nil ? urlString : CocktailImageURL.thumbnail(for: urlString)) {
                kfImage(url)
                    .placeholder {
                        // Shimmering block until the photo arrives
                        Rectangle()
                            .fill(AppColors.neutralSubtle(isDarkMode: isDarkMode))
                            .frame(maxWidth: .infinity)
                            .frame(height: height)
                            .shimmering()
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
        // Decorative everywhere it is used: the cocktail's name is always
        // next to it (and "photo" placeholders mean nothing when spoken)
        .accessibilityHidden(true)
    }

    /// Side (pt) of a small fixed slot — list rows, cards, the cart — or nil
    /// for the flexible-width detail hero, which keeps the full-size photo.
    /// Without a width the view fills its slot, which callers size from
    /// `height` (e.g. the cart's 80x80 frame).
    private var thumbnailSide: CGFloat? {
        let side = max(width ?? height, height)
        return side <= Self.maxThumbnailSide ? side : nil
    }

    /// The largest slot the ~350px /medium variant still covers well.
    private static let maxThumbnailSide: CGFloat = 150

    /// Small slots decode the photo straight to their pixel size instead of
    /// holding the full-size bitmap in memory for a ~100pt thumbnail.
    private func kfImage(_ url: URL) -> KFImage {
        let image = KFImage(url)
        guard let side = thumbnailSide else { return image }
        return image
            .setProcessor(DownsamplingImageProcessor(size: CGSize(width: side, height: side)))
            .scaleFactor(displayScale)
    }
}

/// TheCocktailDB serves resized copies of every drink photo next to the
/// original: `<photo url>/small`, `/medium` (~350px) and `/large`, while
/// originals are often 700px or more. List-sized slots request /medium —
/// the same idea as the Random Cocktail widget's /preview (Android's
/// OptimizedImage does the same).
enum CocktailImageURL {
    static func thumbnail(for urlString: String) -> String {
        guard urlString.hasPrefix("https://www.thecocktaildb.com/images/media/drink/"),
              let last = urlString.split(separator: "/").last, last.contains(".") else {
            return urlString
        }
        return urlString + "/medium"
    }
}

#Preview {
    CocktailImageView(
        imageUrl: "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg",
        height: 200,
        cornerRadius: 12
    )
}

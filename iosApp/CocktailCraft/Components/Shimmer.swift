import SwiftUI

/// Sweeps a soft highlight across the shapes of the view it is applied to,
/// the iOS counterpart of Android's placeholder shimmer (surface color at
/// 60% over neutralSubtle blocks). Apply it once to a group of
/// ``SkeletonBlock``s rather than to each block, so the whole group shares one
/// animation and shimmers as a single wave.
///
/// The sweep starts with onAppear + withAnimation, so Reduce Motion (the
/// in-app setting or the system one) turns it off through ContentView's
/// app-wide transaction: the blocks render static.
struct ShimmerModifier: ViewModifier {
    @Environment(\.isDarkMode) private var isDarkMode
    // -1: highlight just left of the content, 1: just right of it
    @State private var phase: CGFloat = -1

    func body(content: Content) -> some View {
        content
            .overlay {
                GeometryReader { geometry in
                    LinearGradient(
                        colors: [.clear, highlightColor, .clear],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                    .frame(width: geometry.size.width)
                    .offset(x: phase * geometry.size.width)
                }
                // Highlight only where the skeleton shapes are drawn
                .mask { content }
                .allowsHitTesting(false)
            }
            .onAppear {
                withAnimation(.linear(duration: 1.5).delay(0.2).repeatForever(autoreverses: false)) {
                    phase = 1
                }
            }
    }

    private var highlightColor: Color {
        AppColors.surface(isDarkMode: isDarkMode).opacity(0.6)
    }
}

extension View {
    /// Animated loading highlight; see ``ShimmerModifier``.
    func shimmering() -> some View {
        modifier(ShimmerModifier())
    }
}

/// One skeleton shape in the neutralSubtle fill (the image-placeholder
/// color, as on Android). A nil width fills the available width.
struct SkeletonBlock: View {
    var width: CGFloat? = nil
    var height: CGFloat
    var cornerRadius: CGFloat = 4
    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        // Circular corners: a radius of half the size gives a true circle
        // (the default continuous style would draw a squircle)
        RoundedRectangle(cornerRadius: cornerRadius, style: .circular)
            .fill(AppColors.neutralSubtle(isDarkMode: isDarkMode))
            .frame(width: width, height: height)
            .frame(maxWidth: width == nil ? .infinity : nil, alignment: .leading)
    }
}

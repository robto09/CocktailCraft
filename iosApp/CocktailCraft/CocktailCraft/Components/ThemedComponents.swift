import SwiftUI

// MARK: - Themed Button Styles
struct PrimaryButtonStyle: ButtonStyle {
    @Environment(\.colorScheme) var colorScheme
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 16, weight: .medium))
            .foregroundColor(.white)
            .padding(.horizontal, 24)
            .padding(.vertical, 12)
            .background(Color.primary(for: colorScheme))
            .cornerRadius(8)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
    }
}

struct SecondaryButtonStyle: ButtonStyle {
    @Environment(\.colorScheme) var colorScheme
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 16, weight: .medium))
            .foregroundColor(Color.primary(for: colorScheme))
            .padding(.horizontal, 24)
            .padding(.vertical, 12)
            .background(Color.clear)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color.primary(for: colorScheme), lineWidth: 1)
            )
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
    }
}

// MARK: - Themed Text Views
struct ThemedTitle: View {
    let text: String
    let size: CGFloat
    @Environment(\.colorScheme) var colorScheme
    
    init(_ text: String, size: CGFloat = 20) {
        self.text = text
        self.size = size
    }
    
    var body: some View {
        Text(text)
            .font(.system(size: size, weight: .bold))
            .foregroundColor(Color.textPrimary(for: colorScheme))
    }
}

struct ThemedSubtitle: View {
    let text: String
    let size: CGFloat
    @Environment(\.colorScheme) var colorScheme
    
    init(_ text: String, size: CGFloat = 16) {
        self.text = text
        self.size = size
    }
    
    var body: some View {
        Text(text)
            .font(.system(size: size))
            .foregroundColor(Color.textSecondary(for: colorScheme))
    }
}

// MARK: - Themed Card
struct ThemedCardView<Content: View>: View {
    let content: Content
    @Environment(\.colorScheme) var colorScheme
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
            .background(Color.surface(for: colorScheme))
            .cornerRadius(12)
            .shadow(
                color: Color.black.opacity(colorScheme == .dark ? 0.3 : 0.1),
                radius: 2,
                x: 0,
                y: 1
            )
    }
}

// MARK: - Usage Examples
struct ThemedComponentExamples: View {
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        VStack(spacing: 20) {
            // Example of themed title
            ThemedTitle("Welcome to CocktailCraft")
            
            // Example of themed subtitle
            ThemedSubtitle("Discover amazing cocktails")
            
            // Example of themed card
            ThemedCardView {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Mojito")
                        .font(.headline)
                        .foregroundColor(Color.textPrimary(for: colorScheme))
                    Text("A refreshing mint cocktail")
                        .font(.subheadline)
                        .foregroundColor(Color.textSecondary(for: colorScheme))
                }
                .padding()
            }
            .padding(.horizontal)
            
            // Example of themed buttons
            HStack(spacing: 16) {
                Button("Primary") {}
                    .buttonStyle(PrimaryButtonStyle())
                
                Button("Secondary") {}
                    .buttonStyle(SecondaryButtonStyle())
            }
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.background(for: colorScheme))
    }
}
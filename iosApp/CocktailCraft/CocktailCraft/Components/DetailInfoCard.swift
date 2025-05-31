import SwiftUI

struct DetailInfoCard<Content: View>: View {
    let title: String
    let content: () -> Content
    var titleFontSize: CGFloat = 18
    var titleFontWeight: Font.Weight = .bold
    var titleColor: Color = .primary
    var backgroundColor: Color = Color(UIColor.secondarySystemBackground)
    var shadowRadius: CGFloat = 2
    var cornerRadius: CGFloat = 12
    var contentPadding: CGFloat = 16
    
    init(
        title: String,
        titleFontSize: CGFloat = 18,
        titleFontWeight: Font.Weight = .bold,
        titleColor: Color = .primary,
        backgroundColor: Color = Color(UIColor.secondarySystemBackground),
        shadowRadius: CGFloat = 2,
        cornerRadius: CGFloat = 12,
        contentPadding: CGFloat = 16,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.title = title
        self.titleFontSize = titleFontSize
        self.titleFontWeight = titleFontWeight
        self.titleColor = titleColor
        self.backgroundColor = backgroundColor
        self.shadowRadius = shadowRadius
        self.cornerRadius = cornerRadius
        self.contentPadding = contentPadding
        self.content = content
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.system(size: titleFontSize, weight: titleFontWeight))
                .foregroundColor(titleColor)
            
            content()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(contentPadding)
        .background(backgroundColor)
        .cornerRadius(cornerRadius)
        .shadow(color: Color.black.opacity(0.1), radius: shadowRadius, x: 0, y: 1)
    }
}

#Preview {
    VStack(spacing: 16) {
        DetailInfoCard(title: "Ingredients") {
            VStack(alignment: .leading, spacing: 8) {
                ForEach(["2 oz Vodka", "1 oz Lime juice", "0.5 oz Simple syrup", "Ginger beer"], id: \.self) { ingredient in
                    HStack {
                        Image(systemName: "circle.fill")
                            .font(.system(size: 6))
                            .foregroundColor(.secondary)
                        Text(ingredient)
                            .font(.body)
                    }
                }
            }
        }
        
        DetailInfoCard(title: "Instructions") {
            Text("1. Add vodka, lime juice and simple syrup to a shaker with ice.\n2. Shake well and strain into a copper mug filled with ice.\n3. Top with ginger beer and garnish with a lime wheel.")
                .font(.body)
                .foregroundColor(.secondary)
        }
        
        DetailInfoCard(
            title: "Glass Type",
            backgroundColor: .orange.opacity(0.1),
            shadowRadius: 0
        ) {
            HStack {
                Image(systemName: "wineglass")
                    .font(.title2)
                    .foregroundColor(.orange)
                Text("Copper Mug")
                    .font(.body)
            }
        }
    }
    .padding()
}
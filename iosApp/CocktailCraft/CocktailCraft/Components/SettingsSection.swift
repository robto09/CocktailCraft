import SwiftUI

struct SettingsSection<Content: View>: View {
    let title: String
    let content: Content
    
    init(title: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(title)
                .font(.system(size: 18, weight: .bold))
                .padding(.bottom, 16)
            
            content
        }
        .padding()
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(16)
    }
}
import SwiftUI

struct ModernSearchBar: View {
    @Binding var text: String
    @State private var isEditing = false
    
    var placeholder: String = "Search cocktails..."
    var onSearchButtonClicked: (() -> Void)?
    var onCancelButtonClicked: (() -> Void)?
    
    var body: some View {
        HStack {
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(.secondary)
                
                TextField(placeholder, text: $text)
                    .textFieldStyle(.plain)
                    .onTapGesture {
                        isEditing = true
                    }
                    .onSubmit {
                        onSearchButtonClicked?()
                    }
                
                if !text.isEmpty {
                    Button(action: {
                        text = ""
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(.secondary)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 10))
            
            if isEditing {
                Button("Cancel") {
                    isEditing = false
                    text = ""
                    onCancelButtonClicked?()
                    // Dismiss keyboard
                    UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
                }
                .foregroundStyle(.blue)
                .transition(.move(edge: .trailing).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.2), value: isEditing)
    }
}

#Preview {
    VStack {
        ModernSearchBar(text: .constant(""))
        ModernSearchBar(text: .constant("Margarita"))
    }
    .padding()
}

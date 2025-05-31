import SwiftUI

struct FilterChip: View {
    let label: String
    let selected: Bool
    let onClick: () -> Void
    var selectedColor: Color = .orange
    var unselectedColor: Color = .white
    var selectedTextColor: Color = .white
    var unselectedTextColor: Color = .orange
    var trailingIcon: String? = nil
    var selectedIconColor: Color = .white
    
    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 4) {
                Text(label)
                    .font(.system(size: 14, weight: selected ? .bold : .medium))
                    .foregroundColor(selected ? selectedTextColor : unselectedTextColor)
                
                if let trailingIcon = trailingIcon {
                    Image(systemName: trailingIcon)
                        .font(.system(size: 12))
                        .foregroundColor(selected ? selectedIconColor : unselectedTextColor)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 6)
            .background(selected ? selectedColor : unselectedColor)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(selected ? Color.clear : selectedColor.opacity(0.2), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(selected ? 0.1 : 0), radius: selected ? 2 : 0, x: 0, y: 1)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct MultiSelectFilterChips: View {
    let options: [String]
    @Binding var selectedOptions: Set<String>
    var selectedColor: Color = .orange
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(options, id: \.self) { option in
                    FilterChip(
                        label: option,
                        selected: selectedOptions.contains(option),
                        onClick: {
                            if selectedOptions.contains(option) {
                                selectedOptions.remove(option)
                            } else {
                                selectedOptions.insert(option)
                            }
                        },
                        selectedColor: selectedColor
                    )
                }
            }
            .padding(.horizontal)
        }
    }
}

struct SingleSelectFilterChips: View {
    let options: [String]
    @Binding var selectedOption: String?
    var selectedColor: Color = .orange
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(options, id: \.self) { option in
                    FilterChip(
                        label: option,
                        selected: selectedOption == option,
                        onClick: {
                            selectedOption = selectedOption == option ? nil : option
                        },
                        selectedColor: selectedColor
                    )
                }
            }
            .padding(.horizontal)
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        // Individual chips
        HStack(spacing: 8) {
            FilterChip(label: "All", selected: true, onClick: {})
            FilterChip(label: "Vodka", selected: false, onClick: {})
            FilterChip(label: "Gin", selected: false, onClick: {}, trailingIcon: "chevron.down")
        }
        
        // Multi-select
        MultiSelectFilterChips(
            options: ["Sweet", "Sour", "Bitter", "Fruity", "Creamy"],
            selectedOptions: .constant(Set(["Sweet", "Fruity"]))
        )
        
        // Single-select
        SingleSelectFilterChips(
            options: ["Easy", "Medium", "Hard"],
            selectedOption: .constant("Easy")
        )
    }
    .padding()
}
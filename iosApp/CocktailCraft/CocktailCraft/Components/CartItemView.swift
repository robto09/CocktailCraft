import SwiftUI
import shared

struct CartItemView: View {
    let item: CocktailCartItem
    let isFavorite: Bool
    let onIncreaseQuantity: () -> Void
    let onDecreaseQuantity: () -> Void
    let onRemove: () -> Void
    let onToggleFavorite: () -> Void
    
    private var currencyFormatter: NumberFormatter {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = Locale(identifier: "en_US")
        return formatter
    }
    
    var body: some View {
        HStack(spacing: 12) {
            // Product Image
            if let imageUrl = item.cocktail.imageUrl, let url = URL(string: imageUrl) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.gray.opacity(0.2))
                            .frame(width: 80, height: 80)
                            .overlay(
                                ProgressView()
                                    .scaleEffect(0.7)
                            )
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(width: 80, height: 80)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    case .failure(_):
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.gray.opacity(0.2))
                            .frame(width: 80, height: 80)
                            .overlay(
                                Image(systemName: "photo")
                                    .foregroundColor(.gray)
                            )
                    @unknown default:
                        EmptyView()
                    }
                }
            } else {
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.gray.opacity(0.2))
                    .frame(width: 80, height: 80)
                    .overlay(
                        Image(systemName: "photo")
                            .foregroundColor(.gray)
                    )
            }
            
            VStack(alignment: .leading, spacing: 4) {
                // Name and Favorite Button
                HStack {
                    Text(item.cocktail.name)
                        .font(.system(size: 16, weight: .bold))
                        .lineLimit(1)
                        .truncationMode(.tail)
                    
                    Spacer()
                    
                    Button(action: onToggleFavorite) {
                        Image(systemName: isFavorite ? "heart.fill" : "heart")
                            .font(.system(size: 16))
                            .foregroundColor(isFavorite ? .red : .gray)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
                
                // Unit Price
                Text(currencyFormatter.string(from: NSNumber(value: item.cocktail.price)) ?? "$0.00")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.blue)
                
                // Alcoholic Status
                if let alcoholic = item.cocktail.alcoholic {
                    Text(alcoholic)
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                // Quantity Controls and Total
                HStack {
                    // Quantity Controls
                    HStack(spacing: 12) {
                        Button(action: onDecreaseQuantity) {
                            Image(systemName: "minus")
                                .font(.system(size: 14))
                                .foregroundColor(.primary)
                                .frame(width: 36, height: 36)
                                .background(Color.gray.opacity(0.15))
                                .clipShape(Circle())
                        }
                        .buttonStyle(PlainButtonStyle())
                        
                        Text("\(item.quantity)")
                            .font(.system(size: 16, weight: .bold))
                            .frame(minWidth: 30)
                        
                        Button(action: onIncreaseQuantity) {
                            Image(systemName: "plus")
                                .font(.system(size: 14))
                                .foregroundColor(.white)
                                .frame(width: 36, height: 36)
                                .background(Color.blue)
                                .clipShape(Circle())
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                    
                    Spacer()
                    
                    // Total Price and Delete
                    HStack(spacing: 8) {
                        Text(currencyFormatter.string(from: NSNumber(value: item.cocktail.price * Double(item.quantity))) ?? "$0.00")
                            .font(.system(size: 16, weight: .bold))
                        
                        Button(action: onRemove) {
                            Image(systemName: "trash")
                                .font(.system(size: 16))
                                .foregroundColor(.red)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding()
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 1)
    }
}
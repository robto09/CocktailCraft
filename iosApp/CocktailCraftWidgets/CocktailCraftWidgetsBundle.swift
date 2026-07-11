import WidgetKit
import SwiftUI

/// WidgetKit counterparts to Android's two Glance widgets
/// (FavoritesWidget, RandomCocktailWidget). Both deep-link into the app
/// via cocktailcraft://cocktail/{id} (WidgetBridge.swift).
@main
struct CocktailCraftWidgetsBundle: WidgetBundle {
    var body: some Widget {
        FavoritesWidget()
        RandomCocktailWidget()
    }
}

// Brand colors, duplicated from AppColors because the extension compiles
// neither the app target nor the shared framework.
enum WidgetColors {
    static let primary = Color(red: 0xEB / 255.0, green: 0x6A / 255.0, blue: 0x43 / 255.0)
    static let secondary = Color(red: 0xFF / 255.0, green: 0xC8 / 255.0, blue: 0x4D / 255.0)
}

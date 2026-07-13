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

// Brand colors built from the cross-target tokens in WidgetBridge.swift
// (compiled into this extension), so they cannot drift from AppColors.
enum WidgetColors {
    static let primary = Color(red: BrandColorComponents.primary.red,
                               green: BrandColorComponents.primary.green,
                               blue: BrandColorComponents.primary.blue)
    static let secondary = Color(red: BrandColorComponents.secondary.red,
                                 green: BrandColorComponents.secondary.green,
                                 blue: BrandColorComponents.secondary.blue)
}

package com.coffee.store.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CocktailResponse(
    @SerialName("drinks")
    val drinks: List<CocktailDto>? = null
)

@Serializable
data class CocktailDto(
    @SerialName("idDrink")
    val id: String,
    
    @SerialName("strDrink")
    val name: String,
    
    @SerialName("strDrinkAlternate")
    val alternateName: String? = null,
    
    @SerialName("strTags")
    val tags: String? = null,
    
    @SerialName("strCategory")
    val category: String? = null,
    
    @SerialName("strIBA")
    val iba: String? = null,
    
    @SerialName("strAlcoholic")
    val alcoholic: String? = null,
    
    @SerialName("strGlass")
    val glass: String? = null,
    
    @SerialName("strInstructions")
    val instructions: String? = null,
    
    @SerialName("strDrinkThumb")
    val imageUrl: String? = null,
    
    @SerialName("strIngredient1")
    val ingredient1: String? = null,
    
    @SerialName("strIngredient2")
    val ingredient2: String? = null,
    
    @SerialName("strIngredient3")
    val ingredient3: String? = null,
    
    @SerialName("strIngredient4")
    val ingredient4: String? = null,
    
    @SerialName("strIngredient5")
    val ingredient5: String? = null,
    
    @SerialName("strIngredient6")
    val ingredient6: String? = null,
    
    @SerialName("strIngredient7")
    val ingredient7: String? = null,
    
    @SerialName("strIngredient8")
    val ingredient8: String? = null,
    
    @SerialName("strIngredient9")
    val ingredient9: String? = null,
    
    @SerialName("strIngredient10")
    val ingredient10: String? = null,
    
    @SerialName("strIngredient11")
    val ingredient11: String? = null,
    
    @SerialName("strIngredient12")
    val ingredient12: String? = null,
    
    @SerialName("strIngredient13")
    val ingredient13: String? = null,
    
    @SerialName("strIngredient14")
    val ingredient14: String? = null,
    
    @SerialName("strIngredient15")
    val ingredient15: String? = null,
    
    @SerialName("strMeasure1")
    val measure1: String? = null,
    
    @SerialName("strMeasure2")
    val measure2: String? = null,
    
    @SerialName("strMeasure3")
    val measure3: String? = null,
    
    @SerialName("strMeasure4")
    val measure4: String? = null,
    
    @SerialName("strMeasure5")
    val measure5: String? = null,
    
    @SerialName("strMeasure6")
    val measure6: String? = null,
    
    @SerialName("strMeasure7")
    val measure7: String? = null,
    
    @SerialName("strMeasure8")
    val measure8: String? = null,
    
    @SerialName("strMeasure9")
    val measure9: String? = null,
    
    @SerialName("strMeasure10")
    val measure10: String? = null,
    
    @SerialName("strMeasure11")
    val measure11: String? = null,
    
    @SerialName("strMeasure12")
    val measure12: String? = null,
    
    @SerialName("strMeasure13")
    val measure13: String? = null,
    
    @SerialName("strMeasure14")
    val measure14: String? = null,
    
    @SerialName("strMeasure15")
    val measure15: String? = null,
    
    @SerialName("strImageSource")
    val imageSource: String? = null,
    
    @SerialName("strImageAttribution")
    val imageAttribution: String? = null,
    
    @SerialName("strCreativeCommonsConfirmed")
    val creativeCommonsConfirmed: String? = null,
    
    @SerialName("dateModified")
    val dateModified: String? = null
) {
    fun getIngredients(): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        
        if (!ingredient1.isNullOrBlank()) ingredients.add(Ingredient(ingredient1, measure1 ?: ""))
        if (!ingredient2.isNullOrBlank()) ingredients.add(Ingredient(ingredient2, measure2 ?: ""))
        if (!ingredient3.isNullOrBlank()) ingredients.add(Ingredient(ingredient3, measure3 ?: ""))
        if (!ingredient4.isNullOrBlank()) ingredients.add(Ingredient(ingredient4, measure4 ?: ""))
        if (!ingredient5.isNullOrBlank()) ingredients.add(Ingredient(ingredient5, measure5 ?: ""))
        if (!ingredient6.isNullOrBlank()) ingredients.add(Ingredient(ingredient6, measure6 ?: ""))
        if (!ingredient7.isNullOrBlank()) ingredients.add(Ingredient(ingredient7, measure7 ?: ""))
        if (!ingredient8.isNullOrBlank()) ingredients.add(Ingredient(ingredient8, measure8 ?: ""))
        if (!ingredient9.isNullOrBlank()) ingredients.add(Ingredient(ingredient9, measure9 ?: ""))
        if (!ingredient10.isNullOrBlank()) ingredients.add(Ingredient(ingredient10, measure10 ?: ""))
        if (!ingredient11.isNullOrBlank()) ingredients.add(Ingredient(ingredient11, measure11 ?: ""))
        if (!ingredient12.isNullOrBlank()) ingredients.add(Ingredient(ingredient12, measure12 ?: ""))
        if (!ingredient13.isNullOrBlank()) ingredients.add(Ingredient(ingredient13, measure13 ?: ""))
        if (!ingredient14.isNullOrBlank()) ingredients.add(Ingredient(ingredient14, measure14 ?: ""))
        if (!ingredient15.isNullOrBlank()) ingredients.add(Ingredient(ingredient15, measure15 ?: ""))
        
        return ingredients
    }
}

data class Ingredient(
    val name: String,
    val measure: String
)

@Serializable
data class CategoryResponse(
    @SerialName("drinks")
    val categories: List<CategoryDto>? = null
)

@Serializable
data class CategoryDto(
    @SerialName("strCategory")
    val name: String
)

@Serializable
data class GlassResponse(
    @SerialName("drinks")
    val glasses: List<GlassDto>? = null
)

@Serializable
data class GlassDto(
    @SerialName("strGlass")
    val name: String
)

@Serializable
data class IngredientResponse(
    @SerialName("drinks")
    val ingredients: List<IngredientDto>? = null
)

@Serializable
data class IngredientDto(
    @SerialName("strIngredient1")
    val name: String
)

@Serializable
data class AlcoholicFilterResponse(
    @SerialName("drinks")
    val filters: List<AlcoholicFilterDto>? = null
)

@Serializable
data class AlcoholicFilterDto(
    @SerialName("strAlcoholic")
    val name: String
) 
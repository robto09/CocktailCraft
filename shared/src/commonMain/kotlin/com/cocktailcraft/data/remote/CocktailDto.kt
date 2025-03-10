package com.cocktailcraft.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.cocktailcraft.domain.model.CocktailIngredient

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
    fun getIngredients(): List<CocktailIngredient> {
        val ingredients = mutableListOf<CocktailIngredient>()
        
        // Helper function to get ingredient and measure by index
        fun getIngredientAndMeasure(index: Int): Pair<String?, String?> {
            return when (index) {
                1 -> ingredient1 to measure1
                2 -> ingredient2 to measure2
                3 -> ingredient3 to measure3
                4 -> ingredient4 to measure4
                5 -> ingredient5 to measure5
                6 -> ingredient6 to measure6
                7 -> ingredient7 to measure7
                8 -> ingredient8 to measure8
                9 -> ingredient9 to measure9
                10 -> ingredient10 to measure10
                11 -> ingredient11 to measure11
                12 -> ingredient12 to measure12
                13 -> ingredient13 to measure13
                14 -> ingredient14 to measure14
                15 -> ingredient15 to measure15
                else -> null to null
            }
        }
        
        // Collect all non-null ingredients with their measures
        (1..15).forEach { index ->
            val (ingredient, measure) = getIngredientAndMeasure(index)
            if (!ingredient.isNullOrBlank()) {
                ingredients.add(
                    CocktailIngredient(
                        name = ingredient.trim(),
                        measure = measure?.trim() ?: ""
                    )
                )
            }
        }
        
        return ingredients
    }
}

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
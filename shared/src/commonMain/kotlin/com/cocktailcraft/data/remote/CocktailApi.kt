package com.cocktailcraft.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.http.*

interface CocktailApi {
    suspend fun searchCocktailsByName(name: String): List<CocktailDto>
    suspend fun searchCocktailsByFirstLetter(letter: Char): List<CocktailDto>
    suspend fun getCocktailById(id: String): CocktailDto?
    suspend fun getRandomCocktail(): CocktailDto?
    suspend fun filterByIngredient(ingredient: String): List<CocktailDto>
    suspend fun filterByAlcoholic(alcoholic: Boolean): List<CocktailDto>
    suspend fun filterByCategory(category: String): List<CocktailDto>
    suspend fun filterByGlass(glass: String): List<CocktailDto>
    suspend fun getCategories(): List<CategoryDto>
    suspend fun getGlasses(): List<GlassDto>
    suspend fun getIngredients(): List<IngredientDto>
    suspend fun getAlcoholicFilters(): List<AlcoholicFilterDto>
    
    // New method to check API connectivity
    suspend fun pingApi(): Boolean
}

class CocktailApiImpl(
    private val client: HttpClient
) : CocktailApi {
    
    override suspend fun searchCocktailsByName(name: String): List<CocktailDto> {
        val response = client.get("$BASE_URL/search.php") {
            parameter("s", name)
        }.body<CocktailResponse>()
        
        return response.drinks ?: emptyList()
    }
    
    override suspend fun searchCocktailsByFirstLetter(letter: Char): List<CocktailDto> {
        val response = client.get("$BASE_URL/search.php") {
            parameter("f", letter.toString())
        }.body<CocktailResponse>()
        
        return response.drinks ?: emptyList()
    }
    
    override suspend fun getCocktailById(id: String): CocktailDto? {
        try {
            // Force API call to specifically use the lookup endpoint for full details
            val response = client.get("$BASE_URL/lookup.php") {
                parameter("i", id)
                // Add a timeout to ensure the request doesn't hang
                timeout {
                    requestTimeoutMillis = 10000
                }
            }.body<CocktailResponse>()
            
            val cocktail = response.drinks?.firstOrNull()
            
            if (cocktail == null) {
                return null
            }
            
            return cocktail
        } catch (e: Exception) {
            throw e
        }
    }
    
    override suspend fun getRandomCocktail(): CocktailDto? {
        val response = client.get("$BASE_URL/random.php")
            .body<CocktailResponse>()
        
        return response.drinks?.firstOrNull()
    }
    
    override suspend fun filterByIngredient(ingredient: String): List<CocktailDto> {
        val response = client.get("$BASE_URL/filter.php") {
            parameter("i", ingredient)
        }.body<CocktailResponse>()
        
        return response.drinks ?: emptyList()
    }
    
    override suspend fun filterByAlcoholic(alcoholic: Boolean): List<CocktailDto> {
        val filter = if (alcoholic) "Alcoholic" else "Non_Alcoholic"
        val response = client.get("$BASE_URL/filter.php") {
            parameter("a", filter)
        }.body<CocktailResponse>()
        
        return response.drinks ?: emptyList()
    }
    
    override suspend fun filterByCategory(category: String): List<CocktailDto> {
        try {
            // First get the list of cocktails in this category (only ID, name, and thumbnail)
            val response = client.get("$BASE_URL/filter.php") {
                parameter("c", category)
            }.body<CocktailResponse>()
            
            val basicCocktails = response.drinks ?: emptyList()
            
            // If we need full details (with instructions) for these cocktails later,
            // we'll need to fetch them individually using lookup.php
            return basicCocktails
        } catch (e: Exception) {
            return emptyList()
        }
    }
    
    override suspend fun filterByGlass(glass: String): List<CocktailDto> {
        val response = client.get("$BASE_URL/filter.php") {
            parameter("g", glass)
        }.body<CocktailResponse>()
        
        return response.drinks ?: emptyList()
    }
    
    override suspend fun getCategories(): List<CategoryDto> {
        val response = client.get("$BASE_URL/list.php") {
            parameter("c", "list")
        }.body<CategoryResponse>()
        
        return response.categories ?: emptyList()
    }
    
    override suspend fun getGlasses(): List<GlassDto> {
        val response = client.get("$BASE_URL/list.php") {
            parameter("g", "list")
        }.body<GlassResponse>()
        
        return response.glasses ?: emptyList()
    }
    
    override suspend fun getIngredients(): List<IngredientDto> {
        val response = client.get("$BASE_URL/list.php") {
            parameter("i", "list")
        }.body<IngredientResponse>()
        
        return response.ingredients ?: emptyList()
    }
    
    override suspend fun getAlcoholicFilters(): List<AlcoholicFilterDto> {
        val response = client.get("$BASE_URL/list.php") {
            parameter("a", "list")
        }.body<AlcoholicFilterResponse>()
        
        return response.filters ?: emptyList()
    }
    
    // Implemented the ping method
    override suspend fun pingApi(): Boolean {
        return try {
            // Use a lightweight endpoint for checking connectivity
            val response = client.get("$BASE_URL/random.php")
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
    
    companion object {
        private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1"
        
        fun create(): CocktailApi {
            return CocktailApiImpl(
                client = HttpClient() // Assuming a default client setup
            )
        }
    }
} 
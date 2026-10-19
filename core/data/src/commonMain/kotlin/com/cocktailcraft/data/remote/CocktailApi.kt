package com.cocktailcraft.data.remote

import com.cocktailcraft.domain.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.json.Json

internal interface CocktailApi {
    suspend fun searchCocktailsByName(name: String): List<CocktailDto>
    suspend fun getCocktailById(id: String): CocktailDto?
    suspend fun getRandomCocktail(): CocktailDto?
    suspend fun filterByIngredient(ingredient: String): List<CocktailDto>
    suspend fun filterByAlcoholic(alcoholic: Boolean): List<CocktailDto>
    suspend fun filterByCategory(category: String): List<CocktailDto>
    suspend fun getCategories(): List<CategoryDto>
    suspend fun getIngredients(): List<IngredientDto>
}

internal class CocktailApiImpl(private val client: HttpClient, appConfig: AppConfig, private val json: Json) :
    CocktailApi {

    private val baseUrl: String = appConfig.apiBaseUrl

    // Drinks responses skip ContentNegotiation: see decodeDrinks
    private suspend fun HttpResponse.drinks(): List<CocktailDto>? = json.decodeDrinks(bodyAsText())

    override suspend fun searchCocktailsByName(name: String): List<CocktailDto> {
        val response = client.get("$baseUrl/search.php") {
            parameter("s", name)
        }.drinks()

        return response ?: emptyList()
    }

    override suspend fun getCocktailById(id: String): CocktailDto? {
        // The lookup endpoint returns full details. Timeout comes from the
        // global HttpTimeout plugin (NetworkModule) like every sibling
        // endpoint — the old per-call 10s override silently shrank just this
        // endpoint's budget to a third of the shared one.
        val response = client.get("$baseUrl/lookup.php") {
            parameter("i", id)
        }.drinks()

        return response?.firstOrNull()
    }

    override suspend fun getRandomCocktail(): CocktailDto? {
        val response = client.get("$baseUrl/random.php").drinks()

        return response?.firstOrNull()
    }

    override suspend fun filterByIngredient(ingredient: String): List<CocktailDto> {
        val response = client.get("$baseUrl/filter.php") {
            parameter("i", ingredient)
        }.drinks()

        return response ?: emptyList()
    }

    override suspend fun filterByAlcoholic(alcoholic: Boolean): List<CocktailDto> {
        val filter = if (alcoholic) "Alcoholic" else "Non_Alcoholic"
        val response = client.get("$baseUrl/filter.php") {
            parameter("a", filter)
        }.drinks()

        return response ?: emptyList()
    }

    override suspend fun filterByCategory(category: String): List<CocktailDto> {
        // filter.php only returns partial data (id, name, thumbnail); full
        // details need lookup.php per id. Failures propagate like every other
        // endpoint — swallowing them here turned real API failures into empty
        // "successful" categories upstream.
        val response = client.get("$baseUrl/filter.php") {
            parameter("c", category)
        }.drinks()

        return response ?: emptyList()
    }

    override suspend fun getCategories(): List<CategoryDto> {
        val response = client.get("$baseUrl/list.php") {
            parameter("c", "list")
        }.body<CategoryResponse>()

        return response.categories ?: emptyList()
    }

    override suspend fun getIngredients(): List<IngredientDto> {
        val response = client.get("$baseUrl/list.php") {
            parameter("i", "list")
        }.body<IngredientResponse>()

        return response.ingredients ?: emptyList()
    }
}

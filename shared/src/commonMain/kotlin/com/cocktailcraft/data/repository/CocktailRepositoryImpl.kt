package com.cocktailcraft.data.repository

import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.repository.CocktailRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import com.cocktailcraft.domain.config.AppConfig

class CocktailRepositoryImpl(
    private val api: CocktailApi,
    private val settings: Settings,
    private val appConfig: AppConfig
) : CocktailRepository {

    override suspend fun searchCocktailsByName(name: String): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.searchCocktailsByName(name).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun searchCocktailsByFirstLetter(letter: Char): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.searchCocktailsByFirstLetter(letter).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailById(id: String): Flow<Cocktail?> = flow {
        try {
            val dto = api.getCocktailById(id)
            if (dto != null) {
                emit(mapDtoToCocktail(dto))
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun getRandomCocktail(): Flow<Cocktail?> = flow {
        try {
            val dto = api.getRandomCocktail()
            if (dto != null) {
                emit(mapDtoToCocktail(dto))
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun filterByIngredient(ingredient: String): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByIngredient(ingredient).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun filterByAlcoholic(alcoholic: Boolean): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByAlcoholic(alcoholic).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun filterByCategory(category: String): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByCategory(category).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun filterByGlass(glass: String): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByGlass(glass).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCategories(): Flow<List<String>> = flow {
        try {
            val categories = api.getCategories().map { it.name }
            emit(categories)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getGlasses(): Flow<List<String>> = flow {
        try {
            val glasses = api.getGlasses().map { it.name }
            emit(glasses)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getIngredients(): Flow<List<String>> = flow {
        try {
            val ingredients = api.getIngredients().map { it.name }
            emit(ingredients)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getAlcoholicFilters(): Flow<List<String>> = flow {
        try {
            val filters = api.getAlcoholicFilters().map { it.name }
            emit(filters)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getFavoriteCocktails(): Flow<List<Cocktail>> = flow {
        val favoriteIds = settings.getStringOrNull(appConfig.favoritesStorageKey)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val favoriteCocktails = mutableListOf<Cocktail>()
        
        for (id in favoriteIds) {
            getCocktailById(id).collect { cocktail ->
                if (cocktail != null) {
                    favoriteCocktails.add(cocktail)
                }
            }
        }
        
        emit(favoriteCocktails)
    }

    override suspend fun addToFavorites(cocktail: Cocktail) {
        val currentFavorites = settings.getStringOrNull(appConfig.favoritesStorageKey)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.toMutableList()
            ?: mutableListOf()

        if (!currentFavorites.contains(cocktail.id)) {
            currentFavorites.add(cocktail.id)
            settings.putString(appConfig.favoritesStorageKey, currentFavorites.joinToString(","))
        }
    }

    override suspend fun removeFromFavorites(cocktail: Cocktail) {
        val currentFavorites = settings.getStringOrNull(appConfig.favoritesStorageKey)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.toMutableList()
            ?: mutableListOf()

        currentFavorites.remove(cocktail.id)
        settings.putString(appConfig.favoritesStorageKey, currentFavorites.joinToString(","))
    }

    override suspend fun isCocktailFavorite(id: String): Flow<Boolean> = flow {
        val favoriteIds = settings.getStringOrNull(appConfig.favoritesStorageKey)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        emit(favoriteIds.contains(id))
    }

    override suspend fun getCocktailsSortedByNewest(): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByCategory("Cocktail").map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails.sortedByDescending { it.dateAdded })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailsSortedByPriceLowToHigh(): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByCategory("Cocktail").map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails.sortedBy { it.price })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailsSortedByPriceHighToLow(): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByCategory("Cocktail").map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails.sortedByDescending { it.price })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailsSortedByPopularity(): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByCategory("Cocktail").map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails.sortedByDescending { it.popularity })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByCategory("Cocktail").map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails.filter { it.price in minPrice..maxPrice })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    private fun mapDtoToCocktail(dto: CocktailDto): Cocktail {
        return Cocktail(
            id = dto.id,
            name = dto.name,
            alternateName = dto.alternateName,
            tags = dto.tags?.split(",")?.map { it.trim() },
            category = dto.category,
            iba = dto.iba,
            alcoholic = dto.alcoholic,
            glass = dto.glass,
            instructions = dto.instructions,
            imageUrl = dto.imageUrl,
            ingredients = dto.getIngredients().map { 
                CocktailIngredient(name = it.name, measure = it.measure) 
            },
            imageSource = dto.imageSource,
            imageAttribution = dto.imageAttribution,
            creativeCommonsConfirmed = dto.creativeCommonsConfirmed?.equals("Yes", ignoreCase = true),
            dateModified = dto.dateModified,
            price = 10.0, // Fixed price for all cocktails
            // Additional fields with default values
            inStock = true,
            stockCount = 50,
            rating = 4.5f,
            popularity = calculatePopularity(dto),
            dateAdded = System.currentTimeMillis()
        )
    }
    
    private fun calculatePopularity(dto: CocktailDto): Int {
        // Calculate popularity based on various factors
        var popularity = 0
        
        // More ingredients might indicate a more complex and popular cocktail
        popularity += dto.getIngredients().size * 5
        
        // If it's part of IBA (International Bartenders Association), it's likely more popular
        if (!dto.iba.isNullOrBlank()) {
            popularity += 20
        }
        
        // If it has tags, it might be more popular
        if (!dto.tags.isNullOrBlank()) {
            popularity += dto.tags.split(",").size * 10
        }
        
        return popularity
    }

    override fun getCocktailImageUrl(cocktail: Cocktail): String {
        // First try to use the image URL provided by the API
        return cocktail.imageUrl ?: 
            // Otherwise, construct a URL for the ingredient based on the cocktail name
            "${appConfig.imageBaseUrl}/${appConfig.ingredientsImagePath}/${cocktail.name.replace(" ", "-").lowercase()}.png"
    }
} 
import CoreData
import shared

class PersistenceController {
    static let shared = PersistenceController()
    
    let container: NSPersistentContainer
    
    init(inMemory: Bool = false) {
        container = NSPersistentContainer(name: "CocktailCraft")
        
        if inMemory {
            container.persistentStoreDescriptions.first?.url = URL(fileURLWithPath: "/dev/null")
        }
        
        container.loadPersistentStores { description, error in
            if let error = error {
                fatalError("Error: \(error.localizedDescription)")
            }
        }
        
        container.viewContext.mergePolicy = NSMergeByPropertyObjectTrumpMergePolicy
        container.viewContext.automaticallyMergesChangesFromParent = true
    }
    
    // MARK: - Save Context
    
    func saveContext() {
        let context = container.viewContext
        if context.hasChanges {
            do {
                try context.save()
            } catch {
                print("Error saving context: \(error)")
            }
        }
    }
    
    // MARK: - Cocktails
    
    func saveCocktails(_ cocktails: [Cocktail]) {
        let context = container.viewContext
        
        // Delete existing cocktails
        let fetchRequest: NSFetchRequest<NSFetchRequestResult> = CachedCocktail.fetchRequest()
        let deleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        try? context.execute(deleteRequest)
        
        // Save new cocktails
        for cocktail in cocktails {
            let cached = CachedCocktail(context: context)
            cached.id = cocktail.id
            cached.name = cocktail.name
            cached.category = cocktail.category
            cached.cocktailDescription = cocktail.description_
            cached.imageUrl = cocktail.imageUrl
            cached.price = cocktail.price
            cached.rating = cocktail.rating
            cached.instructions = cocktail.instructions
            cached.glassType = cocktail.glassType
            cached.garnish = cocktail.garnish
            cached.timestamp = Date()
            
            // Save ingredients
            for ingredient in cocktail.ingredients {
                let cachedIngredient = CachedIngredient(context: context)
                cachedIngredient.name = ingredient.name
                cachedIngredient.amount = ingredient.amount
                cachedIngredient.unit = ingredient.unit
                cachedIngredient.isOptional = ingredient.isOptional
                cachedIngredient.cocktail = cached
            }
            
            // Save reviews
            for review in cocktail.reviews {
                let cachedReview = CachedReview(context: context)
                cachedReview.id = review.id
                cachedReview.userId = review.userId
                cachedReview.userName = review.userName
                cachedReview.rating = review.rating
                cachedReview.comment = review.comment
                cachedReview.likes = Int32(review.likes)
                cachedReview.date = Date(timeIntervalSince1970: TimeInterval(review.date / 1000))
                cachedReview.cocktail = cached
            }
        }
        
        saveContext()
    }
    
    func getCocktails() -> [Cocktail] {
        let context = container.viewContext
        let fetchRequest: NSFetchRequest<CachedCocktail> = CachedCocktail.fetchRequest()
        
        do {
            let cachedCocktails = try context.fetch(fetchRequest)
            return cachedCocktails.map { cached in
                Cocktail(
                    id: cached.id ?? "",
                    name: cached.name ?? "",
                    category: cached.category,
                    description_: cached.cocktailDescription,
                    imageUrl: cached.imageUrl,
                    price: cached.price,
                    rating: cached.rating,
                    ingredients: (cached.ingredients?.allObjects as? [CachedIngredient])?.map { ingredient in
                        CocktailIngredient(
                            name: ingredient.name ?? "",
                            amount: ingredient.amount,
                            unit: ingredient.unit ?? "",
                            isOptional: ingredient.isOptional
                        )
                    } ?? [],
                    instructions: cached.instructions,
                    glassType: cached.glassType,
                    garnish: cached.garnish,
                    reviews: (cached.reviews?.allObjects as? [CachedReview])?.map { review in
                        Review(
                            id: review.id ?? "",
                            userId: review.userId ?? "",
                            userName: review.userName ?? "",
                            rating: review.rating,
                            comment: review.comment,
                            likes: Int(review.likes),
                            date: Int64(review.date?.timeIntervalSince1970 ?? 0) * 1000
                        )
                    } ?? []
                )
            }
        } catch {
            print("Error fetching cocktails: \(error)")
            return []
        }
    }
    
    // MARK: - Cart
    
    func saveCart(_ items: [CocktailCartItem]) {
        let context = container.viewContext
        
        // Delete existing items
        let fetchRequest: NSFetchRequest<NSFetchRequestResult> = CachedCartItem.fetchRequest()
        let deleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        try? context.execute(deleteRequest)
        
        // Save new items
        for item in items {
            let cached = CachedCartItem(context: context)
            cached.cocktailId = item.cocktailId
            cached.name = item.name
            cached.price = item.price
            cached.quantity = Int32(item.quantity)
            cached.imageUrl = item.imageUrl
            cached.specialInstructions = item.specialInstructions
            cached.timestamp = Date()
        }
        
        saveContext()
    }
    
    func getCart() -> [CocktailCartItem] {
        let context = container.viewContext
        let fetchRequest: NSFetchRequest<CachedCartItem> = CachedCartItem.fetchRequest()
        
        do {
            let cachedItems = try context.fetch(fetchRequest)
            return cachedItems.map { cached in
                CocktailCartItem(
                    cocktailId: cached.cocktailId ?? "",
                    name: cached.name ?? "",
                    price: cached.price,
                    quantity: Int(cached.quantity),
                    imageUrl: cached.imageUrl,
                    specialInstructions: cached.specialInstructions
                )
            }
        } catch {
            print("Error fetching cart: \(error)")
            return []
        }
    }
    
    // MARK: - Favorites
    
    func saveFavorites(_ cocktails: [Cocktail]) {
        let context = container.viewContext
        
        // Delete existing favorites
        let fetchRequest: NSFetchRequest<NSFetchRequestResult> = CachedFavorite.fetchRequest()
        let deleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        try? context.execute(deleteRequest)
        
        // Save new favorites
        for cocktail in cocktails {
            let cached = CachedFavorite(context: context)
            cached.cocktailId = cocktail.id
            cached.timestamp = Date()
        }
        
        saveContext()
    }
    
    func getFavorites() -> [String] {
        let context = container.viewContext
        let fetchRequest: NSFetchRequest<CachedFavorite> = CachedFavorite.fetchRequest()
        
        do {
            let cachedFavorites = try context.fetch(fetchRequest)
            return cachedFavorites.compactMap { $0.cocktailId }
        } catch {
            print("Error fetching favorites: \(error)")
            return []
        }
    }
    
    // MARK: - Orders
    
    func saveOrders(_ orders: [Order]) {
        let context = container.viewContext
        
        // Delete existing orders
        let fetchRequest: NSFetchRequest<NSFetchRequestResult> = CachedOrder.fetchRequest()
        let deleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        try? context.execute(deleteRequest)
        
        // Save new orders
        for order in orders {
            let cached = CachedOrder(context: context)
            cached.id = order.id
            cached.status = order.status.name
            cached.totalAmount = order.totalAmount
            cached.createdAt = Date(timeIntervalSince1970: TimeInterval(order.createdAt / 1000))
            cached.deliveryAddress = order.deliveryAddress
            cached.paymentMethod = order.paymentMethod
            cached.specialInstructions = order.specialInstructions
            cached.timestamp = Date()
            
            // Save order items
            for item in order.items {
                let cachedItem = CachedOrderItem(context: context)
                cachedItem.cocktailId = item.cocktailId
                cachedItem.name = item.name
                cachedItem.price = item.price
                cachedItem.quantity = Int32(item.quantity)
                cachedItem.order = cached
            }
        }
        
        saveContext()
    }
    
    func getOrders() -> [Order] {
        let context = container.viewContext
        let fetchRequest: NSFetchRequest<CachedOrder> = CachedOrder.fetchRequest()
        
        do {
            let cachedOrders = try context.fetch(fetchRequest)
            return cachedOrders.map { cached in
                Order(
                    id: cached.id ?? "",
                    status: OrderStatus.valueOf(value: cached.status ?? "PENDING"),
                    items: (cached.items?.allObjects as? [CachedOrderItem])?.map { item in
                        CocktailCartItem(
                            cocktailId: item.cocktailId ?? "",
                            name: item.name ?? "",
                            price: item.price,
                            quantity: Int(item.quantity)
                        )
                    } ?? [],
                    totalAmount: cached.totalAmount,
                    createdAt: Int64(cached.createdAt?.timeIntervalSince1970 ?? 0) * 1000,
                    deliveryAddress: cached.deliveryAddress,
                    paymentMethod: cached.paymentMethod,
                    specialInstructions: cached.specialInstructions
                )
            }
        } catch {
            print("Error fetching orders: \(error)")
            return []
        }
    }
    
    // MARK: - Sync Metadata
    
    func getLastSyncTime(for key: String) -> Date? {
        let context = container.viewContext
        let fetchRequest: NSFetchRequest<SyncMetadata> = SyncMetadata.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "key == %@", key)
        
        do {
            let metadata = try context.fetch(fetchRequest).first
            return metadata?.lastSyncTime
        } catch {
            print("Error fetching sync metadata: \(error)")
            return nil
        }
    }
    
    func updateLastSyncTime(for key: String) {
        let context = container.viewContext
        let fetchRequest: NSFetchRequest<SyncMetadata> = SyncMetadata.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "key == %@", key)
        
        do {
            let metadata = try context.fetch(fetchRequest).first ?? SyncMetadata(context: context)
            metadata.key = key
            metadata.lastSyncTime = Date()
            saveContext()
        } catch {
            print("Error updating sync metadata: \(error)")
        }
    }
    
    // MARK: - Clear All Data
    
    func clearAllData() {
        let context = container.viewContext
        let entities = container.managedObjectModel.entities
        
        for entity in entities {
            if let name = entity.name {
                let fetchRequest: NSFetchRequest<NSFetchRequestResult> = NSFetchRequest(entityName: name)
                let deleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
                try? context.execute(deleteRequest)
            }
        }
        
        saveContext()
    }
}
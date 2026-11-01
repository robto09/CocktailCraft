# Sequence Diagram - Place Order

```mermaid
sequenceDiagram
    actor User
    participant UI as CartScreen / CartView
    participant CartVM as SharedCartViewModel
    participant OrderVM as SharedOrderViewModel
    participant UC as PlaceOrderUseCase
    participant CartRepo as CartRepository
    participant OrderRepo as OrderRepository
    participant DB as SQLDelight database (cart_item, customer_order, customer_order_item)

    %% View Cart
    Note over User, DB: View cart (the signed-in user's, or the guest's)
    User->>UI: Open the Cart tab
    UI->>CartVM: collect uiState
    CartVM->>CartRepo: observeCartItems()
    CartRepo->>DB: select the current owner's cart_item rows (joined to cached_cocktail)
    DB-->>CartRepo: rows, again after every write to the cart
    CartRepo-->>CartVM: Flow<List<CocktailCartItem>>
    CartVM-->>UI: uiState (items, total, finalTotal)
    UI-->>User: Cart items and total

    %% Place Order - Success Path
    Note over User, DB: Place order: success
    User->>UI: Checkout, then confirm
    UI->>OrderVM: placeOrder(cartItems)
    activate OrderVM
    OrderVM->>OrderVM: uiState: isPlacingOrder = true (a second call meanwhile returns false)
    OrderVM->>UC: invoke(cartItems)
    UC->>UC: Build the Order (new id, date, items, total from DeliveryPolicy, PROCESSING)
    UC->>OrderRepo: placeOrderAndClearCart(order)
    OrderRepo->>DB: one transaction: insert the order and its items, delete the owner's cart_item rows
    DB-->>OrderRepo: committed
    OrderRepo-->>UC: Result.Success
    UC-->>OrderVM: Result.Success(order)
    OrderVM->>OrderVM: uiState: currentOrder = order, isPlacingOrder = false
    OrderVM-->>UI: true
    deactivate OrderVM
    DB-->>CartVM: cart flow re-emits: empty
    DB-->>OrderVM: orders flow re-emits with the new order
    UI->>UI: Navigate to the order list (Android) / select the Orders tab (iOS)
    UI-->>User: Order list showing the new order

    %% Place Order - Error Path
    Note over User, DB: Place order: failure
    User->>UI: Checkout, then confirm
    UI->>OrderVM: placeOrder(cartItems)
    activate OrderVM
    OrderVM->>UC: invoke(cartItems)
    UC->>OrderRepo: placeOrderAndClearCart(order)
    OrderRepo->>DB: transaction fails and rolls back (order not stored, cart unchanged)
    OrderRepo-->>UC: Result.Error
    UC-->>OrderVM: Result.Error
    OrderVM->>OrderVM: setError(order failed title, ErrorHandler message)
    OrderVM-->>UI: false; error StateFlow emits
    deactivate OrderVM
    UI-->>User: Error dialog; the cart is still there to try again
```

How placing an order works:

1. **View cart**: the cart screen collects `SharedCartViewModel.uiState`, fed by `CartRepository.observeCartItems()`, a SQLDelight query flow over the current owner's `cart_item` rows that re-emits after every write.
2. **Success**: the screen calls `SharedOrderViewModel.placeOrder(cartItems)` (a `suspend` function run in the ViewModel's scope). `PlaceOrderUseCase` builds the `Order`, computing the total itself, and `OrderRepository.placeOrderAndClearCart(order)` saves the order and empties the cart in one transaction (`OrderRepositoryImpl`). The screen never clears the cart itself: the cart and order flows simply re-emit. On `true` the screen moves to the order list.
3. **Failure**: nothing is stored and the cart is unchanged; the ViewModel reports the failure with `setError`, and the screen shows it from the shared `error` StateFlow.
4. **Owner**: the cart read, the order insert and the cart delete all use the current owner (the signed-in user, or the guest), so an order belongs to whoever placed it (see `UserDataOwnership.kt`).

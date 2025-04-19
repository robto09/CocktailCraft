# Sequence Diagram - Place Order

```mermaid
sequenceDiagram
    actor User
    participant UI as CartScreen
    participant VM as CartViewModel
    participant UC as PlaceOrderUseCase
    participant Repo as OrderRepository
    participant Storage as LocalStorage
    
    %% View Cart
    Note over User, Storage: View Cart
    User->>UI: View cart
    activate UI
    UI->>VM: loadCartItems()
    activate VM
    VM->>Repo: getCartItems()
    activate Repo
    Repo->>Storage: Read cart items
    activate Storage
    Storage-->>Repo: Return cart items
    deactivate Storage
    Repo-->>VM: Return cart items
    deactivate Repo
    VM->>Repo: getCartTotal()
    activate Repo
    Repo->>Storage: Calculate total
    activate Storage
    Storage-->>Repo: Return total
    deactivate Storage
    Repo-->>VM: Return total
    deactivate Repo
    VM-->>UI: Update UI with cart items and total
    deactivate VM
    UI-->>User: Display cart items and total
    deactivate UI
    
    %% Place Order
    Note over User, Storage: Place Order
    User->>UI: Click "Place Order"
    activate UI
    UI->>VM: placeOrder()
    activate VM
    VM->>UC: invoke(cartItems, totalPrice)
    activate UC
    UC->>UC: Create Order object
    UC->>Repo: addOrder(order)
    activate Repo
    Repo->>Storage: Save order
    activate Storage
    Storage-->>Repo: Confirm save
    deactivate Storage
    Repo-->>UC: Return success
    deactivate Repo
    UC-->>VM: Return Result.Success(order)
    deactivate UC
    VM->>Repo: clearCart()
    activate Repo
    Repo->>Storage: Clear cart items
    activate Storage
    Storage-->>Repo: Confirm clear
    deactivate Storage
    Repo-->>VM: Return success
    deactivate Repo
    VM-->>UI: Order placed successfully
    deactivate VM
    UI-->>User: Show order confirmation
    deactivate UI
    
    %% Navigate to Order History
    Note over User, Storage: Navigate to Order History
    User->>UI: Click "View Orders"
    activate UI
    UI->>NavigationManager: navigateToOrderList()
    NavigationManager->>OrderListScreen: Navigate
    OrderListScreen-->>User: Display order history
    deactivate UI
```

This sequence diagram illustrates the flow of interactions when a user places an order in the CocktailCraft application, including viewing the cart, placing the order, and navigating to the order history.
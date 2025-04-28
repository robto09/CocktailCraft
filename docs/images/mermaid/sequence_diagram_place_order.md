# Sequence Diagram - Place Order

```mermaid
sequenceDiagram
    actor User
    participant UI as CartScreen
    participant ErrorUI as ErrorDialog
    participant VM as CartViewModel
    participant BaseVM as BaseViewModel
    participant Network as NetworkMonitor
    participant UC as PlaceOrderUseCase
    participant Repo as OrderRepository
    participant Cache as CocktailCache
    participant Storage as LocalStorage
    participant ErrorUtils as ErrorUtils

    %% Check Network Status
    Note over User, ErrorUtils: Check Network Status
    VM->>Network: isOnline.collect()
    activate Network
    Network-->>VM: Network status (online/offline)
    deactivate Network

    %% View Cart
    Note over User, ErrorUtils: View Cart
    User->>UI: View cart
    activate UI
    UI->>VM: loadCartItems()
    activate VM
    VM->>VM: setLoading(true)
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
    VM->>VM: setLoading(false)
    VM-->>UI: Update UI with cart items and total
    deactivate VM
    UI-->>User: Display cart items and total
    deactivate UI

    %% Place Order - Success Path
    Note over User, ErrorUtils: Place Order - Success Path
    User->>UI: Click "Place Order"
    activate UI
    UI->>VM: placeOrder()
    activate VM
    VM->>BaseVM: executeWithErrorHandling()
    activate BaseVM
    BaseVM->>BaseVM: setLoading(true)
    BaseVM->>UC: invoke(cartItems, totalPrice)
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
    UC-->>BaseVM: Return Result.Success(order)
    deactivate UC
    BaseVM->>Repo: clearCart()
    activate Repo
    Repo->>Storage: Clear cart items
    activate Storage
    Storage-->>Repo: Confirm clear
    deactivate Storage
    Repo-->>BaseVM: Return success
    deactivate Repo
    BaseVM->>BaseVM: setLoading(false)
    BaseVM-->>VM: Order placed successfully
    deactivate BaseVM
    VM-->>UI: Order placed successfully
    deactivate VM
    UI-->>User: Show order confirmation
    deactivate UI

    %% Place Order - Error Path
    Note over User, ErrorUtils: Place Order - Error Path
    User->>UI: Click "Place Order" (with network error)
    activate UI
    UI->>VM: placeOrder()
    activate VM
    VM->>BaseVM: executeWithErrorHandling()
    activate BaseVM
    BaseVM->>BaseVM: setLoading(true)
    BaseVM->>UC: invoke(cartItems, totalPrice)
    activate UC
    UC->>UC: Create Order object
    UC->>Repo: addOrder(order)
    activate Repo
    Repo--xRepo: Network error occurs
    Repo-->>UC: Throw exception
    deactivate Repo
    UC-->>BaseVM: Throw exception
    deactivate UC
    BaseVM->>ErrorUtils: getErrorFromException()
    activate ErrorUtils
    ErrorUtils-->>BaseVM: Return UserFriendlyError
    deactivate ErrorUtils
    BaseVM->>BaseVM: setError(userFriendlyError)
    BaseVM->>BaseVM: setLoading(false)
    BaseVM-->>VM: Error event
    deactivate BaseVM
    VM-->>UI: Show error
    deactivate VM
    UI->>ErrorUI: Show error dialog
    activate ErrorUI
    ErrorUI-->>User: Display error with retry option
    User->>ErrorUI: Click "Retry"
    ErrorUI->>UI: Retry action
    deactivate ErrorUI
    UI->>VM: placeOrder() (retry)
    deactivate UI

    %% Navigate to Order History
    Note over User, ErrorUtils: Navigate to Order History
    User->>UI: Click "View Orders"
    activate UI
    UI->>NavigationManager: navigateToOrderList()
    NavigationManager->>OrderListScreen: Navigate
    OrderListScreen-->>User: Display order history
    deactivate UI
```

This sequence diagram illustrates the flow of interactions when a user places an order in the CocktailCraft application, including:

1. **Network Status Check**: Monitoring network connectivity before operations
2. **View Cart**: Loading and displaying cart items with loading state management
3. **Place Order - Success Path**: The happy path when placing an order succeeds
   - Using BaseViewModel's error handling wrapper
   - Managing loading state
   - Clearing cart after successful order
4. **Place Order - Error Path**: The error handling path when network issues occur
   - Error conversion to user-friendly format
   - Displaying error dialog with retry option
   - Retry flow
5. **Navigation**: Moving to order history after placing an order

The diagram shows how the application handles both successful operations and error scenarios with proper error handling and user feedback.
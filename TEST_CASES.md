# Detailed Test Cases

## ViewModel Tests

### CartViewModelTest
- **`initial state should load cart items and total`**: Validates that the CartViewModel properly initializes by loading cart items and calculating the total price.
- **`addToCart should add item to cart`**: Verifies that adding an item to the cart correctly calls the repository method and refreshes the cart data.
- **`removeFromCart should remove item from cart`**: Ensures that removing an item from the cart correctly calls the repository method and updates the cart state.
- **`updateQuantity should update item quantity in cart`**: Tests that updating an item's quantity properly calls the repository method with the correct parameters.
- **`clearCart should clear all items from cart`**: Confirms that the clearCart function properly calls the repository method and refreshes the cart.
- **`error handling should set error state`**: Validates that the ViewModel correctly handles and exposes errors that occur during cart operations.

### ProfileViewModelTest
- **`initial state should be not signed in`**: Verifies that the initial state of the ProfileViewModel correctly reflects a user not being signed in.
- **`initial user should be null`**: Ensures that the user object is initially null when no user is signed in.
- **`sign in success should update state`**: Tests that a successful sign-in updates the signed-in state and user object.
- **`sign in failure should set error state`**: Validates error handling during the sign-in process.
- **`sign out should update state`**: Ensures that signing out properly updates the authentication state and clears user data.
- **`update profile should call repository and refresh user data`**: Tests that profile updates are properly handled and user data is refreshed.

### OrderViewModelTest
- **`place order should create order from cart items`**: Verifies that placing an order correctly creates an order from the current cart items.
- **`get orders should load user orders`**: Tests that the ViewModel correctly loads and exposes user orders.
- **`cancel order should update order status`**: Validates that canceling an order updates its status appropriately.
- **`track order should return order details`**: Ensures that order tracking functionality returns the correct order information.

## Domain Layer Tests

### PlaceOrderUseCaseTest
- **`invoke should create order and add to repository`**: Tests that the PlaceOrderUseCase correctly creates an order from cart items and adds it to the repository.
- **`invoke should handle empty cart`**: Verifies appropriate handling of empty cart situations.
- **`invoke should handle repository errors`**: Tests error handling when repository operations fail.
- **`generated order should have correct properties`**: Ensures that generated orders have the correct properties including ID format, items list, and pricing.

## Repository Tests

### AuthRepositoryImplTest
- **`signUp should return success when email is not taken`**: Validates that user registration works when the email is available.
- **`signUp should return failure when email is already taken`**: Tests that registration fails appropriately when the email is already in use.
- **`signIn should return success with valid credentials`**: Ensures authentication works with correct credentials.
- **`signIn should return failure with invalid credentials`**: Verifies that authentication fails with incorrect credentials.
- **`signOut should clear current user`**: Tests that the sign-out process correctly clears user session data.
- **`isUserSignedIn should return true when user is signed in`**: Validates the signed-in state detection.
- **`getCurrentUser should return correct user data`**: Ensures that the repository correctly returns the current user's data.
- **`updateProfile should update user information`**: Tests that profile updates are correctly stored.

## Running Tests
To run the tests, you can use:

- **Android Studio**:
  - Right-click on a test class or method and select "Run"
  - Navigate to the test directory, right-click and select "Run Tests in..."

- **Command Line**:
  ```bash
  ./gradlew test        # Run all tests
  ./gradlew :androidApp:testDebugUnitTest  # Run Android unit tests
  ```
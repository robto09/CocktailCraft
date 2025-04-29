# Use Case Diagram

```mermaid
graph LR
    User((User))
    System((System))

    subgraph CocktailCraft["CocktailCraft Application"]
        %% Home Screen Use Cases
        UC1[Browse Cocktails]
        UC2[Search Cocktails]
        UC3[Basic Filter Cocktails]
        UC4[Advanced Filter Cocktails]
        UC5[Load Filter Options]
        UC6[Sort Cocktails]
        UC7[View Cocktail Details]

        %% Cart Use Cases
        UC8[Add to Cart]
        UC9[View Cart]
        UC10[Update Quantity]
        UC11[Remove from Cart]
        UC12[Place Order]

        %% Favorites Use Cases
        UC13[Add to Favorites]
        UC14[View Favorites]
        UC15[Remove from Favorites]

        %% Order Use Cases
        UC16[View Order History]
        UC17[View Order Details]

        %% Profile Use Cases
        UC18[View Profile]
        UC19[Edit Profile]
        UC20[Login/Logout]

        %% Review Use Cases
        UC21[Write Review]
        UC22[View Reviews]

        %% Theme Use Cases
        UC23[Toggle Dark Mode]
        UC24[Toggle System Theme]

        %% Offline Mode Use Cases
        UC25[Enable/Disable Offline Mode]
        UC26[View Cached Cocktails]
        UC27[Clear Cache]

        %% Recommendation Use Cases
        UC28[View Similar Cocktails]
        UC29[Get Personalized Recommendations]

        %% Error Handling Use Cases
        UC30[Handle Network Errors]
        UC31[Retry Failed Operations]
    end

    %% User Associations
    User --> UC1
    User --> UC2
    User --> UC3
    User --> UC4
    User --> UC6
    User --> UC7
    User --> UC8
    User --> UC9
    User --> UC10
    User --> UC11
    User --> UC12
    User --> UC13
    User --> UC14
    User --> UC15
    User --> UC16
    User --> UC17
    User --> UC18
    User --> UC19
    User --> UC20
    User --> UC21
    User --> UC22
    User --> UC23
    User --> UC24
    User --> UC25
    User --> UC26
    User --> UC27
    User --> UC28
    User --> UC31

    %% System handles filter options loading and advanced filtering
    System --> UC4
    System --> UC5

    %% System Associations
    System --> UC29
    System --> UC30

    %% Include/Extend Relationships
    %% Include/Extend Relationships
    UC7 --> UC8
    UC7 --> UC13
    UC7 --> UC21
    UC7 --> UC22
    UC7 --> UC28
    UC9 --> UC10
    UC9 --> UC11
    UC9 --> UC12
    UC12 --> UC9
    UC14 --> UC15
    UC17 --> UC16
    UC18 --> UC23
    UC18 --> UC24
    UC1 --> UC30
    UC2 --> UC30
    UC3 --> UC30
    UC4 --> UC30
    UC7 --> UC30
    UC12 --> UC30
    UC30 --> UC31
    UC1 --> UC25
    UC1 --> UC26
    UC26 --> UC27

    %% Advanced Search Relationships
    UC2 --> UC4
    UC4 --> UC5
    UC3 --> UC5
```

This diagram illustrates the main user interactions with the CocktailCraft system, including:

1. **Core Functionality**:
   - Browsing cocktails
   - Basic and advanced search/filtering
   - Loading and managing filter options
   - Viewing details
   - Managing cart
   - Placing orders
   - Managing favorites
   - Writing reviews
2. **Theme Management**: Toggling dark mode and system theme integration
3. **Offline Mode**: Enabling/disabling offline mode, viewing cached cocktails, and clearing cache
4. **Recommendations**: Viewing similar cocktails and getting personalized recommendations
5. **Error Handling**: Handling network errors and retrying failed operations

The diagram shows both user-initiated use cases and system-initiated use cases, as well as the relationships between them.
# Use Case Diagram

```mermaid
graph LR
    User((User))
    System((System))

    subgraph CocktailCraft["CocktailCraft Application"]
        %% Home Screen Use Cases
        UC1[Browse Cocktails]
        UC2[Search Cocktails]
        UC3[Filter Cocktails]
        UC4[Sort Cocktails]
        UC5[View Cocktail Details]

        %% Cart Use Cases
        UC6[Add to Cart]
        UC7[View Cart]
        UC8[Update Quantity]
        UC9[Remove from Cart]
        UC10[Place Order]

        %% Favorites Use Cases
        UC11[Add to Favorites]
        UC12[View Favorites]
        UC13[Remove from Favorites]

        %% Order Use Cases
        UC14[View Order History]
        UC15[View Order Details]

        %% Profile Use Cases
        UC16[View Profile]
        UC17[Edit Profile]
        UC18[Login/Logout]

        %% Review Use Cases
        UC19[Write Review]
        UC20[View Reviews]

        %% Theme Use Cases
        UC21[Toggle Dark Mode]
        UC22[Toggle System Theme]

        %% Offline Mode Use Cases
        UC23[Enable/Disable Offline Mode]
        UC24[View Cached Cocktails]
        UC25[Clear Cache]

        %% Recommendation Use Cases
        UC26[View Similar Cocktails]
        UC27[Get Personalized Recommendations]

        %% Error Handling Use Cases
        UC28[Handle Network Errors]
        UC29[Retry Failed Operations]
    end

    %% User Associations
    User --> UC1
    User --> UC2
    User --> UC3
    User --> UC4
    User --> UC5
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
    User --> UC29

    %% System Associations
    System --> UC27
    System --> UC28

    %% Include/Extend Relationships
    UC5 --> UC6
    UC5 --> UC11
    UC5 --> UC19
    UC5 --> UC20
    UC5 --> UC26
    UC7 --> UC8
    UC7 --> UC9
    UC7 --> UC10
    UC10 --> UC7
    UC12 --> UC13
    UC15 --> UC14
    UC16 --> UC21
    UC16 --> UC22
    UC1 --> UC28
    UC2 --> UC28
    UC3 --> UC28
    UC5 --> UC28
    UC10 --> UC28
    UC28 --> UC29
    UC1 --> UC23
    UC1 --> UC24
    UC24 --> UC25
```

This diagram illustrates the main user interactions with the CocktailCraft system, including:

1. **Core Functionality**: Browsing cocktails, searching, filtering, viewing details, managing cart, placing orders, managing favorites, and writing reviews
2. **Theme Management**: Toggling dark mode and system theme integration
3. **Offline Mode**: Enabling/disabling offline mode, viewing cached cocktails, and clearing cache
4. **Recommendations**: Viewing similar cocktails and getting personalized recommendations
5. **Error Handling**: Handling network errors and retrying failed operations

The diagram shows both user-initiated use cases and system-initiated use cases, as well as the relationships between them.
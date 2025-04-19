# Use Case Diagram

```mermaid
graph LR
    User((User))
    
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
    
    %% Include/Extend Relationships
    UC5 --> UC6
    UC5 --> UC11
    UC5 --> UC19
    UC5 --> UC20
    UC7 --> UC8
    UC7 --> UC9
    UC7 --> UC10
    UC10 --> UC7
    UC12 --> UC13
    UC15 --> UC14
```

This diagram illustrates the main user interactions with the CocktailCraft system, including browsing cocktails, searching, filtering, viewing details, managing cart, placing orders, managing favorites, and writing reviews.
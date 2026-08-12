# Restaurant & Catalog Service

NomNom Food Delivery 

## Overview

The Restaurant & Catalog Service manages restaurant registration, menu management, and order validation. It is one of 4 independent microservices in the NomNom system.

**Port:** `8081`
**Database:** H2 in-memory (`jdbc:h2:mem:nomnom_restaurant`)

## Responsibilities

- Register and manage restaurant profiles
- Manage menu items (add, update, remove)
- Consume `order.placed` event from Order Service via Kafka
- Validate item availability and publish `order.confirmed` (CONFIRMED or REJECTED) back to Kafka

## Kafka Events

| Topic | Role | Description |
|---|---|---|
| `order.placed` | **Consumes** | Validates items when Order Service places an order |
| `order.confirmed` | **Publishes** | Confirms or rejects the order after validation |
| `restaurant.registered` | **Publishes** | Published when a new restaurant is registered |

## How to Run

### Prerequisites
- Java 21
- Maven
- Docker Desktop (for Kafka)

### Step 1 — Start Kafka
From the root of the project (where `docker-compose.yml` is):
```bash
docker-compose up -d
```

### Step 2 — Run the service
```bash
cd restaurant-service
./mvnw spring-boot:run
```

Service starts at: `http://localhost:8081`

### Step 3 — Test with REST Client
Open `restaurant-api.http` in VS Code with the REST Client extension (humao.rest-client) and click **Send Request** above any request.

## H2 Console
Open in browser: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:nomnom_restaurant`
- Username: `sa`
- Password: *(leave blank)*

## Sample Inputs

### Register a Restaurant
```
POST http://localhost:8081/restaurants
Content-Type: application/json

{
  "name": "Nasi Lemak Wangi",
  "description": "Authentic Malaysian nasi lemak since 1985",
  "cuisine": "Malaysian",
  "ownerName": "Encik Razif",
  "contactPhone": "0321234567",
  "contactEmail": "nasilemakwangi@email.com",
  "openingHours": "Mon-Sun 07:00-15:00",
  "address": {
    "street": "Jalan Ampang",
    "postcode": "50450",
    "city": "Kuala Lumpur",
    "state": "WP Kuala Lumpur",
    "country": "Malaysia"
  }
}
```

### Add a Menu Item
```
POST http://localhost:8081/restaurants/{restaurantCode}/menu
Content-Type: application/json

{
  "name": "Nasi Lemak Special",
  "description": "Rice with sambal, egg, anchovies and peanuts",
  "price": 12.90,
  "category": "Main Course"
}
```

### Get All Restaurants
```
GET http://localhost:8081/restaurants
```

### Get Menu for a Restaurant
```
GET http://localhost:8081/restaurants/{restaurantCode}/menu
```

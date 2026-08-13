# NomNom – Microservice Food Delivery System

## 1. Project Overview

NomNom is a prototype food delivery platform developed using a microservice architecture. The system allows customers to register an account, browse restaurant menus, place orders, and track orders through confirmation and delivery.

The system consists of four independent Spring Boot microservices:

- Customer Service
- Restaurant Service
- Order Service
- Delivery Service

Each microservice is responsible for a specific business capability and owns its own H2 database. The services communicate using synchronous REST APIs and asynchronous Apache Kafka events.

---

## 2. Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | Application development |
| Spring Boot | Microservice development |
| Apache Kafka | Asynchronous event-driven communication |
| H2 Database | Database for each microservice |
| Docker / Docker Compose | Kafka and project environment |
| Maven | Build and run Spring Boot applications |
| HTML / JavaScript | Frontend interface |
| VS Code Live Server | Running the frontend |

---

## 3. Project Structure

```text
NomNom/
│
├── customer-service/
├── restaurant-service/
├── order-service/
├── delivery-service/
├── nomnom-frontend/
│
├── docker-compose.yml
└── README.md
````

### Customer Service

Responsible for:

* Customer registration
* Customer profile management
* Customer account status
* Customer address management
* Customer validation for order placement
* Loyalty point management after successful delivery

### Restaurant Service

Responsible for:

* Restaurant registration
* Restaurant profile management
* Menu management
* Menu item availability
* Validation of ordered menu items

### Order Service

Responsible for:

* Order creation
* Order item management
* Order pricing
* Order status lifecycle
* Publishing and consuming order-related events

### Delivery Service

Responsible for:

* Rider registration
* Rider assignment
* Delivery creation
* Delivery status tracking
* Publishing delivery-related events

---

## 4. Service Ports

| Service            | Port | Base URL                                       |
| ------------------ | ---: | ---------------------------------------------- |
| Customer Service   | 8080 | [http://localhost:8080](http://localhost:8080) |
| Restaurant Service | 8081 | [http://localhost:8081](http://localhost:8081) |
| Order Service      | 8082 | [http://localhost:8082](http://localhost:8082) |
| Delivery Service   | 8083 | [http://localhost:8083](http://localhost:8083) |
| Kafka              | 9092 | localhost:9092                                 |

---

## 5. Communication Between Services

NomNom uses two communication mechanisms.

### 5.1 REST Communication

REST is used for synchronous communication when a service requires an immediate response.

For example, when an order is placed, Order Service calls Customer Service to verify that the provided `customerCode` exists and belongs to an active customer.

```text
Order Service
      |
      | REST request
      v
Customer Service
      |
      | Customer validation response
      v
Order Service
```

### 5.2 Kafka Communication

Apache Kafka is used for asynchronous communication through business events.

The main event flow is:

```text
Order Service
     |
     | order.placed
     v
Restaurant Service
     |
     | order.confirmed
     v
Order Service
     |
     +--------------------+
     |                    |
     v                    v
Order Service       Delivery Service
                         |
                         | delivery.assigned
                         |
                         | order.delivered
                         v
                  Order Service
                         +
                  Customer Service
```

Main Kafka topics:

| Topic               | Producer           | Consumer                        | Purpose                                 |
| ------------------- | ------------------ | ------------------------------- | --------------------------------------- |
| `order.placed`      | Order Service      | Restaurant Service              | Validate ordered menu items             |
| `order.confirmed`   | Restaurant Service | Order Service, Delivery Service | Confirm order and trigger delivery      |
| `delivery.assigned` | Delivery Service   | Other interested services       | Notify that a rider has been assigned   |
| `order.delivered`   | Delivery Service   | Order Service, Customer Service | Notify that delivery has been completed |

---

# 6. Prerequisites

Before running the system, install the following:

* Java 21
* Docker Desktop
* Git
* Visual Studio Code
* VS Code Live Server extension

Apache Kafka must be available on:

```text
localhost:9092
```

---

# 7. Configuration and Running Instructions

## 7.1 Start Kafka

Make sure Docker Desktop is running.

From the project root directory, run:

```bash
docker compose up -d
```

Verify that the Kafka container is running:

```bash
docker ps
```

The Kafka broker should be available at:

```text
localhost:9092
```

---

## 7.2 Start Customer Service

Open a terminal and navigate to:

```text
customer-service/
```

### Windows

```bash
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

Customer Service runs on:

```text
http://localhost:8080
```

Verify that the service is running by opening:

```text
http://localhost:8080/customers
```

---

## 7.3 Start Restaurant Service

Open another terminal and navigate to:

```text
restaurant-service/
```

### Windows

```bash
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

Restaurant Service runs on:

```text
http://localhost:8081
```

Verify that the service is running by opening:

```text
http://localhost:8081/restaurants
```

---

## 7.4 Start Order Service

Customer Service must be running before starting Order Service because Order Service performs a synchronous REST request to Customer Service when an order is placed.

Open another terminal and navigate to:

```text
order-service/
```

### Windows

```bash
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

Order Service runs on:

```text
http://localhost:8082
```

Verify that the service is running by opening:

```text
http://localhost:8082/orders
```

---

## 7.5 Start Delivery Service

Open another terminal and navigate to:

```text
delivery-service/
```

### Windows

```bash
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

Delivery Service runs on:

```text
http://localhost:8083
```

The application should start successfully with the `NomnomDeliveryServiceApplication` startup message.

---

## 7.6 Start the Frontend

Open the following file in Visual Studio Code:

```text
nomnom-frontend/index.html
```

Run it using the VS Code Live Server extension.

The frontend provides the customer-facing interface, including the Register and My Profile pages.

---

# 8. H2 Database Console

Each microservice owns an independent H2 database.

## Customer Service

H2 Console:

```text
http://localhost:8080/h2-console
```

JDBC URL:

```text
jdbc:h2:mem:nomnom_customer
```

Username:

```text
sa
```

Password:

```text
```

Leave the password field empty.

---

## Restaurant Service

H2 Console:

```text
http://localhost:8081/h2-console
```

JDBC URL:

```text
jdbc:h2:mem:nomnom_restaurant
```

Username:

```text
sa
```

Password:

```text
```

Leave the password field empty.

---

## Order Service

H2 Console:

```text
http://localhost:8082/h2-console
```

JDBC URL:

```text
jdbc:h2:mem:nomnom_order
```

Username:

```text
sa
```

Password:

```text
```

Leave the password field empty.

---

## Delivery Service

H2 Console:

```text
http://localhost:8083/h2-console
```

JDBC URL:

```text
jdbc:h2:mem:nomnom_delivery
```

Username:

```text
sa
```

Password:

```text
```

Leave the password field empty.

---

# 9. Sample Inputs

The following sample inputs demonstrate the main use cases of the NomNom system.

## 9.1 Register a Customer

### Endpoint

```text
POST http://localhost:8080/customers
```

### Sample Input

```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "0111234567",
  "password": "Password123",
  "dateOfBirth": "2000-05-14",
  "address": {
    "unitOrHouseNumber": "12A",
    "street": "Jalan Santong",
    "postcode": "40150",
    "city": "Shah Alam",
    "state": "Selangor",
    "country": "Malaysia"
  }
}
```

---

## 9.2 Register a Restaurant

### Endpoint

```text
POST http://localhost:8081/restaurants
```

### Sample Input

```json
{
  "name": "Nasi Lemak Wangi",
  "description": "Authentic Malaysian nasi lemak",
  "cuisine": "Malaysian",
  "ownerName": "Ali Rahman",
  "contactPhone": "0321234567",
  "contactEmail": "restaurant@example.com",
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

---

## 9.3 Add a Menu Item

Replace `RST001` with the restaurant code generated by Restaurant Service.

### Endpoint

```text
POST http://localhost:8081/restaurants/RST001/menu
```

### Sample Input

```json
{
  "name": "Nasi Lemak Special",
  "description": "Rice with sambal, egg, anchovies and peanuts",
  "price": 12.90,
  "category": "Main Course"
}
```

---

## 9.4 Place an Order

Customer Service and Restaurant Service must be running before placing an order.

Replace the sample customer, restaurant and menu item codes with the actual codes generated by the respective services.

### Endpoint

```text
POST http://localhost:8082/orders
```

### Sample Input

```json
{
  "customerCode": "CUS1755000000000",
  "restaurantCode": "RST001",
  "deliveryAddress": "12A, Jalan Meranti, 40150 Shah Alam, Selangor",
  "items": [
    {
      "menuItemCode": "ITM001",
      "itemName": "Nasi Lemak Ayam",
      "quantity": 2,
      "unitPrice": 12.50
    },
    {
      "menuItemCode": "ITM002",
      "itemName": "Teh O Ais",
      "quantity": 2,
      "unitPrice": 3.00
    }
  ]
}
```

---

# 10. End-to-End Order Flow

The complete order journey can be demonstrated using the following sequence:

```text
1. Customer registers
        ↓
2. Restaurant registers
        ↓
3. Restaurant adds menu items
        ↓
4. Customer places an order
        ↓
5. Order Service validates customer through REST
        ↓
6. Order Service creates the order
        ↓
7. Order Service publishes order.placed
        ↓
8. Restaurant Service validates menu availability
        ↓
9. Restaurant Service publishes order.confirmed
        ↓
10. Order Service updates order to CONFIRMED
        ↓
11. Delivery Service receives order.confirmed
        ↓
12. Delivery Service assigns an available rider
        ↓
13. Delivery Service publishes delivery.assigned
        ↓
14. Rider completes the delivery
        ↓
15. Delivery Service publishes order.delivered
        ↓
16. Order Service updates order to DELIVERED
        ↓
17. Customer Service receives order.delivered
        ↓
18. Loyalty points are credited to the customer
```

---

# 11. Loyalty Point Business Rule

Customer Service manages the customer's loyalty points.

Loyalty points are awarded only when the customer's order has been successfully delivered.

The process is triggered by the `order.delivered` Kafka event:

```text
Delivery Service
       |
       | order.delivered
       v
Customer Service
       |
       | Identify customer using customerCode
       |
       | Apply loyalty point rule
       v
Customer loyalty points updated
```

---

# 12. Important Notes

* Each microservice owns its own H2 database.
* Services must not directly access another service's database.
* Customer Service must be running before placing an order.
* Kafka must be running before using the event-driven workflow.
* Use the actual generated customer, restaurant and menu item codes when testing.
* The H2 databases are in-memory databases. Data may be reset when the corresponding Spring Boot application is restarted.

---


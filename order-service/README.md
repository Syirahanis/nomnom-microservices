# NomNom — Order Service

Order placement and status-tracking microservice for the NomNom platform. Backend is Spring Boot + H2 (in-memory), communicating with Customer Service over REST and with the rest of the platform over Apache Kafka.

## Project structure

```
order-service/
├── src/main/java/com/nomnom/orderservice/
│   ├── client/        # CustomerServiceClient (REST call to Customer Service), CustomerLookupResponse
│   ├── config/        # CorsConfig, SecurityConfig, RestTemplateConfig
│   ├── controller/     # OrderController (REST endpoints)
│   ├── dto/            # Request/response objects
│   ├── entity/         # Order (aggregate root), OrderItem, Money (value object), OrderStatus
│   ├── event/           # OrderPlacedEvent, OrderConfirmedEvent, OrderDeliveredEvent
│   ├── exception/      # GlobalExceptionHandler + custom exceptions
│   ├── kafka/          # OrderKafkaProducer, OrderKafkaConsumer, KafkaConsumerConfig
│   ├── repository/     # OrderRepository (JPA)
│   └── service/        # OrderServiceImpl (application service), PricingService (domain service)
└── src/main/resources/application.properties
```

## What's already done

- Place an order, look it up by order code, list all orders, list a customer's orders, and cancel an order (while still PLACED) — wired end-to-end from the API to the H2 database.
- Before an order is created, Order Service calls Customer Service over REST (`GET /customers/{customerCode}`) to confirm the customer exists and is active. An unknown or inactive customer is rejected with `400 Bad Request`.
- Delivery fee is calculated by a domain service (`PricingService`): flat RM5.00, waived once the order subtotal reaches RM100.00.
- Publishes an `OrderPlaced` event to Kafka topic `order.placed` whenever an order is successfully created.
- Consumes `OrderConfirmed` (topic `order.confirmed`) and `OrderDelivered` (topic `order.delivered`) to advance an order's status, once the Restaurant & Catalog Service and Delivery Service publish them.
- CORS configured so a frontend can call the API directly.
- Backend returns JSON error bodies (not plain text) for validation errors, unknown orders, invalid state transitions, and unreachable downstream services.

To confirm it's working, visit `http://localhost:8082/orders` in a browser — you should see `[]` (or a list of orders).

H2 console (to inspect the database directly): `http://localhost:8082/h2-console`
- JDBC URL: `jdbc:h2:mem:nomnom_order`
- Username: `sa`
- Password: *(leave blank)*

> Note: the database is in-memory (`ddl-auto=create-drop`), so all data resets every time the backend restarts.

## Running instructions

1. Start Kafka: `docker compose up -d` (from the `nomnom-microservices` root — same Kafka container the other services share).
2. Start Customer Service first: `cd customer-service && ./mvnw spring-boot:run` (port 8080). Order Service needs it reachable to validate customers.
3. Start Order Service: `cd order-service && ./mvnw spring-boot:run` (port 8082).
4. Register a customer via Customer Service, then use the returned `customerCode` in the requests below.

## Sample inputs (use cases)

_(1) Register a customer (Customer Service, port 8080)_:
```shell
curl -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Aina Rahman","email":"aina@example.com","phoneNumber":"0123456789","password":"Test1234"}'
```

_(2) Place an order (Order Service, port 8082)_:
```shell
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerCode": "CUSXXXXXXXXXXXX",
    "restaurantCode": "REST001",
    "deliveryAddress": "12A, Jalan Example, 40150 Shah Alam, Selangor",
    "items": [
      { "menuItemCode": "MENU-NASI-LEMAK", "itemName": "Nasi Lemak Ayam", "quantity": 2, "unitPrice": 12.50 },
      { "menuItemCode": "MENU-TEH-O", "itemName": "Teh O Ais", "quantity": 2, "unitPrice": 3.00 }
    ]
  }'
```

_(3) Get order by order code_:
```shell
curl -X GET http://localhost:8082/orders/{orderCode}
```

_(4) Get all orders for a customer_:
```shell
curl -X GET http://localhost:8082/orders/customer/{customerCode}
```

_(5) Cancel an order (only while status is PLACED)_:
```shell
curl -X PUT http://localhost:8082/orders/{orderCode}/cancel
```

_(6) Simulate the Restaurant & Catalog Service confirming an order (manual Kafka message, useful until that service exists)_:
```shell
docker exec -it nomnom-kafka /opt/kafka/bin/kafka-console-producer.sh \
  --topic order.confirmed --broker-list localhost:9092
```
then paste (single line) and press Enter:
```json
{"orderCode":"ORDXXXXXXXXXXXXX","restaurantCode":"REST001","status":"CONFIRMED"}
{"orderCode":"ORD1786511183845","restaurantCode":"REST001","status":"CONFIRMED"}
```
_(7) Confirm the status changed_:
```shell
curl -X GET http://localhost:8082/orders/{orderCode}
curl -X GET http://localhost:8082/orders/{ORD1786511183845}
```
`status` should now read `"CONFIRMED"`.

## API endpoints

| Method | Path                          | Description                          |
|--------|--------------------------------|----------------------------------------|
| POST   | `/orders`                       | Place a new order                      |
| GET    | `/orders`                       | List all orders                        |
| GET    | `/orders/{orderCode}`           | Get an order by order code              |
| GET    | `/orders/customer/{customerCode}` | List all orders for a customer         |
| PUT    | `/orders/{orderCode}/cancel`    | Cancel an order (only while PLACED)     |

## Kafka topics

| Topic             | Direction | Payload                                        |
|--------------------|-----------|--------------------------------------------------|
| `order.placed`     | publishes | orderCode, customerCode, restaurantCode, items[]   |
| `order.confirmed`  | consumes  | orderCode, restaurantCode, status                  |
| `order.delivered`  | consumes  | orderCode, customerCode, status                    |

## Troubleshooting

- **"Could not reach Customer Service" / 503 response** → Customer Service isn't running on port 8080, or `customer-service.base-url` in `application.properties` doesn't match. Start Customer Service first.
- **CORS error in browser console** → confirm `CorsConfig.java` exists and `SecurityConfig.java` has `.cors(Customizer.withDefaults())`, then restart the backend.
- **Kafka listener not receiving events** → confirm the Kafka container is running (`docker compose ps`) and that the topic name in the producer matches exactly (`order.confirmed`, `order.delivered` — case-sensitive, dot-separated).
- **`localhost:8082` shows a Whitelabel Error Page** → expected, there's no page mapped to `/`. Use `/orders` or the H2 console instead.

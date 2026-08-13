# NomNom Delivery Service

## Overview

The **Delivery Service** is a Spring Boot microservice within the NomNom food delivery platform. It manages the full delivery lifecycle — from assigning an available rider to a confirmed order, through tracking the delivery in transit, to marking it as delivered and freeing the rider.

It is **event-driven**: it consumes order-confirmation events from the Restaurant service over **Kafka**, assigns riders, and publishes delivery events back onto the message bus for other services (e.g., notifications) to consume.


### Kafka Topics

| Topic | Direction | Description |
|-------|-----------|-------------|
| `order.confirmed` | **Consumed** | Published by the Restaurant service when an order is confirmed. Triggers rider assignment. |
| `delivery.assigned` | **Produced** | Published after a rider is assigned to a confirmed order. |
| `order.delivered` | **Produced** | Published after a delivery is completed. |

---

## Project Structure

```
delivery-service/
├── src/
│   ├── main/
│   │   ├── java/com/nomnom/deliveryservice/
│   │   │   ├── NomnomDeliveryServiceApplication.java   # Main entry point
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java                     # CORS configuration
│   │   │   │   └── SecurityConfig.java                 # Security filter chain
│   │   │   ├── controller/
│   │   │   │   ├── DeliveryController.java             # /api/deliveries endpoints
│   │   │   │   └── RiderController.java                # /api/riders endpoints
│   │   │   ├── dto/                                    # Request/response objects
│   │   │   │   ├── DeliveryResponse.java
│   │   │   │   ├── DeliveryStatusResponse.java
│   │   │   │   ├── RegisterRiderRequest.java
│   │   │   │   ├── RiderResponse.java
│   │   │   │   └── UpdateDeliveryStatusRequest.java
│   │   │   ├── entity/                                 # JPA entities & enums
│   │   │   │   ├── Delivery.java
│   │   │   │   ├── DeliveryStatus.java
│   │   │   │   ├── Rider.java
│   │   │   │   └── RiderStatus.java
│   │   │   ├── event/                                  # Kafka event models
│   │   │   │   ├── DeliveryAssignedEvent.java
│   │   │   │   ├── OrderConfirmedEvent.java
│   │   │   │   └── OrderDeliveredEvent.java
│   │   │   ├── exception/                               # Custom exceptions & handler
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InvalidDeliveryStateException.java
│   │   │   │   ├── NoAvailableRiderException.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── kafka/                                  # Kafka producer/consumer
│   │   │   │   ├── DeliveryKafkaConsumer.java
│   │   │   │   ├── DeliveryKafkaProducer.java
│   │   │   │   └── KafkaConsumerConfig.java
│   │   │   ├── repository/                              # Spring Data repositories
│   │   │   │   ├── DeliveryRepository.java
│   │   │   │   └── RiderRepository.java
│   │   │   └── service/                                 # Business logic
│   │   │       ├── DeliveryService.java
│   │   │       ├── DeliveryServiceImpl.java
│   │   │       ├── RiderAssignmentService.java
│   │   │       ├── RiderService.java
│   │   │       └── RiderServiceImpl.java
│   │   └── resources/
│   │       └── application.properties                  # Configuration
│   └── test/                                            # Tests
├── pom.xml                                              # Maven build file
└── Dockerfile                                           # Container build
```

---

## Domain Model

### Delivery Status (`DeliveryStatus`)

| Value | Description |
|-------|-------------|
| `ASSIGNED` | A rider has been assigned to the order. |
| `IN_TRANSIT` | The rider is delivering the order. |
| `DELIVERED` | The order has been delivered. |

### Rider Status (`RiderStatus`)

| Value | Description |
|-------|-------------|
| `AVAILABLE` | The rider can accept new deliveries. |
| `BUSY` | The rider is currently on a delivery. |

### Delivery Entity

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | Auto-generated primary key |
| `deliveryCode` | `String` | Unique delivery identifier (e.g., `DEL<timestamp>`) |
| `orderId` | `String` | Unique order identifier |
| `customerCode` | `String` | Customer identifier |
| `riderId` | `String` | Assigned rider code |
| `status` | `DeliveryStatus` | Current delivery state |
| `deliveryAddress` | `String` | Delivery destination |
| `assignedAt` | `LocalDateTime` | When the rider was assigned |
| `inTransitAt` | `LocalDateTime` | When transit started |
| `deliveredAt` | `LocalDateTime` | When the delivery completed |

### Rider Entity

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | Auto-generated primary key |
| `riderCode` | `String` | Unique rider identifier |
| `name` | `String` | Rider name |
| `phoneNumber` | `String` | Rider contact number |
| `status` | `RiderStatus` | Current rider state |
| `registeredAt` | `LocalDateTime` | When the rider registered |

---

## Prerequisites

- **Java 17+** (Spring Boot 3.x — verify in `pom.xml`)
- **Maven 3.8+**
- **Kafka** running on `localhost:9092` (for event-driven flow)
- **Docker** (optional, for containerized runs)

---

## Configuration

Configuration lives in `src/main/resources/application.properties`:

| Property | Value | Description |
|----------|-------|-------------|
| `spring.application.name` | `nomnom-delivery-service` | Service name |
| `server.port` | `8083` | HTTP port |
| `spring.datasource.url` | `jdbc:h2:mem:nomnom_delivery` | In-memory H2 database |
| `spring.datasource.username` | `sa` | DB username |
| `spring.datasource.password` | *(empty)* | DB password |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Schema auto-created on startup |
| `spring.h2.console.enabled` | `true` | H2 console enabled |
| `spring.h2.console.path` | `/h2-console` | H2 console URL |
| `spring.kafka.bootstrap-servers` | `localhost:9092` | Kafka broker address |
| `spring.kafka.consumer.group-id` | `delivery-service` | Consumer group |
| `spring.kafka.consumer.auto-offset-reset` | `earliest` | Offset reset policy |

> **Note:** The database is in-memory (`create-drop`), so all data is lost on restart. For persistence, switch to a real database (e.g., PostgreSQL/MySQL) and update the datasource properties.

---

## Running the Service

### 1. Start Kafka (required for event flow)

Make sure a Kafka broker is running on `localhost:9092`. If using Docker:

```bash
docker run -d -p 9092:9092 --name kafka \
  -e KAFKA_CFG_NODE_ID=0 \
  -e KAFKA_CFG_PROCESS_ROLES=controller,broker \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka:9093 \
  -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  bitnami/kafka:latest
```

### 2. Build the project

```bash
cd delivery-service
mvn clean package
```

### 3. Run with Maven (development)

```bash
mvn spring-boot:run
```

### 4. Run the packaged JAR

```bash
java -jar target/delivery-service-0.0.1-SNAPSHOT.jar
```

### 5. Run with Docker

```bash
docker build -t nomnom-delivery-service .
docker run -p 8083:8083 nomnom-delivery-service
```

---

## Verifying It's Up

Once started, the service logs:

```
Started NomnomDeliveryServiceApplication in X.XX seconds
```

The H2 console is available at:

```
http://localhost:8083/h2-console
```

- **JDBC URL:** `jdbc:h2:mem:nomnom_delivery`
- **Username:** `sa`
- **Password:** *(empty)*

---

## API Endpoints

Base URL: `http://localhost:8083`

### Deliveries — `/api/deliveries`

#### `GET /api/deliveries`
List all deliveries.

**Response `200 OK`** — array of `DeliveryResponse`:
```json
[
  {
    "id": 1,
    "deliveryCode": "DEL1710000000000",
    "orderId": "ORD-123",
    "riderId": "RID-001",
    "status": "ASSIGNED",
    "deliveryAddress": "Address on file for order ORD-123",
    "assignedAt": "2026-08-13T10:00:00",
    "inTransitAt": null,
    "deliveredAt": null
  }
]
```

#### `GET /api/deliveries/{deliveryCode}`
Get full delivery details by delivery code.

**Path parameter:** `deliveryCode` (e.g., `DEL1710000000000`)

**Response `200 OK`** — `DeliveryResponse` (same shape as above).

**Errors:**
- `404 Not Found` — no delivery with that code.

#### `GET /api/deliveries/order/{orderCode}`
Get delivery by order code.

**Path parameter:** `orderCode` (e.g., `ORD-123`)

**Response `200 OK`** — `DeliveryResponse`.

**Errors:**
- `404 Not Found` — no delivery for that order.

#### `GET /api/deliveries/{deliveryCode}/status`
Get the current delivery status (tracking).

**Path parameter:** `deliveryCode`

**Response `200 OK`** — `DeliveryStatusResponse`:
```json
{
  "deliveryCode": "DEL1710000000000",
  "orderId": "ORD-123",
  "riderId": "RID-001",
  "status": "IN_TRANSIT",
  "assignedAt": "2026-08-13T10:00:00",
  "deliveredAt": null
}
```

**Errors:**
- `404 Not Found` — no delivery with that code.

#### `PUT /api/deliveries/{deliveryCode}/status`
Manually update the delivery status (useful for testing/demo).

**Path parameter:** `deliveryCode`

**Request body** — `UpdateDeliveryStatusRequest`:
```json
{
  "status": "IN_TRANSIT"
}
```

**Valid status transitions:**
- `ASSIGNED` → `IN_TRANSIT`
- `IN_TRANSIT` → `DELIVERED` (also releases the rider and publishes `order.delivered`)

**Response `200 OK`** — `DeliveryResponse`.

**Errors:**
- `400 Bad Request` — invalid status value, or invalid transition (e.g., moving back to `ASSIGNED`).
- `404 Not Found` — no delivery with that code.

---

### Riders — `/api/riders`

#### `POST /api/riders`
Register a new rider.

**Request body** — `RegisterRiderRequest`:
```json
{
  "name": "John Doe",
  "phoneNumber": "+1234567890"
}
```

**Response `201 Created`** — `RiderResponse`:
```json
{
  "id": 1,
  "riderCode": "RID-001",
  "name": "John Doe",
  "phoneNumber": "+1234567890",
  "status": "AVAILABLE",
  "registeredAt": "2026-08-13T10:00:00"
}
```

**Errors:**
- `400 Bad Request` — missing `name` or `phoneNumber`.

#### `GET /api/riders`
List all riders.

**Response `200 OK`** — array of `RiderResponse`.

#### `GET /api/riders/{riderCode}`
Get a rider by rider code.

**Path parameter:** `riderCode` (e.g., `RID-001`)

**Response `200 OK`** — `RiderResponse`.

**Errors:**
- `404 Not Found` — no rider with that code.

---

## Event Flow (How Deliveries Are Created)

1. The **Restaurant service** publishes an `OrderConfirmedEvent` to the `order.confirmed` topic.
2. `DeliveryKafkaConsumer` receives the event and calls `DeliveryServiceImpl.handleOrderConfirmed(...)`.
3. Only orders with status `CONFIRMED` are processed; `REJECTED` orders are ignored.
4. If a delivery already exists for the order, the event is ignored (no double assignment).
5. `RiderAssignmentService` picks the first `AVAILABLE` rider and marks them `BUSY`.
6. A new `Delivery` is created with status `ASSIGNED` and saved.
7. A `DeliveryAssignedEvent` is published to the `delivery.assigned` topic.
8. When the delivery is marked `DELIVERED`, the rider is released back to `AVAILABLE` and an `OrderDeliveredEvent` is published to the `order.delivered` topic.

---

## Testing

Run the test suite:

```bash
mvn test
```

---

## Troubleshooting

- **Port already in use** — change `server.port` in `application.properties` or stop the process on port `8083`.
- **Kafka connection refused** — ensure Kafka is running on `localhost:9092` before starting the service.
- **No available rider** — deliveries are only created when a rider is registered and `AVAILABLE`. Register a rider via `POST /api/riders` first.
- **Data resets on restart** — the H2 database is in-memory (`create-drop`). Use a persistent database for long-lived data.


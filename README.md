README

# NomNom — Customer Service

Customer registration and account management microservice for the NomNom platform. Backend is Spring Boot + H2 (in-memory), frontend is a standalone HTML/CSS/JS page that talks to the backend over REST.

## Project structure

```
nomnom-microservices/
├── customer-service/                               # Spring Boot backend (API + H2 database)
│   ├── src/main/java/com/nomnom/customerservice/
│   │   ├── config/                                 # CorsConfig, SecurityConfig
│   │   ├── controller/                             # CustomerController (REST endpoints)
│   │   ├── dto/                                    # Request/response objects
│   │   ├── entity/                                 # Customer, Address, AccountStatus
│   │   ├── exception/                              # GlobalExceptionHandler
│   │   ├── kafka/                                  # Kafka producer/consumer
│   │   ├── repository/                             # CustomerRepository (JPA)
│   │   └── service/                                # CustomerServiceImpl (business logic)
│   └── src/main/resources/application.properties
├── nomnom-frontend/                                # Static frontend (HTML/CSS/JS)
│   ├── index.html
│   ├── script.js
│   └── style.css
└── docker-compose.yml                              # Kafka setup
```
```
## What's already done

- Customer registration, lookup by customer code, profile editing, and account deletion — all wired end-to-end from the UI through the API to the H2 database.
- CORS configured so the frontend (served on its own port) can call the backend.
- Backend returns JSON error bodies (not plain text), so the frontend can show real error messages.

To confirm it's working, visit `http://localhost:8080/customers` in a browser — you should see `[]` (or a list of customers).

H2 console (to inspect the database directly): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:nomnom_customer`
- Username: `sa`
- Password: *(leave blank)*

> Note: the database is in-memory (`ddl-auto=create-drop`), so all data resets every time the backend restarts.

## Running the frontend

1. Install the **Live Server** extension in VS Code (Extensions tab → search "Live Server" → Install).
2. Right-click `nomnom-frontend/index.html` → **Open with Live Server**.
3. It opens something like `http://127.0.0.1:5500/nomnom-frontend/index.html` — that's the page to use, not `localhost:8080` (which is the backend API, not a page).

**Make sure the backend is running first — the frontend won't be able to register/find/edit/delete accounts otherwise.

## API endpoints

| Method | Path                        | Description                  |
|--------|-----------------------------|-------------------------------|
| POST   | `/customers`                 | Register a new customer      |
| GET    | `/customers`                  | List all customers           |
| GET    | `/customers/{customerCode}`    | Get a customer by code       |
| PUT    | `/customers/{customerCode}`    | Update a customer             |
| DELETE | `/customers/{customerCode}`    | Delete (deactivate) a customer |

## Troubleshooting

- **"Could not reach Customer Service" toast** → backend isn't running, or was stopped/restarted without picking up config changes. Fully stop and rerun `mvnw spring-boot:run`.
- **CORS error in browser console** → confirm `CorsConfig.java` exists and `SecurityConfig.java` has `.cors(Customizer.withDefaults())`, then restart the backend (config changes require a full restart, not just a save).
- **`localhost:8080` shows a Whitelabel Error Page** → expected, there's no page mapped to `/`. Use the Live Server URL for the frontend instead.
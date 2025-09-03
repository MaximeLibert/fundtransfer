# Fund Transfer Service

A Spring Boot application for transferring funds between accounts with currency conversion.

In order to test the application, the database is pre-filled with existing accounts

| id | owner_id | currency | balance  |
|----|----------|----------|----------|
| 1  | 1        | USD      | 1000.00  |
| 2  | 2        | EUR      | 500.00   |
| 3  | 3        | GBP      | 750.00   |
| 4  | 4        | GBP      | 1750.00  |

**Explanation:**
- **id:** The unique identifier for the account.
- **owner_id:** The unique identifier for the account owner.
- **currency:** The currency of the account (USD, EUR, GBP).
- **balance:** The current balance in the account.

## Getting Started

### 1. Clone the Repository
```
git clone https://github.com/MaximeLibert/fundtransfer.git
cd fundtransfer
```


### 2. Build the Application
```
mvn clean package
```

### 3. Run the database with Docker Compose
```
docker-compose up -d
```
This command will start the MariaDB database.

### 4. Run the application
```
java -jar target/fundtransfer-0.0.1-SNAPSHOT.jar
```

### 5. Access the Application
- API Docs - Swagger UI: http://localhost:8080/fundtransfer-api/swagger-ui.html
- API Endpoint: http://localhost:8080/fundtransfer-api

You can use the postman collection available in this repo to get started.

---

## Run the app with maven
```
mvn spring-boot:run
```

Note : If you have previously run the database and want to start fresh, reset the database volume.

```
docker-compose down -v
docker-compose up -d 
```

## Running Tests
To run tests locally, you need to have Java 21 and Maven installed. Use the following command:
```
mvn test
```
This will execute all unit and integration tests.

---

## Troubleshooting

### Database Connection Issues
- Ensure the MariaDB container is running: ```docker-compose ps```
- Check the logs for errors:  ```docker-compose logs db```

### Application Fails to Start
- Check the application logs: ```docker-compose logs app```
- Ensure all required environment variables are set correctly.

### Port Conflicts
- If port 8080 or 13306 is already in use, update the ports section in docker-compose.yml to use different ports.

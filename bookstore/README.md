# Simple Online Bookstore

This project is a simple Online Bookstore application.

The backend is developed using Spring Boot and Java 17. It provides REST APIs for managing books, users, shopping cart and checkout.

The application uses H2 database for storing the data.

## Technologies Used

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security
- H2 Database
- Maven
- JUnit 5
- Mockito
- MockMvc
- OpenAPI / Swagger

## Backend Features

### User

- User registration
- Basic authentication using Spring Security
- Duplicate username validation

### Books

- Get available books
- Add a book
- Update a book
- Book data is stored in H2 database

### Cart

- Add book to cart
- View cart
- Update cart item quantity
- Remove book from cart
- Validate available stock

### Checkout

- Checkout the user's cart
- Create an order from cart items
- Create order items for each cart item
- Calculate total order amount
- Reduce book stock
- Validate insufficient stock
- Clear the cart after successful checkout

## API Endpoints

### User Registration

```http
POST /api/users/registration
```

Example request:

```json
{
  "username": "user1",
  "password": "1234"
}
```

Registration does not require authentication.

### Books

```http
GET /api/books
POST /api/books
PUT /api/books/{id}
```

Book management APIs require authentication.

### Cart

```http
POST /api/cart
GET /api/cart
PUT /api/cart/{itemId}
DELETE /api/cart/{itemId}
```

### Checkout

```http
POST /api/cart/checkout
```

Checkout requires authentication.

## Authentication

The application uses Spring Security Basic Authentication.

For protected APIs, send the username and password using Basic Authentication.

Example:

```text
Username: user1
Password: 1234
```

Registration API is available without authentication.

## Database

The application uses H2 database.

H2 Console:

```text
http://localhost:8080/h2-console
```

The database configuration can be found in:

```text
application.properties
```

## Swagger / OpenAPI

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Swagger can be used to view and test the REST APIs.

## Running the Application

### Prerequisites

- Java 17
- Maven

### Start the application

Run the following command from the backend project directory:

```bash
mvn spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

## Running Tests

To run all tests:

```bash
mvn test
```

The backend tests are written using JUnit 5, Mockito and MockMvc.

## Error Handling

The application has global exception handling for common errors such as:

- Resource not found
- Duplicate username
- Insufficient stock
- Invalid Quantity Count
- Required quantity not available

 

## Project Structure

```text
src
 ├── main
 │    ├── java
 │    │    └── com.kata.bookstore
 │    │         ├── controller
 │    │         ├── service
 │    │         ├── repository
 │    │         ├── entity
 │    │         ├── dto
 │    │         ├── exception
 │    │         └── config
 │    │
 │    └── resources
 │         └── application.properties
 │
 └── test
      └── java
           └── com.kata.bookstore
```

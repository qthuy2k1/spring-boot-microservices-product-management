# PRODUCT MANAGEMENT API

#### Technologies in use:

* Coding: Spring Boot, Spring Security, Spring Cloud
* RDBMS: PosgreSQL, MongoDB
* Messaging: Kafka
* Containerization: Docker
* Orchestration: Kubernetes
* Caching: Redis
* Api Layer: GraphQL
* Unit test: testcontainers

#### Services:

* Eureka Server (Service Discovery): a service registry and discovery service for all other microservices in the system.
* Api Gateway: a single entry point for all client requests to the microservices.
* User Service:
    * Manage user accounts and profiles within the system.
    * User registration and authentication (login, logout).
    * Manages user roles and permissions.
* Product Service:
    * Manages information about the products offered in the system.
    * Handles product catalog management (adding, updating, deleting products).
    * Stores product details (name, description, price, SKU, category, etc.).
    * Interacts with the Inventory Service to check product availability.
* Payment Service (not available at the moment):
    * Handles all payment-related operations within the system.
    * Processes payments (integrating with VNPay).
    * Interacts with the Order Service to update order statuses after payment.
* Order Service:
    * Manages the creation, processing, and tracking of customer orders.
    * Creates orders based on user selections.
    * Manages order statuses.
    * Calculates order totals.
    * Manages order items and quantities.
    * Interacts with the Inventory Service to reduce stock levels. (working...)
* Notification Service:
    * Handles sending various types of notifications to users.
    * Sends email notifications.
    * Subscribes to events from other services
* Inventory Service:
    * Manages the stock levels and availability of products.
    * Tracks the quantity of each product in stock.
    * Updates stock levels based on orders and returns.
    * Provides product availability information to other services (Product and Order Services).

## Steps to set up

1. **Clone the Repository**

  ```bash
  git clone https://github.com/qthuy2k1/spring-boot-microservices-product-management.git
  cd spring-boot-microservices-product-management
  ```

2. **Start Docker Compose**

* This will start all necessary infrastructure services (databases, Kafka, Redis, Eureka, etc.).

  ```bash
  docker compose -f docker-compose.local.yml up -d
  ```

3. **Run Microservices with Maven:**

* Navigate to each microservice directory (e.g., `user-service`, `product-service`).
* Run the Spring Boot application using Maven:

  ```bash
  cd user-service
  mvn spring-boot:run
  ```

* Repeat this for each microservice.

4. **Access the Application:**

* Once all services are running, the API Gateway will be available at `http://localhost:8080`.

* You can then access the different microservices through the gateway, or directly via their own URLs.

## Explore Rest APIs

The app defines following CRUD APIs.

### Users

| Method | Url                    | Description          | Sample Valid Request Body |
|--------|------------------------|----------------------|---------------------------|
| GET    | /api/v1/users/{id}     | Get user info by id  |                           |
| GET    | /api/v1/users/my-info  | Get user signed info |                           |
| POST   | /api/v1/users/register | Create user          | [JSON](#usercreate)       |
| PUT    | /api/v1/users/{id}     | Update user          | [JSON](#userupdate)       |
| DELETE | /api/v1/users/{id}     | Delete user          |                           |

### Auth

| Method | Url                        | Description       | Sample Valid Request Body |
|--------|----------------------------|-------------------|---------------------------|
| POST   | /api/v1/auth/token         | Get token         | [JSON]()                  |
| POST   | /api/v1/auth/introspect    | Validate token    | [JSON]()                  |
| POST   | /api/v1/auth/refresh-token | Get refresh token | [JSON]()                  |
| POST   | /api/v1/auth/logout        | Logout            | [JSON]()                  |

### Products

| Method | Url                     | Description                                               | Sample Valid Request Body |
|--------|-------------------------|-----------------------------------------------------------|---------------------------|
| GET    | /api/v1/products        | Get all products                                          |                           |
| GET    | /api/v1/products/{id}   | Get product by id                                         |                           |
| POST   | /api/v1/products        | Create new product                                        | [JSON](#productcreate)    |
| POST   | /api/v1/products/upload | Upload a CSV file listing products to create them in bulk |                           |
| PUT    | /api/v1/products/{id}   | Update product                                            | [JSON](#productupdate)    |
| DELETE | /api/v1/products/{id}   | Delete product                                            |                           |

### Product Categories

| Method | Url                        | Description                 | Sample Valid Request Body      |
|--------|----------------------------|-----------------------------|--------------------------------|
| GET    | /api/v1/product-categories | Get all product categories  |                                |
| POST   | /api/v1/product-categories | Create new product category | [JSON](#productcategorycreate) |

### Inventories

| Method | Url                                        | Description                                              | Sample Valid Request Body |
|--------|--------------------------------------------|----------------------------------------------------------|---------------------------|
| GET    | /api/v1/inventories?quantity=1&productId=1 | Check the quantity of a product which is in stock or not |                           |
| POST   | /api/v1/inventories                        | Create new inventory                                     |                           |

### Order

| Method | Url                 | Description      | Sample Valid Request Body |
|--------|---------------------|------------------|---------------------------|
| POST   | /api/v1/orders      | Create new order |                           |
| PUT    | /api/v1/orders/{id} | Update order     |                           |

## GraphQL API

### Products

| Query    | Url               | Description        | Sample Valid Request Body     |
|----------|-------------------|--------------------|-------------------------------|
| Query    | /products/graphql | Get product by id  | [JSON](#productgetgraphql)    |
| Query    | /products/graphql | Get all products   | [JSON](#producgetallgraphql)  |
| Mutation | /products/graphql | Create new product | [JSON](#productcreategraphql) |

### Orders

| Query | Url             | Description    | Sample Valid Request Body |
|-------|-----------------|----------------|---------------------------|
| Query | /orders/graphql | Get all orders | [JSON](#ordergetgraphql)  |

Test them using postman or any other rest client.

## Sample Valid JSON Request Body

##### <a id="usercreate">User Register -> /api/v1/users/register</a>

```json
{
  "code": 1000,
  "result": {
    "name": "John Doe",
    "password": "password",
    "email": "john.doe@gmail.com"
  }
}
```

##### <a id="userupdate">Update User -> /api/v1/users/{id}</a>

```json
{
  "code": 1000,
  "result": {
    "name": "12311",
    "email": "112@gmail.com",
    "password": "123123"
  }
}
```

##### <a id="gettoken">Log In -> /api/v1/auth/token </a>

```json
{
  "email": "john.doe@gmail.com",
  "password": "password"
}
```

##### <a id="introspect">Introspect -> /api/v1/auth/introspect</a>

```json
{
  "token": "token here"
}
```

##### <a id="refreshtoken">Get refresh token -> /api/v1/auth/refresh-token</a>

```json
{
  "token": "token here"
}
```

##### <a id="logout">Logout -> /api/v1/auth/logout</a>

```json
{
  "token": "token here"
}
```

##### <a id="productcreate">Create Product -> /api/v1/products</a>

```json
{
  "code": 1000,
  "result": {
    "name": "Iphone 12",
    "description": "iphone 12",
    "price": 1000,
    "categoryId": 1,
    "skuCode": "abc",
    "quantity": "4"
  }
}
```

##### <a id="productupdate">Update Product -> /api/v1/products/{id}</a>

```json
{
  "code": 1000,
  "result": {
    "name": "Iphone 12",
    "description": "iphone 12",
    "price": 1000,
    "categoryId": 1,
    "skuCode": "abc",
    "quantity": "4"
  }
}
```

##### <a id="productupload"> Upload a CSV file listing products -> /api/v1/products/upload</a>

```html

<form id="productUploadForm" method="POST" enctype="multipart/form-data">
    <input type="file" id="productCsvFile" name="file" accept=".csv" required>
    <button type="submit">Upload Products</button>
</form>
```

##### <a id="productcategorycreate">Create Product Category-> /api/v1/product-categories</a>

```json
{
  "code": 1000,
  "result": {
    "name": "Category 1",
    "description": "Description of category 1"
  }
}
```

##### <a id="ordercreate">Create Order -> /api/v1/orders</a>

```json
{
  "code": 1000,
  "result": {
    "userId": 1,
    "orderItem": [
      {
        "productId": 2,
        "quantity": 2,
        "skuCode": "abc"
      }
    ]
  }
}
```

## Sample Valid GraphQL Query

##### <a id="productgetgraphql">Get a Product in GraphQL -> /products/graphql

###### Query

```graphql
query Product($arg1: Int!) {
    productById(id: $arg1) {
        id
        name
        description
        price
        skuCode
        category {
            id
            name
            description
        }
    }
}
```

###### Variables

```json
{
  "arg1": 1
}
```

##### <a id="producgetallgraphql">Get all Products in GraphQL -> /products/graphql

###### Query

```graphql
query GetProducts {
    getProducts {
        id
        name
        description
        price
        skuCode
        category {
            id
            name
            description
        }
    }
}
```

##### <a id="productcreategraphql">Create Product in GraphQL -> /products/graphql

###### Query

```graphql
mutation CreateProduct($productData: ProductRequest!) {
    createProduct(input: $productData)
}
```

###### Variables

```json
{
  "productData": {
    "name": "Iphone 12",
    "description": "Iphone 12 des",
    "price": 950,
    "categoryId": 1,
    "skuCode": "aaa"
  }
}
```

##### <a id="ordergetgraphql">Get all Orders in GraphQL -> /orders/graphql

###### Query

```graphql
query GetOrders {
    getOrders {
        id
        status
        totalAmount
        createdAt
        updatedAt
        user {
            id
            name
            email
            role
        }
        orderItems {
            id
            quantity
            price
            product {
                id
                name
                description
                price
                skuCode
                category {
                    id
                    name
                    description
                }
            }
        }
    }
}
```

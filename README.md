 # OrderFlow: A Spring Boot Microservices E-commerce Backend Application

OrderFlow is a backend system designed to simulate the core functionalities of an e-commerce order processing platform. It's built using a microservices architecture with Spring Boot and Spring Cloud, incorporating various modern backend development patterns and (eventually) AI-driven features.

This project serves as a learning playground for practicing and demonstrating skills in:
*   Microservice Design & Implementation
*   Spring Boot & Spring Cloud Ecosystem
*   REST API Development
*   Inter-service Communication (Synchronous & Asynchronous)
*   Service Discovery, API Gateway, Centralized Configuration
*   Resilience Patterns (Circuit Breakers, Retries)
*   Security (JWT/OAuth2)
*   Observability (Logging, Metrics, Tracing)
*   Containerization with Docker & Docker Compose
*   Basic AI/ML Integrations

## Project Goals

*   To build a robust, scalable, and maintainable microservices application.
*   To implement common enterprise backend features.
*   To explore and learn various Spring Boot and Spring Cloud components.
*   To integrate simple AI features relevant to an e-commerce domain.
*   To provide a practical example for aspiring backend developers.

## Architecture Overview

OrderFlow follows a microservices architecture. The key services include:

*   **`user-service`**: Manages user accounts, profiles, and authentication.
*   **`product-service`**: Manages the product catalog, details, pricing, and (optionally) reviews.
*   **`inventory-service`**: Tracks stock levels for products.
*   **`order-service`**: Handles order creation, retrieval, and orchestrates the order processing workflow.
*   **`payment-service` (Mock)**: Simulates payment processing.
*   **`api-gateway`**: Single entry point for all client requests, handles routing and cross-cutting concerns.
*   **`config-server`**: Centralized configuration management for all services.
*   **`discovery-service` (Eureka)**: Service registration and discovery.
*   **`recommendation-service` (Planned/Optional)**: Provides product recommendations.
*   **`analysis-service` (Planned/Optional)**: Performs tasks like sentiment analysis on product reviews.
*   **`fraud-detection-service` (Planned/Optional)**: Analyzes orders for potential fraud.

*(Optional: You can add a simple architecture diagram here if you create one, e.g., using draw.io and embedding the image)*

## Tech Stack

**Core Backend:**
*   Java (JDK 17+)
*   Spring Boot 3.x
*   Spring Web MVC / WebFlux (Primarily MVC for now)
*   Spring Data JPA
*   Spring Security (JWT/OAuth2)
*   Spring Cloud:
    *   Netflix Eureka (Service Discovery)
    *   Spring Cloud Gateway (API Gateway)
    *   Spring Cloud Config (Configuration Management)
    *   Resilience4j (Circuit Breaker, Retry)
    *   Spring Cloud Stream (for Kafka/RabbitMQ integration)
    *   Micrometer Tracing (Distributed Tracing)
*   Spring Boot Actuator (Monitoring & Metrics)

**Databases:**
*   PostgreSQL / MySQL (for relational data)
*   MongoDB (for product catalog or other suitable data - optional)
*   H2 (for in-memory testing)
*   Neo4j (for graph-based recommendations - optional)

**Messaging:**
*   Apache Kafka / RabbitMQ

**AI/ML (Planned/Optional - Free Tier/Open Source):**
*   Python (Flask/FastAPI) for dedicated AI services
*   Libraries: NLTK, TextBlob, VADER, Scikit-learn, Hugging Face Transformers (local models)
*   Java ML Libraries: Weka, Tribuo, Apache Mahout

**Containerization & Orchestration:**
*   Docker
*   Docker Compose

**Build & Version Control:**
*   Maven
*   Git & GitHub

**Observability (Local Setup):**
*   Zipkin / Tempo (Distributed Tracing)
*   Prometheus (Metrics Collection)
*   Grafana (Metrics & Log Visualization)
*   ELK Stack (Elasticsearch, Logstash, Kibana) / Loki (Logging)

**Testing:**
*   JUnit 5
*   Mockito
*   Spring Boot Test
*   Testcontainers
*   Postman / Insomnia (for API testing)

## Features Implemented / Planned

*(Update this section as you progress)*

*   **User Management:**
    *   [x] User Registration
    *   [x] User Login & JWT Generation
    *   [x] Get User Profile
*   **Product Catalog:**
    *   [x] Create/Read/Update/Delete Products
    *   [ ] List Products with Pagination
*   **Inventory Management:**
    *   [x] Update Stock Levels
    *   [x] Check Product Availability
*   **Order Processing:**
    *   [ ] Create Order (validating user, product, stock)
    *   [ ] Get Order Details
    *   [ ] List User's Orders
    *   [ ] Asynchronous inventory update via Message Queue
*   **API Gateway:**
    *   [ ] Route requests to appropriate microservices
    *   [ ] Basic request filtering/security
*   **Service Discovery:**
    *   [ ] Services register with Eureka
    *   [ ] Gateway uses Eureka for dynamic routing
*   **Centralized Configuration:**
    *   [ ] Services fetch configuration from Config Server
    *   [ ] Dynamic configuration refresh
*   **Resilience:**
    *   [ ] Circuit Breakers on inter-service calls
    *   [ ] Fallback mechanisms
*   **Security:**
    *   [ ] Secure API endpoints using JWT
    *   [ ] Service-to-service security
*   **Observability:**
    *   [ ] Distributed Tracing across services
    *   [ ] Centralized Logging
    *   [ ] Application Metrics exposed to Prometheus
*   **AI Integrations (Planned):**
    *   [ ] Sentiment Analysis for product reviews
    *   [ ] Basic Product Recommendation engine
    *   [ ] Rule-based/Simple ML Fraud Detection

## Getting Started

### Prerequisites

*   JDK 17 or higher
*   Maven 3.6+ or Gradle 7.x+
*   Docker Desktop
*   Git
*   An IDE (IntelliJ IDEA, VS Code with Java/Spring extensions, Eclipse STS)
*   Postman or Insomnia (for testing APIs)

### Setup & Running the Application

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/orderflow.git
    cd orderflow
    ```

2.  **Set up Configuration (if Config Server is implemented):**
    *   Ensure your configuration Git repository (for Spring Cloud Config Server) is set up.
    *   Update the `spring.cloud.config.server.git.uri` in the `config-server`'s `application.yml` if needed.

3.  **Build all services:**
    *(Using Maven Wrapper)*
    ```bash
    ./mvnw clean package -DskipTests
    ```
    *(Or using Gradle Wrapper)*
    ```bash
    ./gradlew build -x test
    ```

4.  **Run using Docker Compose (Recommended for full system):**
    This will start all microservices, databases, message brokers, and observability tools defined in `docker-compose.yml`.
    ```bash
    docker-compose up -d
    ```
    *   *Note: The first time you run this, Docker will download images, which might take some time.*
    *   *Ensure your `docker-compose.yml` is configured correctly.*

5.  **Run individual services (for development/debugging):**
    Navigate to each service directory (e.g., `user-service`) and run:
    *(Using Maven)*
    ```bash
    cd user-service
    ../mvnw spring-boot:run
    ```
    *(Or using Gradle)*
    ```bash
    cd user-service
    ../gradlew bootRun
    ```
    *   *Ensure dependencies like databases, Eureka, Config Server are running if an individual service depends on them.*

### Accessing Services

*   **API Gateway:** `http://localhost:8080` (or your configured gateway port)
*   **Eureka Dashboard:** `http://localhost:8761` (or your configured Eureka port)
*   **Zipkin/Tempo UI:** `http://localhost:9411` (or your configured port)
*   **Prometheus UI:** `http://localhost:9090`
*   **Grafana UI:** `http://localhost:3000`
*   **Kibana/OpenSearch Dashboards:** (Check your Docker Compose config for the port)
*   Individual service ports (e.g., `user-service` on `8081`, `product-service` on `8082`, etc. - check their `application.yml` or Docker Compose).

*(Provide a link to your Postman collection if you create one)*
*   **API Documentation / Postman Collection:** [Link to Postman Collection or Swagger UI if implemented]

## Project Structure

orderflow/
├── api-gateway/
├── config-server/
├── discovery-service/
├── inventory-service/
├── order-service/
├── payment-service/
├── product-service/
├── user-service/
├── recommendation-service/ (optional)
├── analysis-service/ (optional)
├── fraud-detection-service/ (optional)
├── docker-compose.yml
├── pom.xml (For parent project)
└── README.md

## How to Contribute

This is primarily a personal learning project. However, if you have suggestions or find issues, feel free to:
1.  Fork the repository.
2.  Create a new branch (`git checkout -b feature/YourFeature` or `bugfix/YourFix`).
3.  Make your changes.
4.  Commit your changes (`git commit -m 'Add some feature'`).
5.  Push to the branch (`git push origin feature/YourFeature`).
6.  Open a Pull Request.

## License

This project is licensed under the [MIT License](LICENSE.txt) - see the LICENSE.txt file for details (or choose another suitable open-source license).

## Acknowledgements

*   Spring Boot & Spring Cloud Team
*   Baeldung.com for excellent Spring tutorials
*   Various open-source communities whose tools are used in this project.

---

Happy Coding!

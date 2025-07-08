# Patient Management System (WIP)

A comprehensive microservices-based application for managing patient data, authentication, billing, and analytics in a
healthcare environment.

## Features

- **Patient Management**: Create, read, update, and delete patient records with pagination, sorting, and search
  capabilities
- **Authentication**: Secure JWT-based authentication and token validation
- **API Gateway**: Centralized routing, rate limiting, and security for all services
- **Billing Service**: Integration with patient service for billing operations
- **Analytics Service**: Process and analyze patient data through Kafka events
- **Monitoring**: Prometheus metrics and health checks for all services
- **Resilience**: Circuit breakers and retry mechanisms for inter-service communication

## Prerequisites

- Java 21 (JDK for development, JRE for runtime)
- Gradle 8.8+
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL (or H2 for development)
- Redis (for caching and rate limiting)
- Kafka (for event streaming)

## Technologies Used

- **Spring Boot 3.5.0**: Core framework for all microservices
- **Spring Data JPA**: Database access and ORM
- **Spring Security**: Authentication and authorization
- **Spring Cloud Gateway**: API Gateway for routing and filtering
- **Resilience4j**: Circuit breakers and retry mechanisms
- **Kafka**: Event streaming for inter-service communication
- **gRPC**: Synchronous inter-service communication
- **Redis**: Caching and rate limiting
- **PostgreSQL/H2**: Data persistence
- **Docker**: Containerization
- **Prometheus**: Metrics collection
- **Swagger/OpenAPI**: API documentation
- **JUnit**: Testing framework

## Architecture

The application follows a microservices architecture with the following components:

1. **API Gateway**: Entry point for all client requests, handles routing, rate limiting, and JWT validation
2. **Auth Service**: Manages user authentication and token validation
3. **Patient Service**: Core service for patient data management
4. **Billing Service**: Handles billing operations related to patients
5. **Analytics Service**: Processes patient events for analytics purposes
6. **Monitoring**: Prometheus for metrics collection and monitoring

Services communicate through:

- **REST APIs**: For client-facing operations
- **gRPC**: For synchronous inter-service communication
- **Kafka**: For asynchronous event-based communication

## Folder Structure

```
patient-management/
├── analytics-service/       # Service for processing patient analytics
├── api-gateway/             # API Gateway for routing and security
├── api-requests/            # HTTP request examples
├── auth-service/            # Authentication service
├── billing-service/         # Billing operations service
├── infrastructure/          # Infrastructure as code
├── integration-test/        # Integration tests
├── monitoring/              # Prometheus configuration
└── patient-service/         # Core patient management service
```

## Installation

### Local Docker Deployment

1. Clone the repository:
   ```bash
   git clone https://github.com/pramodbharti/patient-management.git
   ``` 
2. Create the following docker containers with images
   ```
   1. auth-service-db (postgres:latest)
   2. patient-service-db (postgres:latest)
   3. kafka (bitnami/kafka:latest)
   4. redis (redis:latest)
   5. prometheus (prom/prometheus:latest)
   6. grafana (grafana/grafana:latest)
   ```
3. Build Docker images for all services:
   ```bash
   docker build -t patient-service:latest ./patient-service
   docker build -t auth-service:latest ./auth-service
   docker build -t billing-service:latest ./billing-service
   docker build -t analytics-service:latest ./analytics-service
   docker build -t api-gateway:latest ./api-gateway
   docker build -t prometheus-prod:latest ./monitoring
   ```
4. Environment Variables
   ```
   analytics-service -> SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
   api-gateway -> AUTH_SERVICE_URL=http://auth-service:4005
   auth-service-db -> POSTGRES_DB=db;POSTGRES_PASSWORD=password;POSTGRES_USER=admin_user
   auth-service -> JWT_SECRET=<yourjwtsecret>;SPRING_DATASOURCE_PASSWORD=password;SPRING_DATASOURCE_URL=jdbc:postgresql://auth-service-db:5432/db;SPRING_DATASOURCE_USERNAME=admin_user;SPRING_JPA_HIBERNATE_DDL_AUTO=update;SPRING_SQL_INIT_MODE=always
   billing-service -> SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
   kafka -> KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094; KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER; KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka:9093; KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT; KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094; KAFKA_CFG_NODE_ID=0; KAFKA_CFG_PROCESS_ROLES=controller,broker
   patient-service-db -> POSTGRES_USER=admin_user; POSTGRES_PASSWORD=password; POSTGRES_DB=db
   patient-service -> BILLING_SERVICE_ADDRESS=billing-service; BILLING_SERVICE_GRPC_PORT=9001; SPRING_CACHE_TYPE=redis; SPRING_DATA_REDIS_HOST=redis; SPRING_DATA_REDIS_PORT=6379; SPRING_DATASOURCE_PASSWORD=password; SPRING_DATASOURCE_URL=jdbc:postgresql://patient-service-db:5432/db; SPRING_DATASOURCE_USERNAME=admin_user; SPRING_JPA_HIBERNATE_DDL_AUTO=update; SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092; SPRING_SQL_INIT_MODE=always
   
   ```
5. Start each image.

6. Start interacting with the app.

#### Local Development with LocalStack

LocalStack is a cloud service emulator that runs in a single container on your local machine, allowing you to develop
and test your cloud applications without connecting to a remote cloud provider. It provides local AWS cloud stack for
developing and testing your cloud applications offline.

##### What is LocalStack?

LocalStack emulates AWS cloud services such as Lambda, S3, DynamoDB, API Gateway, and many others on your local machine.
This allows developers to:

- Develop and test cloud applications without incurring AWS costs
- Work offline without internet connectivity
- Speed up development cycles by eliminating deployment delays
- Test infrastructure changes safely before deploying to production

##### AWS Services Emulated in This Project

In this project, LocalStack is used to emulate the following AWS services:

- **CloudFormation**: For deploying the infrastructure stack
- **S3**: For storing CloudFormation templates
- **VPC**: Virtual network with 2 availability zones
- **ECS/Fargate**: For running containerized microservices
- **RDS PostgreSQL**: Databases for auth and patient services
- **MSK (Managed Streaming for Kafka)**: For event streaming between services
- **ElastiCache Redis**: For caching and rate limiting
- **Application Load Balancer (ALB)**: Entry point for the API Gateway
- **CloudMap**: For service discovery
- **CloudWatch Logs**: For centralized logging
- **Route53**: For health checks and DNS resolution

##### How LocalStack Deployment Works

The `localstack-deploy.sh` script handles the deployment process:

1. Sets up AWS credentials for LocalStack (using dummy values)
2. Creates an S3 bucket in LocalStack for storing CloudFormation templates
3. Uploads the CloudFormation template generated by AWS CDK
4. Deletes any existing CloudFormation stack with the same name
5. Creates a new CloudFormation stack using the template
6. Waits for the stack creation to complete
7. Fetches and displays the Application Load Balancer DNS name

##### Getting Started with LocalStack

1. Start LocalStack:
   ```bash
   docker run -d -p 4566:4566 -p 4510-4559:4510-4559 localstack/localstack
   ```

2. Build the infrastructure module:
   ```bash
   cd infrastructure
   ./gradlew build
   ```

3. Deploy to LocalStack:
   ```bash
   ./localstack-deploy.sh
   ```

## Usage

### Authentication

1. Login to get a JWT token:
   ```bash
   curl -X POST http://localhost:4004/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"password"}'
   ```

2. Use the token for subsequent requests:
   ```bash
   curl -X GET http://localhost:4004/api/patients -H "Authorization: Bearer YOUR_TOKEN"
   ```

### Patient Management

1. Get all patients (with pagination, sorting, and search):
   ```bash
   curl -X GET "http://localhost:4004/api/patients?page=1&size=10&sort=asc&field=name&search=john" -H "Authorization: Bearer YOUR_TOKEN"
   ```

2. Create a new patient:
   ```bash
   curl -X POST http://localhost:4004/api/patients -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" -d '{"name":"John Doe","email":"john@example.com","dateOfBirth":"1990-01-01"}'
   ```

3. Update a patient:
   ```bash
   curl -X PUT http://localhost:4004/api/patients/PATIENT_ID -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" -d '{"name":"John Doe Updated","email":"john@example.com","dateOfBirth":"1990-01-01"}'
   ```

4. Delete a patient:
   ```bash
   curl -X DELETE http://localhost:4004/api/patients/PATIENT_ID -H "Authorization: Bearer YOUR_TOKEN"
   ```

## API Endpoints

### Auth Service

- `POST /auth/login` - Authenticate user and get JWT token
- `GET /auth/validate` - Validate JWT token

### Patient Service

- `GET /api/patients` - Get all patients with pagination, sorting, and search
- `POST /api/patients` - Create a new patient
- `PUT /api/patients/{id}` - Update an existing patient
- `DELETE /api/patients/{id}` - Delete a patient

### API Documentation

- `/api-docs/patients` - OpenAPI documentation for Patient Service
- `/api-docs/auth` - OpenAPI documentation for Auth Service

## License

This project is licensed under the MIT License—see below for details:

```
MIT License

Copyright (c) 2025 Patient Management System by Pramod Bharti

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

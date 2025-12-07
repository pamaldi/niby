# niby

**Niby Application** - A multi-module application built with Quarkus that combines backend services, RAG (Retrieval-Augmented Generation) capabilities, and a user interface. The application provides AI-powered document search and retrieval using vector embeddings and semantic search.

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Project Structure

- **niby-be-core** - Backend core module (port 8080)
- **niby-rag** - RAG module with pgvector (port 8082)
- **niby-ui** - User interface module (port 8081)
- **niby-parent** - Parent POM with shared dependencies

## Running with Docker Compose

### Prerequisites

1. Docker and Docker Compose installed
2. Create `.env` file with your credentials:

```bash
cp .env.example .env
# Edit .env and add your ANTHROPIC_API_KEY
```

### Build all modules

```bash
./mvnw clean package -DskipTests
```

### Start all containers (Development mode)

```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

Or in detached mode:

```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

### Rebuild and restart containers

Rebuild all containers:
```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

Rebuild a specific service:
```bash
# First rebuild the Maven package
cd niby-rag && ../mvnw clean package -DskipTests && cd ..

# Then rebuild and restart the container
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d niby-rag
```

### Stop all containers

```bash
docker-compose down
```

### Access the services

When running with Docker Compose:
- **niby-ui**: http://localhost:8081
- **niby-be-core**:
  - Swagger UI: http://localhost:8080/q/swagger-ui
  - OpenAPI Spec: http://localhost:8080/q/openapi
- **niby-rag**:
  - Swagger UI: http://localhost:8082/q/swagger-ui
  - OpenAPI Spec: http://localhost:8082/q/openapi
- **pgvector**: localhost:5432 (postgres/postgres)

When running locally in dev mode (`./mvnw quarkus:dev`):
- **niby-be-core**:
  - Dev UI: http://localhost:8080/q/dev/
  - Swagger UI: http://localhost:8080/q/swagger-ui
- **niby-rag**:
  - Dev UI: http://localhost:8082/q/dev/
  - Swagger UI: http://localhost:8082/q/swagger-ui

### Production deployment

For production, use Docker secrets:

```bash
# Create secret files
echo -n "your-anthropic-api-key" > secrets/anthropic_api_key.txt
echo -n "your-postgres-password" > secrets/db_password.txt

# Run with production config
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/niby-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)


# Niby Backend Core

The core backend module of Niby, built with Quarkus Framework for supersonic performance and subatomic footprint.

## 📋 Overview

`niby-be-core` is the main backend module that provides the core functionality of the Niby application. It uses Jakarta EE with Quarkus to offer a modern and performant architecture.

## 🚀 Technologies Used

- **Java 21** - Main programming language
- **Quarkus Framework** - Cloud-native application framework
- **Jakarta EE** - Enterprise standard for Java
- **Maven** - Dependency management and build tool

## 🛠️ Prerequisites

- Java 21 or higher
- Maven 3.9.0+
- (Optional) GraalVM for native compilation

## 🏃‍♂️ Running in Development Mode

To start the application in development mode with hot reload:

```bash
./mvnw quarkus:dev
```

### Dev UI
Quarkus provides a Dev UI available only in development mode:
- **Dev UI**: http://localhost:8080/q/dev/
- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **OpenAPI Spec**: http://localhost:8080/q/openapi
- **Features**: Monitoring, configuration, API testing

## 📦 Build and Packaging

### Standard Build

```bash
./mvnw clean package
```

Generates the `quarkus-run.jar` file in the `target/quarkus-app/` directory.

### Running the Application

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

### Uber-JAR Build
To create a single JAR with all dependencies:

```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

Running:

```bash
java -jar target/*-runner.jar
```

## 🏗️ Native Compilation

### With GraalVM Installed

```bash
./mvnw package -Dnative
```

### With Container (Without GraalVM)

```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

### Native Execution
```bash
./target/niby-be-core-1.0-SNAPSHOT-runner
```

## 🧪 Testing

### Running Tests

```bash
./mvnw test
```

### Integration Tests

```bash
./mvnw verify
```

## 📁 Project Structure

```
niby-be-core/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
├── target/
├── pom.xml
└── README.md
```

## ⚙️ Configuration

Application configurations are located in:
* `src/main/resources/application.properties` - Main configuration
* `src/main/resources/application-dev.properties` - Development configuration
* `src/main/resources/application-prod.properties` - Production configuration

## 🔗 Useful Links

* [Quarkus Documentation](https://quarkus.io/guides/)
* [Maven Tooling Guide](https://quarkus.io/guides/maven-tooling)
* [Jakarta EE Specifications](https://jakarta.ee/specifications/)

## 🤝 Contributing

To contribute to the project:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push the branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## 📄 License

This project is part of the Niby ecosystem developed by Cloud Isaura.

---

**Note**: For specific information about the architecture and APIs, please refer to the project's technical documentation.
```

The translation maintains all the technical content while adapting the language naturally for an English-speaking audience. The structure, commands, and formatting remain identical to ensure technical accuracy.
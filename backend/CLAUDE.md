# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.example.demo.DemoApplicationTests"

# Run a specific test method
./gradlew test --tests "com.example.demo.DemoApplicationTests.contextLoads"

# Clean build
./gradlew clean build
```

## Project Overview

Spring Boot 4.0.0 backend application (Java 25) designed to work with a React frontend. Uses Gradle 9.2.1 as the build tool.

### Tech Stack
- **Framework**: Spring Boot 4.0.0 with Spring Security, Spring Data JPA, Spring Web MVC
- **Database**: H2 (in-memory) with JPA
- **Build**: Gradle with Lombok annotation processing
- **Testing**: JUnit 5 via Spring Boot Test

## Architecture

Standard layered Spring Boot architecture for user management:

```
com.example.demo/
├── DemoApplication.java      # Application entry point
├── config/
│   └── WebConfig.java        # CORS configuration for React (port 3000) -> Spring (port 8080)
├── controller/
│   └── UserController.java   # REST endpoints for login/registration
├── domain/
│   └── User.java             # JPA entity (id, password, name)
└── repository/
    └── UserRepository.java   # JPA repository extending JpaRepository
```

### Request Flow
React (3000) -> UserController -> UserRepository -> H2 Database

## Key Configuration

- CORS must be configured in `WebConfig` to allow React frontend communication
- H2 console available via `spring-boot-h2console` dependency
- Spring Security is enabled - configure security rules as needed
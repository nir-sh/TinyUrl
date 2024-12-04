# TinyURL Service

A scalable and efficient TinyURL clone that allows users to shorten long URLs into compact, easy-to-share links. This project is built with Java, uses Spring Boot, and leverages different databases for specialized purposes.

## Features

- **URL Shortening**: Quickly shorten any long URL.
- **High-Performance Data Handling**: Optimized database usage with Redis, MongoDB, and Cassandra for specific use cases.
- **Docker Support**: Use Docker Compose to easily spin up the application with all necessary services.

## Technology Stack

- **Backend**: Java, Spring Boot
- **Databases**:
  - **Redis**: For quick extraction of shortened URLs.
  - **MongoDB**: For user reporting and tracking clicks per user.
  - **Cassandra**: To document each click's details, designed to handle high-scale traffic.
- **Containerization**: Docker, Docker Compose

## Getting Started

### Prerequisites

- **Java 17**
- **Maven**
- **Docker & Docker Compose**

### Installation

1. **Clone the repository**:

   ```bash
   git clone <repository-url>
   cd nirsh-tinyurl-main
   ```

2. **Build the project**:

   ```bash
   mvn clean install
   ```

3. **Run with Docker Compose**:

   ```bash
   docker-compose up
   ```

### Running Without Docker

If you prefer to run the application without Docker:

1. **Set up the Databases**: Make sure Redis, MongoDB, and Cassandra are running and properly configured.
2. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```

## Usage

- Once the application is running, navigate to `http://localhost:8080`.
- Use the provided UI or API to shorten URLs.

## Configuration

- Update the `application.properties` or `application.yml` in the `src/main/resources` directory to change configurations such as the database connection.

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or suggestions, please contact Nir Shaulian.


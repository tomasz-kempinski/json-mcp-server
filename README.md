# json-mcp-server

[![Java Version](https://img.shields.io/badge/Java-25-blue.svg)](https://jdk.java.net/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

`json-mcp-server` is a [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server that enables AI models to interact with local JSON files. It provides a robust set of tools for querying, schema inference, and validation, allowing LLMs to process structured data efficiently.

Built with **Spring AI** and leveraging **Java 25**, it offers high performance and modern language features.

## 🚀 Features

- **JSON Querying**: Full support for [JsonPath](https://github.com/json-path/JsonPath) expressions to filter and extract specific data.
- **Schema Inference**: Automatically generate JSON Schema from existing JSON files to help LLMs understand data structures.
- **Schema Validation**: Built-in tools to validate JSON Schemas.
- **Dual Transport Modes**: Support for both HTTP (SSE) and Standard Input/Output (Stdio) for maximum compatibility with different MCP clients.
- **Cloud-Native**: Fully containerized with Docker, featuring a secure non-root user and multi-stage builds.

## 🧰 MCP Tools Reference

### `query_json`
Searches a local JSON file using a JsonPath query and returns the results.

- **Arguments**:
  - `absolutePath` (string): The absolute path to the local JSON file.
  - `query` (string): The JsonPath expression to execute.
- **Example Queries**:
  - `$.users[*].id`
  - `$.orders[?(@.price > 50.0)]`
  - `$.store.book[0].author`

### `generate_json_schema`
Generates a JSON Schema inferred from the structure of a provided JSON file.

- **Arguments**:
  - `absolutePath` (string): The absolute path to the JSON file to analyze.

### `validate_json_schema`
Validates that a JSON schema is properly formed according to meta-schemas.

- **Arguments**:
  - `schema` (string, optional): The JSON schema string to validate.
  - `schemaFilePath` (string, optional): Path to a file containing the JSON schema.

## 🛠️ Built With

- **[Spring Boot 3.5.9](https://spring.io/projects/spring-boot)**: The backbone of the application.
- **[Spring AI](https://spring.io/projects/spring-ai)**: Providing the Model Context Protocol (MCP) server implementation.
- **[JsonPath](https://github.com/json-path/JsonPath)**: Used for evaluating expressions in `query_json`.
- **[json-schema-inferrer](https://github.com/saasquatch/json-schema-inferrer)**: Powers the `generate_json_schema` tool.
- **[json-schema-validator](https://github.com/networknt/json-schema-validator)**: Used in `validate_json_schema` to ensure schema correctness.
- **[Lombok](https://projectlombok.org/)**: For cleaner, more concise Java code.

## 🛠 Prerequisites

- **Docker** (Recommended)
- **Java 25** (For local development)
- **Gradle 9.1.0+** (For local builds)

## 📦 Quick Start

### Running with Docker

The easiest way to run the server is using Docker.

#### 1. Build the Image
```bash
docker build -t json-mcp-server .
```

#### 2. Run the Server

**HTTP (Streaming) Mode** (Default)
Ideal for web-based or remote clients.
```bash
docker run --name json-mcp-server \
  -v /path/to/local/json/files:/tmp \
  -p 3000:3000 \
  -e MCP_LOG_LEVEL=DEBUG \
  json-mcp-server
```

**Stdio Mode**
Best for local LLM clients like **Claude Desktop**.
```bash
docker run --name json-mcp-server -i --rm \
  -v /path/to/local/json/files:/tmp \
  -e SPRING_PROFILES_ACTIVE=stdio \
  json-mcp-server
```

> [!NOTE]
> Mounting your local directory to `/tmp` allows the server to access your files. When using the tools, use absolute paths starting with `/tmp/` (e.g., `/tmp/data.json`).

---

### Local Development

#### Build the Project
```bash
./gradlew bootJar
```

#### Run Locally
```bash
java -jar build/libs/json-mcp-server-0.0.1-SNAPSHOT.jar
```

## ⚙️ Configuration

The server can be customized using environment variables:

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `SERVER_PORT` | `3000` | Port for the HTTP web server. |
| `SPRING_PROFILES_ACTIVE` | `default` | Set to `stdio` for Stdio mode, or `default` for HTTP mode. |
| `MCP_LOG_LEVEL` | `INFO` | Logging level for MCP components (`DEBUG`, `INFO`, `WARN`, `ERROR`). |
| `MCP_TOOLS_FILE_ALLOWED_EXTENSIONS` | `.json` | Comma-separated list of allowed file extensions. |

## 🧪 Testing

The project contains a comprehensive test suite including unit and integration tests.
```bash
./gradlew test
```

## 🗺️ Roadmap

- **Spring Boot 4 Upgrade**: Future plan to upgrade to **Spring Boot 4++** once a stable release of **Spring AI** supporting it is available.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

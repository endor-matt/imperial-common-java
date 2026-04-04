# Imperial Common Java

Shared utilities for Death Star Operations Platform services.

## Overview

Imperial Common Java provides reusable library modules consumed by all services in the Death Star Operations Platform. It standardizes cryptographic operations, database query construction, HTTP communication, configuration management, audit logging, and data serialization across the fleet.

## Modules

| Module | Package | Description |
|--------|---------|-------------|
| **crypto** | `com.deathstar.common.crypto` | Encryption, hashing, and key management utilities |
| **query** | `com.deathstar.common.query` | Database query builder for dynamic query construction |
| **http** | `com.deathstar.common.http` | HTTP client wrapper for inter-service communication |
| **config** | `com.deathstar.common.config` | Configuration loading and environment-aware property resolution |
| **audit** | `com.deathstar.common.audit` | Structured audit logging for compliance and traceability |
| **codec** | `com.deathstar.common.codec` | Data serialization and deserialization utilities |

## Requirements

- Java 17+
- Maven 3.8+

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.deathstar</groupId>
    <artifactId>imperial-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

You will also need to configure the GitHub Packages Maven registry in your `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_GITHUB_TOKEN</password>
    </server>
</servers>
```

## Building

```bash
mvn clean install
```

## Publishing

```bash
mvn deploy
```

## License

Proprietary -- Imperial Engineering Corps

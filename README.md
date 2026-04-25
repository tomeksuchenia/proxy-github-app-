# Proxy GitHub App

## Description

The application acts as a middleware (proxy) to the GitHub API. It fetches a user's repository list (excluding forks)
and then retrieves the branch list (including the last commit SHA) for each repository.

## Requirements

- Java 25
- Spring Boot 4.0
- Maven

## Configuration

Before running the application, you must set the `GITHUB_API_TOKEN` environment variable, which is required for GitHub
API authorization.

> [!IMPORTANT]
> If the `GITHUB_API_TOKEN` is not set, the application will return an **HTTP 500 Internal Server Error**.

### Setting the Environment Variable

**Windows (CMD):**

```cmd
set GITHUB_API_TOKEN=your_token_here
```

**Windows (PowerShell):**

```powershell
$Env:GITHUB_API_TOKEN = "your_token_here"
```

**Linux/macOS:**

```bash
export GITHUB_API_TOKEN=your_token_here
```

## Building the Application

To build the project, run:

```bash
mvn clean install
```

## Running

After building and setting the environment variable:

```bash
java -jar target/proxy-github-app-1.0-SNAPSHOT.jar
```

Or directly via Maven:

```bash
mvn spring-boot:run
```

## Endpoint

GET `http://localhost:8080/api/v1/github/users/{username}/repos`

Example:

```
GET http://localhost:8080/api/v1/github/users/tomeksuchenia/repos
```

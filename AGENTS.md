# AGENTS.md - API Testing Project

## Project Overview
- **Type**: Java/Gradle API Testing Framework
- **Test Framework**: TestNG 7.7.0
- **API Testing Library**: REST Assured 5.3.0
- **Build Tool**: Gradle

---

## Build & Test Commands

### Run All Tests
```bash
./gradlew test
```

### Run Single Test Class
```bash
./gradlew test --tests "api.ApiTest"
./gradlew test --tests "api.SoapRequestTest"
./gradlew test --tests "auth.BasicAuth"
```

### Run Single Test Method
```bash
./gradlew test --tests "api.ApiTest.testGetRequest"
./gradlew test --tests "api.SoapRequestTest.testSoapRequestWithAssertions"
```

### Build Project
```bash
./gradlew build
```

### Clean Build
```bash
./gradlew clean
```

### Clean and Rebuild
```bash
./gradlew clean build
```

---

## Code Style Guidelines

### Project Structure
```
src/
├── main/java/          # Production code (HttpClient, utilities)
└── test/java/          # Test code
    ├── api/            # API endpoint tests
    └── auth/           # Authentication tests
```

### Package Naming
- Use lowercase with dots: `com.example.api`
- Package declaration matches directory structure

### Class Naming Conventions
- **Classes**: PascalCase (`ApiTest`, `HttpClient`, `BasicAuth`)
- **Test Classes**: End with `Test` (`ApiTest`, `SoapRequestTest`)
- **Methods**: camelCase (`testGetRequest`, `testBasicAuth`)

### Test Method Naming
- Prefix with `test`: `testGetRequest()`, `testSoapRequestWithAssertions()`
- Use descriptive names explaining what is tested

### Import Organization
Order groups with blank lines between:
1. Static imports (`import static ...`)
2. org.testng imports
3. io.restassured imports
4. Other imports (alphabetical)

```java
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.Test;
import org.testng.Assert;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
```

### Code Formatting
- **Indentation**: 4 spaces (Java default)
- **Line length**: Keep under 120 characters when reasonable
- **Braces**: Opening brace on same line
- **Text blocks**: Use `"""` for JSON/XML payloads

### Variable Naming
- **Variables**: camelCase (`baseUri`, `response`, `authToken`)
- **Constants**: UPPER_SNAKE_CASE (`ANSI_GREEN`, `BASE_URL`)
- **Instance variables**: camelCase with `this` prefix when needed

### Type Usage
- Explicit types preferred over `var` for clarity
- Use interfaces when possible (`List<String>` over `ArrayList<String>`)

### Assertions
Use REST Assured chain syntax:
```java
.then()
    .statusCode(200)
    .body("userId", equalTo(1))
    .body("id", equalTo(1));
```

For extracted responses:
```java
response.then().body("name", equalTo(expectedName));
```

TestNG Assert for complex validations:
```java
Assert.assertEquals(result, "expected", "Message on failure");
```

### Error Handling
- Log request/response with `.log().all()` during development
- Validate status codes explicitly
- Include descriptive assertion messages

### HTTP Methods
```java
given()
    .baseUri("https://api.example.com")
    .basePath("/users")
    .contentType(ContentType.JSON)
    .body(requestBody)
    .when()
    .post()  // or .get(), .put(), .delete(), .patch()
    .then()
    .statusCode(201);
```

### Authentication
Basic Auth:
```java
.auth().basic("user", "password")
```

Or manual header:
```java
.header("Authorization", "Basic " + base64Credentials)
```

---

## Test Organization

### Test Class Structure
```java
package api;

import org.testng.annotations.Test;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ApiTest {

    @Test
    public void testEndpoint() {
        // Test implementation
    }
}
```

### Test Data
- Inline for simple payloads
- Consider helper methods for repeated data
- Use text blocks for JSON/XML

---

## Dependencies

### Core Dependencies
- **TestNG**: 7.7.0 - Test framework
- **REST Assured**: 5.3.0 - API testing DSL
- **Jackson**: 2.15.0 - JSON parsing
- **Hamcrest**: 3.0 - Assertions
- **SLF4J**: 2.0.7 - Logging

---

## Common Patterns

### Extract Response Data
```java
Response response = given()
    .when()
    .get("/endpoint")
    .then()
    .extract().response();

String value = response.jsonPath().getString("field");
int id = response.jsonPath().getInt("id");
```

### SOAP Requests
```java
String soapBody = """
    <soap:Envelope xmlns:soap="...">
        <soap:Body>
            <tns:Operation>
                <tns:param>value</tns:param>
            </tns:Operation>
        </soap:Body>
    </soap:Envelope>
    """;

given()
    .contentType("text/xml; charset=UTF-8")
    .body(soapBody)
    .when()
    .post("/endpoint")
    .then()
    .statusCode(200);
```

### Validation Helpers
- Use Hamcrest matchers: `equalTo()`, `containsString()`, `hasSize()`
- Chain validations in single `.then()` block when possible
- Extract and validate separately for complex scenarios

---

## Notes
- Tests use real public APIs (jsonplaceholder, rickandmortyapi, httpbin)
- Test output includes colored console logs for debugging
- XML path support via `xmlPath()` method

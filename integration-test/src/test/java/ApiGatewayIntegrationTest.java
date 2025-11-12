import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiGatewayIntegrationTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    private String getToken() {
        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;

        return given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");
    }

    @Test
    void shouldRouteAuthRequestsCorrectly() {
        // Test that auth requests are routed correctly
        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;

        given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    void shouldRoutePatientRequestsCorrectly() {
        // Test that patient requests are routed correctly
        String token = getToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200)
                .body("patients", notNullValue());
    }

    @Test
    void shouldRequireJwtForProtectedEndpoints() {
        // Test that JWT validation is enforced
        given()
                .when()
                .get("/api/patients")
                .then()
                .statusCode(401); // Unauthorized without token
    }

    @Test
    void shouldRejectInvalidJwt() {
        // Test that invalid JWT is rejected
        String invalidToken = "invalid.token.here";

        given()
                .header("Authorization", "Bearer " + invalidToken)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(401); // Unauthorized with invalid token
    }

    @Test
    void shouldApplyRateLimiting() throws InterruptedException {
        // Test that rate limiting is applied
        String token = getToken();
        final int REQUESTS_TO_TRIGGER_LIMIT = 10;
        int tooManyRequests = 0;

        // Make requests until just before the limit
        for (int i = 1; i <= REQUESTS_TO_TRIGGER_LIMIT; i++) {
            Response response = given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/patients");
            System.out.printf("Request %d -> Status: %d%n", i, response.statusCode());
            if (response.statusCode() == 429) {
                tooManyRequests++;
            }
            Thread.sleep(100);
        }

        assertTrue(tooManyRequests >= 1, "Expected at least 1 request to be blocked");
    }

    @Test
    void shouldRouteApiDocsCorrectly() {
        // Test that API docs requests are routed correctly
        given()
                .when()
                .get("/api-docs/patients")
                .then()
                .statusCode(200);
    }
}
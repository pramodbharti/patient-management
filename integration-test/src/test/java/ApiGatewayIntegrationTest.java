import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiGatewayIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(ApiGatewayIntegrationTest.class);

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
    void shouldRequireAuthenticationForPatientRequests() {
        // Test that patient requests require authentication
        given()
                .when()
                .get("/api/patients")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldRejectInvalidTokenForPatientRequests() {
        // Test that patient requests reject invalid tokens
        given()
                .header("Authorization", "Bearer invalid-token")
                .when()
                .get("/api/patients")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldRouteApiDocsCorrectly() {
        // Test that API docs requests are routed correctly
        given()
                .when()
                .get("/api-docs/patients")
                .then()
                .statusCode(200);

        given()
                .when()
                .get("/api-docs/auth")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldReturn404ForNonExistentEndpoints() {
        // Test that non-existent endpoints return 404
        given()
                .when()
                .get("/non-existent-endpoint")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldApplyRateLimitingToRequests() throws InterruptedException {
        // Test that rate limiting is applied to requests
        String token = getToken();
        final int REQUESTS_TO_TRIGGER_LIMIT = 10;
        int tooManyRequests = 0;

        // Make requests until just before the limit
        for (int i = 1; i <= REQUESTS_TO_TRIGGER_LIMIT; i++) {
            Response response = given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/patients");
            log.info("Request {} -> Status: {}", i, response.statusCode());
            if (response.statusCode() == 429) {
                tooManyRequests++;
            }
            Thread.sleep(100);
        }

        // Assert that at least one request was rate limited
        log.info("Number of rate-limited requests: {}", tooManyRequests);
        assert tooManyRequests >= 1 : "Expected at least 1 request to be rate limited";
    }
}
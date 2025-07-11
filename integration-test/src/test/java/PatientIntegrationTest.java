import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PatientIntegrationTest {

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
    public void shouldReturnPatientsWithValidToken() {
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
    public void shouldReturn429WhenRateLimitExceeded() throws InterruptedException {
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


}

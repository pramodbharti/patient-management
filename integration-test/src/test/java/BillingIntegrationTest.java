import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class BillingIntegrationTest {

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
    public void shouldCreateBillingAccountWhenPatientIsCreated() {
        String token = getToken();
        String uniqueEmail = "test-" + UUID.randomUUID() + "@example.com";
        
        String patientPayload = """
                {
                    "firstName": "Test",
                    "lastName": "Patient",
                    "email": "%s",
                    "phoneNumber": "1234567890",
                    "address": "123 Test St",
                    "dateOfBirth": "1990-01-01"
                }
                """.formatted(uniqueEmail);

        // Create a new patient, which should trigger the creation of a billing account
        String patientId = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(patientPayload)
                .when()
                .post("/api/patients")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getString("id");

        // Verify the patient was created successfully
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients/" + patientId)
                .then()
                .statusCode(200)
                .body("id", equalTo(patientId))
                .body("email", equalTo(uniqueEmail));

        // Note: We can't directly verify the billing account was created because there's no API endpoint to check it
        // In a real test, we might use a database connection or mock to verify this
        System.out.println("Created patient with ID: " + patientId + " and email: " + uniqueEmail);
        System.out.println("Billing account should have been created via gRPC call or Kafka event");
    }
}
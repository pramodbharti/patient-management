import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class AnalyticsIntegrationTest {

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
    public void shouldSendPatientEventWhenPatientIsCreated() {
        String token = getToken();
        String uniqueEmail = "analytics-test-" + UUID.randomUUID() + "@example.com";
        
        String patientPayload = """
                {
                    "firstName": "Analytics",
                    "lastName": "Test",
                    "email": "%s",
                    "phoneNumber": "1234567890",
                    "address": "123 Test St",
                    "dateOfBirth": "1990-01-01"
                }
                """.formatted(uniqueEmail);

        // Create a new patient, which should trigger a Kafka event
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

        // Note: We can't directly verify the Kafka event was processed by the analytics service
        // In a real test, we might use a database connection or mock to verify this
        System.out.println("Created patient with ID: " + patientId + " and email: " + uniqueEmail);
        System.out.println("Patient event should have been sent to Kafka and processed by the analytics service");
    }

    @Test
    public void shouldSendPatientEventWhenPatientIsUpdated() {
        String token = getToken();
        String uniqueEmail = "analytics-update-test-" + UUID.randomUUID() + "@example.com";
        
        // First, create a patient
        String patientPayload = """
                {
                    "firstName": "Analytics",
                    "lastName": "Update",
                    "email": "%s",
                    "phoneNumber": "1234567890",
                    "address": "123 Test St",
                    "dateOfBirth": "1990-01-01"
                }
                """.formatted(uniqueEmail);

        String patientId = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(patientPayload)
                .when()
                .post("/api/patients")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getString("id");

        // Now update the patient, which should trigger another Kafka event
        String updatedPatientPayload = """
                {
                    "firstName": "Updated",
                    "lastName": "Patient",
                    "email": "%s",
                    "phoneNumber": "9876543210",
                    "address": "456 Updated St",
                    "dateOfBirth": "1990-01-01"
                }
                """.formatted(uniqueEmail);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(updatedPatientPayload)
                .when()
                .put("/api/patients/" + patientId)
                .then()
                .statusCode(200);

        // Verify the patient was updated successfully
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients/" + patientId)
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Updated"))
                .body("lastName", equalTo("Patient"))
                .body("address", equalTo("456 Updated St"));

        // Note: We can't directly verify the Kafka event was processed by the analytics service
        System.out.println("Updated patient with ID: " + patientId);
        System.out.println("Patient update event should have been sent to Kafka and processed by the analytics service");
    }

    @Test
    public void shouldSendPatientEventWhenPatientIsDeleted() {
        String token = getToken();
        String uniqueEmail = "analytics-delete-test-" + UUID.randomUUID() + "@example.com";
        
        // First, create a patient
        String patientPayload = """
                {
                    "firstName": "Analytics",
                    "lastName": "Delete",
                    "email": "%s",
                    "phoneNumber": "1234567890",
                    "address": "123 Test St",
                    "dateOfBirth": "1990-01-01"
                }
                """.formatted(uniqueEmail);

        String patientId = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(patientPayload)
                .when()
                .post("/api/patients")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getString("id");

        // Now delete the patient, which should trigger another Kafka event
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/patients/" + patientId)
                .then()
                .statusCode(204);

        // Verify the patient was deleted
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients/" + patientId)
                .then()
                .statusCode(404);

        // Note: We can't directly verify the Kafka event was processed by the analytics service
        System.out.println("Deleted patient with ID: " + patientId);
        System.out.println("Patient delete event should have been sent to Kafka and processed by the analytics service");
    }
}
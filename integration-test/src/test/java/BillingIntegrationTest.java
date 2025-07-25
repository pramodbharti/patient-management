import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BillingIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(BillingIntegrationTest.class);
    private static BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    @BeforeAll
    static void setUp() {
        String serverAddress = "localhost";
        int serverPort = 9001;
        log.info("Connecting to billing GRPC service at {}:{}", serverAddress, serverPort);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();
        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    @Test
    void shouldCreateBillingAccount() {
        // Arrange
        String patientId = "12345";
        String patientName = "Test Patient";
        String patientEmail = "test@example.com";
        
        BillingRequest billingRequest = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(patientName)
                .setEmail(patientEmail)
                .build();
        
        // Act
        BillingResponse response = blockingStub.createBillingAccount(billingRequest);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertEquals("ACTIVE", response.getStatus());
        log.info("Received response from billing service: {}", response);
    }

    @Test
    void shouldHandleEmptyPatientId() {
        // Arrange
        String patientId = "";
        String patientName = "Test Patient";
        String patientEmail = "test@example.com";
        
        BillingRequest billingRequest = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(patientName)
                .setEmail(patientEmail)
                .build();
        
        // Act
        BillingResponse response = blockingStub.createBillingAccount(billingRequest);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void shouldHandleEmptyName() {
        // Arrange
        String patientId = "12345";
        String patientName = "";
        String patientEmail = "test@example.com";
        
        BillingRequest billingRequest = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(patientName)
                .setEmail(patientEmail)
                .build();
        
        // Act
        BillingResponse response = blockingStub.createBillingAccount(billingRequest);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void shouldHandleEmptyEmail() {
        // Arrange
        String patientId = "12345";
        String patientName = "Test Patient";
        String patientEmail = "";
        
        BillingRequest billingRequest = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(patientName)
                .setEmail(patientEmail)
                .build();
        
        // Act
        BillingResponse response = blockingStub.createBillingAccount(billingRequest);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertEquals("ACTIVE", response.getStatus());
    }
}
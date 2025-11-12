import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BillingIntegrationTest {

    private static ManagedChannel channel;
    private static BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    @BeforeAll
    static void setUp() {
        // Connect to the billing service
        channel = ManagedChannelBuilder.forAddress("localhost", 9001)
                .usePlaintext()
                .build();
        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    static void tearDown() throws InterruptedException {
        // Shutdown the channel
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void shouldCreateBillingAccountSuccessfully() {
        // Arrange
        String patientId = "test-patient-id";
        String name = "Test Patient";
        String email = "test@example.com";
        
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build();
        
        // Act
        BillingResponse response = blockingStub.createBillingAccount(request);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertEquals("ACTIVE", response.getStatus());
        
        System.out.println("Created billing account with ID: " + response.getAccountId());
    }
    
    @Test
    void shouldHandleEmptyPatientId() {
        // Arrange
        String patientId = "";
        String name = "Test Patient";
        String email = "test@example.com";
        
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build();
        
        // Act
        BillingResponse response = blockingStub.createBillingAccount(request);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertEquals("ACTIVE", response.getStatus());
    }
    
    @Test
    void shouldHandleEmptyName() {
        // Arrange
        String patientId = "test-patient-id";
        String name = "";
        String email = "test@example.com";
        
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build();
        
        // Act
        BillingResponse response = blockingStub.createBillingAccount(request);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertEquals("ACTIVE", response.getStatus());
    }
}
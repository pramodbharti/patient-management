import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import patient.events.PatientEvent;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AnalyticsIntegrationTest {

    private static KafkaProducer<String, byte[]> producer;

    @BeforeAll
    static void setUp() {
        // Set up Kafka producer
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        
        producer = new KafkaProducer<>(props);
    }

    @AfterAll
    static void tearDown() {
        // Close the producer
        producer.close(10, TimeUnit.SECONDS);
    }

    @Test
    void shouldSendPatientCreatedEvent() throws ExecutionException, InterruptedException {
        // Arrange
        String patientId = UUID.randomUUID().toString();
        String name = "Test Patient";
        String email = "test@example.com";
        
        PatientEvent event = PatientEvent.newBuilder()
                .setEventType("PATIENT_CREATED")
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build();
        
        // Act
        ProducerRecord<String, byte[]> record = new ProducerRecord<>("patient", patientId, event.toByteArray());
        producer.send(record).get(); // Wait for the send to complete
        
        // Assert
        // Note: In a real test, we would verify that the analytics service processed the event correctly.
        // This might involve checking a database, logs, or some other output from the service.
        // For this integration test, we're just verifying that we can send events to Kafka.
        System.out.println("Sent PATIENT_CREATED event for patient ID: " + patientId);
    }
    
    @Test
    void shouldSendPatientUpdatedEvent() throws ExecutionException, InterruptedException {
        // Arrange
        String patientId = UUID.randomUUID().toString();
        String name = "Updated Patient";
        String email = "updated@example.com";
        
        PatientEvent event = PatientEvent.newBuilder()
                .setEventType("PATIENT_UPDATED")
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build();
        
        // Act
        ProducerRecord<String, byte[]> record = new ProducerRecord<>("patient", patientId, event.toByteArray());
        producer.send(record).get(); // Wait for the send to complete
        
        // Assert
        System.out.println("Sent PATIENT_UPDATED event for patient ID: " + patientId);
    }
    
    @Test
    void shouldSendPatientDeletedEvent() throws ExecutionException, InterruptedException {
        // Arrange
        String patientId = UUID.randomUUID().toString();
        
        PatientEvent event = PatientEvent.newBuilder()
                .setEventType("PATIENT_DELETED")
                .setPatientId(patientId)
                .build();
        
        // Act
        ProducerRecord<String, byte[]> record = new ProducerRecord<>("patient", patientId, event.toByteArray());
        producer.send(record).get(); // Wait for the send to complete
        
        // Assert
        System.out.println("Sent PATIENT_DELETED event for patient ID: " + patientId);
    }
}
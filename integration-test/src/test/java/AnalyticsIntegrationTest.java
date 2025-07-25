import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patient.events.PatientEvent;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AnalyticsIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsIntegrationTest.class);
    private static KafkaProducer<String, byte[]> kafkaProducer;
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String PATIENT_TOPIC = "patient";

    @BeforeAll
    static void setUp() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.ByteArraySerializer.class.getName());
        
        kafkaProducer = new KafkaProducer<>(props);
        log.info("Kafka producer initialized with bootstrap servers: {}", KAFKA_BOOTSTRAP_SERVERS);
    }

    @Test
    void shouldSendPatientCreatedEvent() throws ExecutionException, InterruptedException {
        // Arrange
        String patientId = "12345";
        String patientName = "Test Patient";
        String patientEmail = "test@example.com";
        
        PatientEvent patientEvent = PatientEvent.newBuilder()
                .setPatientId(patientId)
                .setName(patientName)
                .setEmail(patientEmail)
                .setEventType("PATIENT_CREATED")
                .build();
        
        // Act
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(PATIENT_TOPIC, patientEvent.toByteArray());
        
        // Assert - We can only verify that the message was sent successfully, not that it was processed
        var result = kafkaProducer.send(record).get();
        log.info("Message sent to topic: {}, partition: {}, offset: {}", 
                result.topic(), result.partition(), result.offset());
        
        // Give the analytics service some time to process the message
        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    void shouldHandleEmptyPatientId() throws ExecutionException, InterruptedException {
        // Arrange
        String patientId = "";
        String patientName = "Test Patient";
        String patientEmail = "test@example.com";
        
        PatientEvent patientEvent = PatientEvent.newBuilder()
                .setPatientId(patientId)
                .setName(patientName)
                .setEmail(patientEmail)
                .setEventType("PATIENT_CREATED")
                .build();
        
        // Act
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(PATIENT_TOPIC, patientEvent.toByteArray());
        
        // Assert - We can only verify that the message was sent successfully, not that it was processed
        var result = kafkaProducer.send(record).get();
        log.info("Message sent to topic: {}, partition: {}, offset: {}", 
                result.topic(), result.partition(), result.offset());
        
        // Give the analytics service some time to process the message
        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    void shouldHandleEmptyName() throws ExecutionException, InterruptedException {
        // Arrange
        String patientId = "12345";
        String patientName = "";
        String patientEmail = "test@example.com";
        
        PatientEvent patientEvent = PatientEvent.newBuilder()
                .setPatientId(patientId)
                .setName(patientName)
                .setEmail(patientEmail)
                .setEventType("PATIENT_CREATED")
                .build();
        
        // Act
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(PATIENT_TOPIC, patientEvent.toByteArray());
        
        // Assert - We can only verify that the message was sent successfully, not that it was processed
        var result = kafkaProducer.send(record).get();
        log.info("Message sent to topic: {}, partition: {}, offset: {}", 
                result.topic(), result.partition(), result.offset());
        
        // Give the analytics service some time to process the message
        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    void shouldHandleEmptyEmail() throws ExecutionException, InterruptedException {
        // Arrange
        String patientId = "12345";
        String patientName = "Test Patient";
        String patientEmail = "";
        
        PatientEvent patientEvent = PatientEvent.newBuilder()
                .setPatientId(patientId)
                .setName(patientName)
                .setEmail(patientEmail)
                .setEventType("PATIENT_CREATED")
                .build();
        
        // Act
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(PATIENT_TOPIC, patientEvent.toByteArray());
        
        // Assert - We can only verify that the message was sent successfully, not that it was processed
        var result = kafkaProducer.send(record).get();
        log.info("Message sent to topic: {}, partition: {}, offset: {}", 
                result.topic(), result.partition(), result.offset());
        
        // Give the analytics service some time to process the message
        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    void shouldHandleDifferentEventType() throws ExecutionException, InterruptedException {
        // Arrange
        String patientId = "12345";
        String patientName = "Test Patient";
        String patientEmail = "test@example.com";
        
        PatientEvent patientEvent = PatientEvent.newBuilder()
                .setPatientId(patientId)
                .setName(patientName)
                .setEmail(patientEmail)
                .setEventType("PATIENT_UPDATED")
                .build();
        
        // Act
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(PATIENT_TOPIC, patientEvent.toByteArray());
        
        // Assert - We can only verify that the message was sent successfully, not that it was processed
        var result = kafkaProducer.send(record).get();
        log.info("Message sent to topic: {}, partition: {}, offset: {}", 
                result.topic(), result.partition(), result.offset());
        
        // Give the analytics service some time to process the message
        TimeUnit.SECONDS.sleep(2);
    }
}
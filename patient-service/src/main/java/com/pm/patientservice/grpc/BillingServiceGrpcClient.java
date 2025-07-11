package com.pm.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import com.pm.patientservice.kafka.KafkaProducer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;
    private final KafkaProducer kafkaProducer;

    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.port:9001}") int serverPort, KafkaProducer kafkaProducer) {
        log.info("Connecting to billing GRPC service at {}:{}", serverAddress, serverPort);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();
        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
        this.kafkaProducer = kafkaProducer;
    }

    @CircuitBreaker(name = "billing-service", fallbackMethod = "billingFallback")
    @Retry(name = "billing-retry")
    public BillingResponse createBillingAccount(String patientId, String patientName, String patientEmail) {
        BillingRequest billingRequest = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(patientName)
                .setEmail(patientEmail)
                .build();
        BillingResponse response = blockingStub.createBillingAccount(billingRequest);
        log.info("Received response from billing service via GRPC: {}", response);
        return response;
    }

    public BillingResponse billingFallback(String patientId, String patientName, String patientEmail, Throwable throwable) {
        log.error("[CIRCUIT BREAKER]: Error calling billing service via GRPC: {}", throwable.getMessage());
        kafkaProducer.sendBillingAccountEvent(patientId, patientName, patientEmail);
        return BillingResponse.newBuilder()
                .setAccountId("")
                .setStatus("PENDING")
                .build();
    }
}

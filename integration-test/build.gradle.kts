plugins {
    id("java")
}

group = "com.db"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.rest-assured:rest-assured:5.5.5")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    // gRPC dependencies for testing billing service
    testImplementation("io.grpc:grpc-netty-shaded:1.58.0")
    testImplementation("io.grpc:grpc-protobuf:1.58.0")
    testImplementation("io.grpc:grpc-stub:1.58.0")
    
    // Kafka dependencies for testing event-based services
    testImplementation("org.springframework.kafka:spring-kafka:3.0.11")
    testImplementation("org.springframework.kafka:spring-kafka-test:3.0.11")
}

tasks.test {
    useJUnitPlatform()
}
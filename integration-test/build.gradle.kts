plugins {
    id("java")
    id("com.google.protobuf") version "0.9.5"
}

group = "com.db"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.69.0"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
            }
        }
    }
}

sourceSets {
    main {
        proto {
            srcDir("../billing-service/src/main/proto")
            srcDir("../patient-service/src/main/proto")
            srcDir("../analytics-service/src/main/proto")
        }
    }
}

dependencies {
    // REST API testing
    testImplementation("io.rest-assured:rest-assured:5.5.5")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    // gRPC testing
    implementation("io.grpc:grpc-netty-shaded:1.69.0")
    implementation("io.grpc:grpc-protobuf:1.69.0")
    implementation("io.grpc:grpc-stub:1.69.0")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")
    implementation("com.google.protobuf:protobuf-java:4.29.1")
    
    // Kafka testing
    testImplementation("org.springframework.kafka:spring-kafka:3.1.0")
    testImplementation("org.springframework.kafka:spring-kafka-test:3.1.0")
    
    // Assertions
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks.test {
    useJUnitPlatform()
}
plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.5"
}

group = "com.db"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
//    plugins {
//        create("grpc") {
//            artifact = "io.grpc:protoc-gen-grpc-java:1.69.0"
//        }
//    }
//    generateProtoTasks {
//        all().forEach {
//            it.plugins {
//                create("grpc")
//            }
//        }
//    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("**/*.proto")
}

sourceSets {
    main {
        proto {
            srcDir("src/main/proto")
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.google.protobuf:protobuf-java:4.29.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

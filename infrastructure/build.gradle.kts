plugins {
    id("java")
    id("application")
}

group = "com.db"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("software.amazon.awscdk:aws-cdk-lib:2.204.0")
    implementation("com.amazonaws:aws-java-sdk:2.31.77")
}

tasks.test {
    useJUnitPlatform()
}
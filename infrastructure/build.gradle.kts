plugins {
    id("java")
    id("application")
}

group = "com.db"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java{
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("software.amazon.awscdk:aws-cdk-lib:2.178.1")
    implementation("com.amazonaws:aws-java-sdk:1.12.780")
}

tasks.test {
    useJUnitPlatform()
}
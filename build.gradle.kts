plugins {
    kotlin("jvm") version "1.9.23"
}

group = "com.tomtruyen"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.api-client:google-api-client:2.6.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20240705-2.0.0")

    implementation("net.fellbaum:jemoji:1.4.1")

    implementation("com.mailjet:mailjet-client:5.2.5")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

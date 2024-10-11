plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jlleitschuh.gradle.ktlint")
    application
}

dependencies {
    implementation(project(":common"))
    implementation(project(":event-publisher"))
    implementation(project(":projection-aggregator"))

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.json)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.cucumber.java8)
    testImplementation(libs.cucumber.junit)
    testImplementation(libs.cucumber.picocontainer)
    testImplementation(libs.junit.platform.suite)
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        useJUnitPlatform()
    }
}

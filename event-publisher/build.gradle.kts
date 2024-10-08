plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.gradleup.shadow")
    id("org.jlleitschuh.gradle.ktlint")
    application
}

dependencies {
    implementation(project(":common"))
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.defaultHeaders)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.json)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger)
    implementation(libs.logback)
    implementation(libs.logback.logstash)
    implementation(libs.kotlinx.coroutines.slf4j)
    implementation(libs.mongo.coroutine.driver)
    implementation(libs.mongo.bson.kotlinx)

    testImplementation(testFixtures(project(":common")))
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.java.snapshot.testing)
    testImplementation(libs.java.snapshot.testing.plugin.jackson)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("publisher.AppKt")
}

sourceSets {
    main {
        resources {
            srcDirs(layout.buildDirectory.dir("buildInfo"))
        }
    }
}

tasks {
    run.configure {
        environment.set("DEVMODE", "true")
    }

    val generateBuildInfo by registering {
        val outputDir =
            layout.buildDirectory
                .dir("buildInfo")
                .get()
                .asFile
        outputs.dir(outputDir)

        doFirst {
            outputDir.mkdirs()

            outputDir.resolve("buildInfo.properties").writeText(
                """baseCommit=${System.getenv("GITHUB_SHA") ?: "local"}
                  |baseBranch=${System.getenv("GITHUB_REF") ?: "local"}
                  |baseBuild=${System.getenv("GITHUB_RUN_NUMBER") ?: "0"}
                  |
                """.trimMargin(),
            )
        }
    }

    processResources {
        dependsOn(generateBuildInfo)
    }

    test {
        systemProperty("pact.verifier.publishResults", "true")
        systemProperty("pact.provider.branch", System.getenv("GITHUB_REF")?.removePrefix("refs/heads/") ?: "master")
        systemProperty("pactbroker.providerBranch", System.getenv("GITHUB_REF")?.removePrefix("refs/heads/") ?: "master")
        systemProperty("pact.provider.version", System.getenv("VERSION") ?: "")
        useJUnitPlatform()
    }
}

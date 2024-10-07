plugins {
    kotlin("jvm") version "2.0.20" apply false
    kotlin("plugin.serialization") version "2.0.20" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    id("com.gradleup.shadow") version "8.3.2" apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.24" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
}

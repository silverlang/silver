plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.6.21"
}

group = "couch.silver.api"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.arrow-kt:arrow-core:1.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.3.0")
    implementation(kotlin("script-runtime"))
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")
    implementation("com.squareup.okio:okio:3.2.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
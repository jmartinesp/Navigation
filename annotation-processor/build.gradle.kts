plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(Kotlin.std)
    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("com.google.devtools.ksp:symbol-processing-api:${KSP.version}")
}
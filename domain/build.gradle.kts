import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// :domain is plain Kotlin (no Android). It holds models, repository interfaces and
// pure math, so it can be unit-tested on the JVM without an emulator.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}

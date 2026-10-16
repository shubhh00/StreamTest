// :data is an Android library. It will hold Room, streams.json parsing, device info
// and the repository implementations (filled in from Milestone 2).
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.app.streamtest.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":domain"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}

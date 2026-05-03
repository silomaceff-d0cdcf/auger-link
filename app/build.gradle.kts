plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.chaquopy)
}

android {
    namespace = "io.silomaceff.augerlink"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.silomaceff.augerlink"
        minSdk = 29              // foreground-service-type matches Phase 1 design substrate
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-phase1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Phase 2: Chaquopy ships native CPython runtime as a per-ABI library.
        // Restrict to arm64-v8a — the only Android ABI the project supports
        // (matches the testbed's posture; rules out 32-bit ARM, x86, x86_64).
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }
    }
}

// Phase 2: embed CPython 3.11 in the APK. No pip deps yet — this commit just
// wires up the runtime so the build still produces a runnable APK with the
// native chaquopy library shipped per arm64-v8a. Code that exercises this
// arrives in subsequent microcommits.
chaquopy {
    defaultConfig {
        version = "3.11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

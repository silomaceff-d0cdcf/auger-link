plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.chaquopy)
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.silomaceff.augerlink"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.silomaceff.augerlink"
        minSdk = 29              // foreground-service-type matches Phase 1 design substrate
        targetSdk = 36
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
    buildFeatures {
        compose = true
    }
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }
    }

    // Vosk's Recognizer mmap-reads the model files on disk and requires them
    // to remain uncompressed within the APK. Without this list AGP gzips
    // the model assets and Vosk fails to load with "Failed to read model".
    androidResources {
        noCompress.addAll(
            listOf("conf", "dubm", "fst", "ie", "int", "mat", "mdl", "stats")
        )
    }
}

// AGP 9's built-in Kotlin support reads the JVM target from this block,
// not from a `kotlinOptions { jvmTarget = "17" }` inside `android { }` (removed in AGP 9).
kotlin {
    jvmToolchain(17)
}

// Phase 2: embed CPython 3.11 in the APK + ship Reticulum and LXMF as
// pip-installed Python packages. Versions match the testbed where these
// libs were first validated (rns 1.1.4 + lxmf 0.9.4).
chaquopy {
    defaultConfig {
        version = "3.11"
        pip {
            install("rns==1.1.4")
            install("lxmf==0.9.4")
        }
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
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // Phase 6a: offline speech-to-text via Vosk (Kaldi). Pulls Kaldi +
    // OpenFST native libs for arm64-v8a (~5 MB additional APK weight).
    // The actual STT model is shipped uncompressed in assets/model-en-us.
    implementation(libs.vosk.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

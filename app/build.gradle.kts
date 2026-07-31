plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)

    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.pass.hype"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.pass.hype"
        minSdk = 24
        targetSdk = 37
        versionCode = 3
        versionName = "3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        lint {
            checkReleaseBuilds = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.material.icons.extended)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.foundation)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Splash Screen
    implementation(libs.androidx.core.splashscreen)

    //Compose Destination
    implementation(libs.compose.destinations.core)
    ksp(libs.github.ksp)

    // Room
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.runtime.livedata)
    ksp(libs.androidx.lifecycle.compiler)
    implementation(libs.androidx.paging.paging.runtime)
    implementation(libs.android.database.sqlcipher)
    implementation(libs.androidx.sqlite)

    //BottomNavigation
    implementation(libs.ui)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.destinations.core)
    implementation(libs.androidx.material)

    //timeAgo
    implementation(libs.timeago)

    //Biometric Authentication
    implementation(libs.androidx.biometric)
    implementation(libs.roomdatabasebackup)
    implementation(libs.androidx.security.crypto)

    // WorkManager for background updates
    implementation (libs.androidx.work.runtime.ktx)

    //Json Serialization
    implementation(libs.kotlinx.serialization.json)

    //Gson Serialization
    implementation("com.google.code.gson:gson:2.13.2")

    // Google ML Kit – Document Scanner (clean card photo + perspective correction)
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")

    // Google ML Kit – Text Recognition (OCR for card number / expiry / name)
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // Coil for card photo display
    implementation("io.coil-kt:coil-compose:2.7.0")
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

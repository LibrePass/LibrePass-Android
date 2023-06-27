plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    kotlin("android")
}

android {
    namespace = "dev.medzik.librepass.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.medzik.librepass.android"
        minSdk = 24
        targetSdk = 34
        versionCode = System.getenv("LIBREPASS_APP_VERSION_CODE")?.toInt() ?: 1
        versionName = System.getenv("LIBREPASS_APP_VERSION_NAME") ?: "0.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/*"
        }
    }
}

dependencies {
    // androidx
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.appcompat)
    implementation(libs.annotation)
    implementation(libs.androidx.material.icons.extended)

    // google accompanist
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.drawablepainter)

    // kotlin
    implementation(libs.kotlinx.coroutines.android)

    // librepass client
    implementation(libs.librepass.client)

    // room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // navigation
    implementation(libs.androidx.navigation.compose)

    // biometric
    implementation(libs.androidx.biometric.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}

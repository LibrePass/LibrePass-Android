plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    kotlin("android")
}

android {
    namespace = "dev.medzik.librepass.android"
    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    defaultConfig {
        applicationId = "dev.medzik.librepass.android"
        minSdk = libs.versions.android.sdk.min.get().toInt()
        targetSdk = libs.versions.android.sdk.target.get().toInt()
        versionCode = 6
        versionName = "1.0.0-alpha06"

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
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/*"
        }
    }
}

dependencies {
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.google.accompanist.drawablepainter)
    implementation(libs.google.accompanist.swiperefresh)
    implementation(libs.google.accompanist.systemuicontroller)

    // for biometric authentication
    implementation(libs.androidx.biometric)

    // used for calling `onResume` and locking vault after X minutes
    implementation(libs.androidx.lifecycle.runtime.compose)

    // for storing preferences
    implementation(libs.androidx.datastore.preferences)

    // room database
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // for cipher icons in vault
    implementation(libs.coil.compose)

    // API client
    implementation(libs.librepass.client)

    // for restarting the application, for example, after changing the theme in settings
    implementation(libs.process.phoenix)

    // kotlin coroutines
    implementation(libs.kotlinx.coroutines.android)

    // local modules
    implementation(project(":components"))
    implementation(project(":crypto"))
    implementation(project(":utils"))

    // for splash screen with material3 and dynamic color
    implementation(libs.google.material)

    // for testing
    debugImplementation(libs.androidx.ui.test.manifest)

    // for preview support
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
}

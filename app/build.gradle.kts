plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "dev.medzik.librepass.android"
    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    defaultConfig {
        applicationId = "dev.medzik.librepass.android"
        minSdk = libs.versions.android.sdk.min.get().toInt()
        targetSdk = libs.versions.android.sdk.target.get().toInt()
        versionCode = 17
        versionName = "1.3.0"

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

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }

    lint {
        warning.add("MissingTranslation")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/*"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.compose.ui)

    implementation(libs.accompanist.drawablepainter)

    implementation(libs.androidx.biometric.ktx)

    // used for calling `onResume` and locking vault after X minutes
    implementation(libs.compose.lifecycle.runtime)

    implementation(projects.databaseLogic)

    // dagger
    implementation(libs.dagger.hilt)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.coil.compose)

    implementation(libs.librepass.client)
    implementation(libs.otp)

    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)

    implementation(projects.common)
    implementation(projects.businessLogic)

    // for splash screen with material3 and dynamic color
    implementation(libs.google.material)

    implementation(libs.zxing.android) { isTransitive = false }
    implementation(libs.zxing)

    implementation(libs.medzik.android.compose)
    implementation(libs.medzik.android.crypto)
    implementation(libs.medzik.android.utils)

    // for testing
    debugImplementation(libs.compose.ui.test.manifest)

    // for preview support
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
}

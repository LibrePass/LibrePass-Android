plugins {
    alias(libs.plugins.android.library)
//    alias(libs.plugins.android.screenshot)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.medzik.librepass.android.ui"
    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.sdk.min.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

dependencies {
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.coil)
    implementation(libs.compose.lifecycle.runtime)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.compose.navigation)
    implementation(libs.compose.ui)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.medzik.android.compose)
    implementation(libs.medzik.android.crypto)
    implementation(libs.medzik.android.utils)
    implementation(libs.medzik.common.kotlin)

    implementation(libs.librepass.client)

    implementation(libs.dagger.hilt)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.dagger.hilt.compiler)

    implementation(projects.common)
    implementation(projects.databaseLogic)

    // for testing
    debugImplementation(libs.compose.ui.test.manifest)

    // for preview support
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)

//    screenshotTestImplementation(libs.compose.ui.tooling)
}

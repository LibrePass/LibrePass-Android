plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "dev.medzik.librepass.android.database"
    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.sdk.min.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    implementation(libs.dagger.hilt)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.librepass.client)
}

room {
    schemaDirectory("$projectDir/schemas")
}

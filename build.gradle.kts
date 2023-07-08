// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.com.google.devtools.ksp) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
}

allprojects {
    // fix task (current target is 1.8) and 'kspDebugKotlin' task (current target is 17) jvm target compatibility should be set to the same Java version.
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

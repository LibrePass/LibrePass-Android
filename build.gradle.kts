// https://youtrack.jetbrains.com/issue/KTIJ-19369/False-positive-cant-be-called-in-this-context-by-implicit-receiver-with-plugins-in-Gradle-version-catalogs-as-a-TOML-file#focus=Comments-27-6204464.0-0
@Suppress("DSL_SCOPE_VIOLATION")

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

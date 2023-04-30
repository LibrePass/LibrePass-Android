// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("8.2.0-alpha01").apply(false)
    id("com.android.library").version("8.2.0-alpha01").apply(false)
    id("com.google.devtools.ksp").version("1.8.20-1.0.11").apply(false)
    kotlin("android").version("1.8.0").apply(false)
}

// fix task (current target is 1.8) and 'kspDebugKotlin' task (current target is 17) jvm target compatibility should be set to the same Java version.
allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
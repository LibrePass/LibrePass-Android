pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LibrePass"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":common")
include(":ui-logic")
include(":database-logic")

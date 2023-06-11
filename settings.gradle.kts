pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "medzik"
            url = uri("https://maven.medzik.dev/snapshots")
        }
    }
}

rootProject.name = "LibrePass"
include(":app")

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

class Projects(dh: DependencyHandler) {
    val common = dh.project(":common")
    val database = dh.project(":database-logic")

    val material3PullRefresh = dh.project(":m3-pullrefresh")
}

val DependencyHandler.projects: Projects
    get() = Projects(this)

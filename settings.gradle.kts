pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("D:/CatatCatat-tools/maven"); content { includeModule("org.xerial", "sqlite-jdbc"); includeModule("com.android.tools.external.com-intellij", "intellij-core") } }
        google(); mavenCentral()
    }
}
rootProject.name = "CatatCatat"
include(":app")

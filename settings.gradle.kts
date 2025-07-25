pluginManagement {

    repositories {

        google {

            content {

                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {

        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "Jetpack UI"
include(":jetpack-ui")
include(":app")

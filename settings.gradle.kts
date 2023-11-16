rootProject.name = "BuildSystem"

include("buildsystem-core")
include("buildsystem-api")

include("buildsystem-abstraction:adapter-1_12")
include("buildsystem-abstraction:adapter-1_13")
include("buildsystem-abstraction:adapter-1_14")
include("buildsystem-abstraction:adapter-1_17")
include("buildsystem-abstraction:adapter-1_20")
include("buildsystem-abstraction:common")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
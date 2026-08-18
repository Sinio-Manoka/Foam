
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()

        maven {
            url = uri(
                "https://packages.jetbrains.team/maven/p/ij/intellij-dependencies"
            )
        }

        maven {
            url = uri("https://css4j.github.io/maven/")

            content {
                includeGroup("io.sf.carte")
                includeGroup("io.sf.jclf")
                includeGroup("xmlpull")
                includeGroup("xpp3")
            }
        }
    }
}

plugins {

    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":app")
include(":utils")

rootProject.name = "foam"

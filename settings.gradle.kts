import java.util.Properties

pluginManagement {
    repositories {
        maven("https://api.xposed.info/")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/releases/") }

        val properties = Properties()
        val localProperties = File(rootDir, "local.properties")
        if (localProperties.exists()) {
            properties.load(localProperties.inputStream())
        }

        maven {
            url = uri("https://maven.pkg.github.com/compose-miuix-ui/miuix")
            credentials {
                username = properties.getProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
                password = properties.getProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "HyperLyric"
include(":app")

includeBuild("D:/AndroidProjects/miuix-main") {
    dependencySubstitution {
        substitute(module("top.yukonga.miuix.kmp:miuix-ui-android"))
            .using(project(":miuix-ui"))
        substitute(module("top.yukonga.miuix.kmp:miuix-blur-android"))
            .using(project(":miuix-blur"))
        substitute(module("top.yukonga.miuix.kmp:miuix-icons-android"))
            .using(project(":miuix-icons"))
        substitute(module("top.yukonga.miuix.kmp:miuix-preference-android"))
            .using(project(":miuix-preference"))
        substitute(module("top.yukonga.miuix.kmp:miuix-navigation3-ui-android"))
            .using(project(":miuix-navigation3-ui"))
    }
}
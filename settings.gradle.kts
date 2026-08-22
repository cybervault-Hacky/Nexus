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
    }
}

// CI runners often expose ANDROID_HOME / ANDROID_SDK_ROOT without a
// local.properties file. AGP 8.2 still prefers sdk.dir.
val androidSdk = System.getenv("ANDROID_HOME")
    ?: System.getenv("ANDROID_SDK_ROOT")
if (!androidSdk.isNullOrBlank()) {
    val localProperties = file("local.properties")
    if (!localProperties.exists()) {
        localProperties.writeText("sdk.dir=${androidSdk.replace("\\", "\\\\")}\n")
    }

    // build-tools 34.0.0 on some CI images is missing dx / lib/dx.jar,
    // which makes AGP fail with "Installed Build Tools revision 34.0.0 is corrupted".
    val buildTools = file("$androidSdk/build-tools/34.0.0")
    if (buildTools.isDirectory) {
        val d8 = file("${buildTools.path}/d8")
        val dx = file("${buildTools.path}/dx")
        if (d8.exists() && !dx.exists()) {
            dx.writeText("#!/bin/bash\nexec \"$(dirname \"\$0\")/d8\" \"\$@\"\n")
            dx.setExecutable(true)
        }
        val d8Jar = file("${buildTools.path}/lib/d8.jar")
        val dxJar = file("${buildTools.path}/lib/dx.jar")
        if (d8Jar.exists() && !dxJar.exists()) {
            java.nio.file.Files.createSymbolicLink(dxJar.toPath(), d8Jar.toPath().fileName)
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Nexus"
include(":app")

// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // Define versions of plugins used across the project.
    // `apply false` means the plugin is not applied to the root project,
    // but makes it available for subprojects to apply.
    id("com.android.application") version "8.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Apply and configure ktlint consistently for all subprojects.
// The actual configuration is stored in ktlint.gradle.kts.
subprojects {
    apply(from = rootProject.file("ktlint.gradle.kts"))
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

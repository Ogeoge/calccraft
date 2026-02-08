// Central configuration for the ktlint Gradle plugin.
// This file is applied from other build scripts.

repositories {
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

// Shared ktlint configuration
ktlint {
    // Use a specific, consistent version of the ktlint engine.
    version.set("1.2.1")

    // Enable verbose output for easier debugging.
    verbose.set(true)

    // Print output to the console.
    outputToConsole.set(true)

    // Use colored output for better readability.
    coloredOutput.set(true)

    // Configure reporters for CI and local feedback.
    reporters {
        // Plain text reporter for console output.
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        // Checkstyle XML reporter for CI systems.
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }

    // Example of how to filter out files (not needed for this project).
    // filter {
    //     exclude("**/generated/**")
    //     include("**/kotlin/**")
    // }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        // Add the classpath for the Android Gradle Plugin
        classpath("com.android.tools.build:gradle:8.1.2") // Match the plugin version you're using
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")  // Kotlin plugin version
    }
}

plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}



// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin apply false
    id("com.google.devtools.ksp") version "2.3.10" apply false
    id("androidx.room") version libs.versions.roomVersion apply false
}
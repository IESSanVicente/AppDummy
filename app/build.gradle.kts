plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")   // procesador de anotaciones de Room
    id("androidx.room")             // plugin de Room para gestión de esquemas
}

android {
    namespace = "es.javiercarrasco.appdummy"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "es.javiercarrasco.appdummy"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Material Icons Extended
    implementation(libs.androidx.compose.material.icons.extended)

    // Coil para la carga de imágenes
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // ViewModel y Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Corrutinas
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.kotlinx.coroutines.test)

    // Navigation Compose (API tipada disponible desde 2.8.0)
    implementation(libs.androidx.navigation.compose)

    // Kotlin Serialization — imprescindible para las rutas @Serializable
    implementation(libs.kotlinx.serialization.json)

    // Room — todos los artefactos deben tener la misma versión
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)       // extensiones de corrutinas
    ksp(libs.androidx.room.compiler)             // generador de código (KSP, NO kapt)

    // Testing de Room
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.room.testing)

    // Gson — deserialización de JSON a objetos Kotlin/Java
    implementation("com.google.code.gson:gson:2.14.0")
}
# AppDummy

Práctica guiada de desarrollo de aplicaciones Android con Kotlin y Jetpack Compose.

## Corrección de versiones en libs.versions.toml

Para evitar errores de compilación, se recomienda reviasar las versiones de las dependencias en el archivo `libs.versions.toml`.

Para hacer compatible el proyecto con la versión **compileSdk 36** (`build.gradle.kts(:app)`), debe cambiarse las versiones para las librerías `androidx.core:core-ktx` y `androidx.lifecycle:lifecycle-runtime-compose-android` a versiones compatibles, `coreKtx = "1.18.0"` y `lifecycleRuntimeKtx = "2.10.0"`.

La sección `[versions]` del archivo `libs.versions.toml` debe quedar de la siguiente manera:

```toml
[versions]
agp = "9.2.1"
coreKtx = "1.18.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.10.0"
activityCompose = "1.13.0"
kotlin = "2.4.0"
composeBom = "2026.06.01"
```
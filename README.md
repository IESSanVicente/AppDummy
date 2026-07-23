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

## Versiones

- **v0.** Proyecto inicial con la estructura de carpetas y archivos base.
- **v1.** Pantalla de bienvenida y dependencia de Material Icons Extended añadida.
- **v2.** Pantalla de listado de libros con filtros y funciones de marcado como leído y favorito añadido.
  - Se añade la dependencia Coil (`io.coil-kt.coil3:coil-compose:3.5.0`) para la carga de imágenes.
  - También se añade la dependencia Coil Network OkHttp (`io.coil-kt.coil3:coil-network-okhttp:3.5.0`) para la carga de imágenes desde la red.
  - Se añade el permiso de acceso a Internet en el archivo `AndroidManifest.xml`.
  - Se añade imagen `nocover.jpg` para libros sin portada en la carpeta `res/drawable`.
  - Se mueve la pantalla de bienvenida a la carpeta `screens`.
- **v3.** Pantalla de gestión de permisos.
  - Se añade la pantalla de gestión de permisos en la carpeta `screens`.
  - Se añade el permiso de acceso a la cámara y características hardware en el archivo `AndroidManifest.xml`.
- **v4.** MVVM y ViewModel
  - Se añaden las dependencias necesarias para realizar el bloque 2 (ViewModel, Lifecycle, Corrutinas y Testing).
  - Se añade la clase `LibrosViewModel` para gestionar el estado de la pantalla de listado de libros.
  - Se crea la clase `Libro` para representar los datos de cada libro, sustituyendo la versión anterior, `LibroUI`.
  - Se crea la clase `LibrosUiState` para representar los posibles estados de la pantalla de listado de libros.
  - Se crea la clase `RepositorioLibros` para simular la obtención de datos de libros desde un repositorio.
  - Se modifica la pantalla de listado de libros para que utilice el `ViewModel` y el estado de la pantalla se gestione a través de `LibrosUiState`.
  - Se añade un evento de un solo disparo para mostrar un Snackbar al marcar favoritos y libros leídos.
  - Se añade la clase `LibrosViewModelTest` para realizar pruebas unitarias del `ViewModel`.
  - Modificación de la clase `MainActivity` para lanzar la pantalla listado, pero se mantiene el método _preview_ para lanzar directamente la pantalla listado.
- **v5.** Navegación y pantalla de detalle de libro.
  - Se añaden las dependencias necesarias para realizar el bloque 3 (Navegación).
  - Se añade la navegación entre pantallas y la pantalla de detalle de libro.
  
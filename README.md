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
  - Se crea navegación entre pantallas utilizando `NavigationBar` utilizando `NavHost` y `NavController`.
- **v6.** Presistencia con ROOM
  - Se añaden las dependencias necesarias para realizar el tema 4 (ROOM).
  - Modificación de la clase `Libro` para que sea una entidad de ROOM.
  - Se crea la interfaz `LibrosDao` para definir las operaciones de acceso a la base de datos.
  - Se crea la clase `AppDatabase` para definir la base de datos ROOM y su configuración.
  - Se añade la clase `Converters` para definir los conversores de tipos de datos personalizados.
  - Se añade la persistencia de datos utilizando ROOM para almacenar los libros favoritos y leídos.
  - Se modifica la clase `RepositorioLibros` para que utilice ROOM en lugar de datos simulados.
  - Se añade la clase `AppDummyApplication` para inicializar la base de datos y el repositorio.
  - Se modifica la clase `LibrosViewModel` para que utilice el repositorio de ROOM.
- **v7.** Retrofit2, consumo API Rest
  - Se añaden las dependencias necesarias para realizar el tema 5 (Retrofit2).
  - Se añaden los DTOs `LibroDTO` y `BusquedaResponseDto` para mapear la respuesta de la API REST.
  - Se crea el objeto `OpenLibrary` para gestionar las URLs de las carátulas y el resto de constantes de la API.
  - Se crea el mappeador `LibroMapper` para convertir los DTOs en entidades de dominio.
  - Se añade un test unitario para el mappeador `LibroMapperTest`.
  - Se crea la interfaz `OpenLibraryApiService` para definir los endpoints de la API REST.
  - Se crea el objeto `RetrofitClient` para configurar Retrofit2 y OkHttpClient.
  - Se crea la clase `RemoteDataSource` para gestionar la obtención de datos desde la API REST.
  - Se modifica la clase `RepositorioLibros` para que utilice el `RemoteDataSource` y la base de datos ROOM para obtener los libros.
  - Se configura `Coil` para que utilice `OkHttpClient` con Retrofit2 en la carga de imágenes.
  - Se añade el composable `CaratulaLibro` para mostrar la carátula del libro en la pantalla de detalle.
  - Nueva ruta de navegación para la pantalla que permite añadir libros.
  - Se crea en `utils` el objeto `Isbn` para validar el ISBN de los libros.
  - Se añade la pantalla de añadir libro con validación de ISBN y control de errores.
- **v8.** Arquitectura offline-first
  - Revisión de las dependencias y actualización de versiones en `libs.versions.toml`.
  - Se añade el permiso `ACCESS_NETWORK_STATE` en el archivo `AndroidManifest.xml`.
  - Se incrementa la versión de la base de datos ROOM a la versión 2 y se añade el campo `actualizadoEn` en la entidad `Libro`.
  - Se modifica el DAO `LibrosDao` para añadir nuevos métodos y gestión del cambpo `actualizadoEn`.
  - Actualización del `LocalDataSource` para gestionar el campo `actualizadoEn` y la nueva versión de la base de datos.
  - Se añade la clase `ResultadoSincronizacion` para representar los posibles resultados de la sincronización de datos.
  - Se actualiza `LibrosRepositorio` para gestionar la sincronización de datos entre la base de datos local y la API REST.
  - Modificación de la clase `LibrosViewModel` para añadir la función de sincronización de datos y el estado de sincronización.
  - Se modifica la pantalla de listado de libros para mostrar el estado de sincronización y permitir al usuario lanzar la sincronización manualmente.
  - Se añade a `utils` la clase `ObservadorConectividad` para observar los cambios en la conectividad de red.
  - Se añade en `AppContainer` la observación de la conectividad de red y se lanza la sincronización de datos cuando se detecta que hay conexión a Internet.
- **v9.** Multimedia y cámara
  - Se añaden las dependencias necesarias de CameraX y Media3, y los permisos necesarios en el archivo `AndroidManifest.xml`.
  - Se añade el composable `SolicitudPermiso` para solicitar permisos de cámara y almacenamiento.
  - Se añade el objeto `AlmacenPortadas` para gestionar el almacenamiento de las portadas de los libros en la memoria interna del dispositivo.
  - Se añade un nuevo atributo a la entidad `Libro` para almacenar la ruta de la portada del libro en la memoria interna.
  - Se añade el método `actualizarPortadaLocal` en el DAO `LibrosDao` para actualizar la ruta de la portada del libro en la base de datos y se actualizan el Datasource local y el repositorio para gestionar la portada local.
  - Se amplia `DetalleViewModel` para gestionar la captura de fotos y el almacenamiento de las portadas en la memoria interna.
  - Modificación del composable `CaratulaLibro` para mostrar la portada local si existe, y en caso contrario, mostrar la portada remota o la imagen por defecto.
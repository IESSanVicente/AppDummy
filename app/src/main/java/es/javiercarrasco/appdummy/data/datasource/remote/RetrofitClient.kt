package es.javiercarrasco.appdummy.data.datasource.remote

import es.javiercarrasco.appdummy.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// ─── data/datasource/remote/RetrofitClient.kt ────────────────────────────────────────────────────
// (En AppDummy esta lógica acabará viviendo en el AppContainer — ver más abajo)
object RetrofitClient {

    // Interceptor: añade la cabecera User-Agent a TODAS las peticiones.
    // Open Library triplica el límite de peticiones a las apps identificadas.
    private val userAgentInterceptor = Interceptor { chain ->
        val peticion = chain.request().newBuilder()
            .header("User-Agent", OpenLibrary.USER_AGENT)
            .build()
        chain.proceed(peticion)
    }

    // HttpLoggingInterceptor imprime en Logcat las peticiones y respuestas HTTP
    // Level.BODY: muestra URL, cabeceras y cuerpo completo (SOLO en debug)
    // Level.NONE: no registra nada (obligatorio en release)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)    // tiempo máximo para conectar
        .readTimeout(30, TimeUnit.SECONDS)       // tiempo máximo para leer la respuesta
        .writeTimeout(30, TimeUnit.SECONDS)      // tiempo máximo para enviar datos
        .addInterceptor(userAgentInterceptor)    // el orden importa: primero cabeceras...
        .addInterceptor(loggingInterceptor)      // ...y después los logs, para verlas en Logcat
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(OpenLibrary.BASE_URL)
        .client(okHttpClient)
        // Gson por defecto: los DTOs no necesitan ninguna configuración especial
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Se crea la implementación de la interfaz de la API
    val openLibraryApiService: OpenLibraryApiService =
        retrofit.create(OpenLibraryApiService::class.java)
}
package es.javiercarrasco.appdummy.data.di

import android.content.Context
import es.javiercarrasco.appdummy.data.datasource.local.AppDatabase
import es.javiercarrasco.appdummy.data.datasource.local.LocalDataSource
import es.javiercarrasco.appdummy.data.datasource.remote.RemoteDataSource
import es.javiercarrasco.appdummy.data.datasource.remote.RetrofitClient
import es.javiercarrasco.appdummy.data.repository.LibrosRepository
import es.javiercarrasco.appdummy.utils.ObservadorConectividad
import okhttp3.OkHttpClient

// ─── data/di/AppContainer.kt ─────────────────────────────────────────────────────────────────────
interface AppContainer {
    val librosRepository: LibrosRepository
    val okHttpClient: OkHttpClient   // se expone para compartirlo con Coil
    val observadorConectividad: ObservadorConectividad   // ← NUEVO en T6
}

class DefaultAppContainer(context: Context) : AppContainer {

    // ─── Capa local (T4) ────────────────────────────────────────────────────────
    // Room — singleton de la base de datos
    // by lazy: se crea una sola vez la primera vez que se accede
    private val database by lazy { AppDatabase.getDatabase(context) }
    private val localDataSource: LocalDataSource by lazy {
        LocalDataSource(database.libroDao())
    }

    // ─── Capa remota (T5) ───────────────────────────────────────────────────────
    override val okHttpClient: OkHttpClient by lazy { RetrofitClient.okHttpClient }

    private val remoteDataSource: RemoteDataSource by lazy {
        RemoteDataSource(RetrofitClient.openLibraryApiService)
    }

    // El repositorio pasa a recibir DOS orígenes de datos
    override val librosRepository: LibrosRepository by lazy {
        LibrosRepository(localDataSource, remoteDataSource)
    }

    // ─── Servicios del sistema (T6) ─────────────────────────────────────────
    // Se usa applicationContext: el observador vive tanto como la aplicación, así
    // que guardar el contexto de una Activity provocaría una fuga de memoria.
    override val observadorConectividad: ObservadorConectividad by lazy {
        ObservadorConectividad(context.applicationContext)
    }
}
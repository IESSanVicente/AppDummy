package es.javiercarrasco.appdummy.data.di

import android.content.Context
import androidx.room.Room
import es.javiercarrasco.appdummy.data.datasource.local.AppDatabase
import es.javiercarrasco.appdummy.data.datasource.local.LocalDataSource
import es.javiercarrasco.appdummy.data.repository.LibrosRepository
import kotlin.getValue

// ─── data/di/AppContainer.kt ─────────────────────────────────────────────────────────────────────
interface AppContainer {
    val librosRepository: LibrosRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    // Room — singleton de la base de datos
    // by lazy: se crea una sola vez la primera vez que se accede
    private val database by lazy {
        AppDatabase.getDatabase(context)
    }

    private val localDataSource: LocalDataSource by lazy {
        LocalDataSource(database.libroDao())
    }

    override val librosRepository: LibrosRepository by lazy {
        LibrosRepository(localDataSource)
    }
}
package es.javiercarrasco.appdummy

import android.app.Application
import es.javiercarrasco.appdummy.data.di.AppContainer
import es.javiercarrasco.appdummy.data.di.DefaultAppContainer

// ─── AppDummyApplication.kt ──────────────────────────────────────────────────────────────────────
class AppDummyApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
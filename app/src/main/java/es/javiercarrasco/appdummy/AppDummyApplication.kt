package es.javiercarrasco.appdummy

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import es.javiercarrasco.appdummy.data.di.AppContainer
import es.javiercarrasco.appdummy.data.di.DefaultAppContainer

// ─── AppDummyApplication.kt — sin cambios respecto a T5 ──────────────────────────────────────────
class AppDummyApplication : Application(), SingletonImageLoader.Factory {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                // Mismo OkHttpClient que Retrofit: un solo pool de conexiones, los
                // mismos timeouts y la misma cabecera User-Agent (T5)
                add(OkHttpNetworkFetcherFactory(callFactory = { container.okHttpClient }))
            }
            // Coil crea por defecto una caché de memoria (un porcentaje de la RAM
            // disponible) y otra de disco. No hace falta configurarlas a mano.
            .crossfade(true)
            .build()
}
package es.javiercarrasco.appdummy

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import es.javiercarrasco.appdummy.data.di.AppContainer
import es.javiercarrasco.appdummy.data.di.DefaultAppContainer

// ─── AppDummyApplication.kt ──────────────────────────────────────────────────────────────────────
class AppDummyApplication : Application(), SingletonImageLoader.Factory {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }

    // SingletonImageLoader.Factory: Coil llama a este método para crear
    // el ImageLoader singleton que usará AsyncImage en toda la app
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                // OkHttpNetworkFetcherFactory: usa el mismo OkHttpClient que Retrofit,
                // de modo que ambos comparten pool de conexiones, timeouts y User-Agent
                add(OkHttpNetworkFetcherFactory(callFactory = { container.okHttpClient }))
            }
            .crossfade(true)   // animación de fundido al cargar las imágenes
            .build()
}
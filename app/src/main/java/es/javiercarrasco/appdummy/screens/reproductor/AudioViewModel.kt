package es.javiercarrasco.appdummy.screens.reproductor

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// ─── screens/reproductor/AudioViewModel.kt ───────────────────────────────────────────────────────
class AudioViewModel(application: Application) : AndroidViewModel(application) {

    // null mientras no hay nada preparado o después de liberar el reproductor.
    private var reproductor: MediaPlayer? = null

    // Corrutina que actualiza la barra de progreso; se cancela al parar.
    private var trabajoProgreso: Job? = null

    private val _reproduciendo = MutableStateFlow(false)
    val reproduciendo: StateFlow<Boolean> = _reproduciendo.asStateFlow()

    private val _posicionMs = MutableStateFlow(0)
    val posicionMs: StateFlow<Int> = _posicionMs.asStateFlow()

    private val _duracionMs = MutableStateFlow(0)
    val duracionMs: StateFlow<Int> = _duracionMs.asStateFlow()

    /** Reproduce un recurso empaquetado en res/raw. */
    fun reproducirRecurso(@RawRes recurso: Int) {
        liberar()
        // MediaPlayer.create() hace por dentro setDataSource() + prepare():
        // devuelve el reproductor ya en estado Prepared. Solo es válido para
        // recursos locales, porque prepare() bloquea el hilo llamante.
        reproductor = MediaPlayer.create(getApplication(), recurso)?.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    // USAGE_MEDIA indica al sistema que es contenido que el
                    // usuario ha pedido escuchar: se enruta al canal de
                    // multimedia y respeta su volumen.
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnCompletionListener {
                _reproduciendo.value = false
                _posicionMs.value = 0
                trabajoProgreso?.cancel()
            }
            _duracionMs.value = duration
            start()
        }
        _reproduciendo.value = reproductor != null
        observarProgreso()
    }

    /** Reproduce audio remoto. Nunca con prepare(): bloquearía la interfaz. */
    fun reproducirUrl(url: String) {
        liberar()
        reproductor = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener { mp ->
                _duracionMs.value = mp.duration
                mp.start()
                _reproduciendo.value = true
                observarProgreso()
            }
            setOnErrorListener { _, que, extra ->
                Log.e("MediaPlayer", "Error what=$que extra=$extra")
                _reproduciendo.value = false
                true    // true = el error queda gestionado; no se llama a OnCompletion
            }
            prepareAsync()
        }
    }

    fun alternarReproduccion() {
        val mp = reproductor ?: return
        if (mp.isPlaying) {
            mp.pause()
            _reproduciendo.value = false
        } else {
            mp.start()
            _reproduciendo.value = true
            observarProgreso()
        }
    }

    /** Salta a una posición concreta. Se llama al soltar el Slider, no al arrastrarlo. */
    fun buscar(posicionMs: Int) {
        reproductor?.seekTo(posicionMs)
        _posicionMs.value = posicionMs
    }

    private fun observarProgreso() {
        trabajoProgreso?.cancel()
        trabajoProgreso = viewModelScope.launch {
            while (reproductor?.isPlaying == true) {
                _posicionMs.value = reproductor?.currentPosition ?: 0
                delay(250.milliseconds)
            }
        }
    }

    private fun liberar() {
        trabajoProgreso?.cancel()
        reproductor?.apply {
            if (isPlaying) stop()
            release()          // libera el decodificador; el objeto queda inservible
        }
        reproductor = null
        _reproduciendo.value = false
        _posicionMs.value = 0
        _duracionMs.value = 0
    }

    /**
     * onCleared() se ejecuta cuando el ViewModel se destruye definitivamente,
     * no en cada rotación de pantalla. Es el único lugar correcto para liberar
     * el reproductor: sin esta llamada el audio seguiría sonando y el
     * decodificador quedaría ocupado (fuga de recursos).
     */
    override fun onCleared() {
        super.onCleared()
        liberar()
    }
}
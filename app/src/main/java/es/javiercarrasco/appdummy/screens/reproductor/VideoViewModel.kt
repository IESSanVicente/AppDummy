package es.javiercarrasco.appdummy.screens.reproductor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

// ─── screens/reproductor/VideoViewModel.kt ───────────────────────────────────────────────────────
class VideoViewModel(application: Application) : AndroidViewModel(application) {

    // ExoPlayer necesita un Context para acceder a los códecs del sistema.
    val player: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        repeatMode = Player.REPEAT_MODE_OFF   // constante de Player, no de ExoPlayer
        playWhenReady = false                 // no reproducir hasta que el usuario lo pida
    }

    /** Carga un contenido. prepare() inicia la descarga del búfer. */
    fun cargar(url: String) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
    }

    override fun onCleared() {
        super.onCleared()
        // Igual que MediaPlayer: sin release() se retiene el decodificador.
        player.release()
    }
}
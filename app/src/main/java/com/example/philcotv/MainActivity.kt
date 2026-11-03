
package com.example.philcotv

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {

    private val playlistUrl =
        "https://iptv-org.github.io/iptv/countries/br.m3u"

    private var player: ExoPlayer? = null
    private lateinit var root: LinearLayout
    private lateinit var list: ListView
    private lateinit var playerView: PlayerView
    private lateinit var status: TextView

    private val channels = mutableListOf<Channel>()
    private var selected = 0

    data class Channel(val name: String, val url: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        buildUi()
        loadPlaylist()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFF101419.toInt())
            setPadding(24, 20, 24, 20)
        }

        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 18, 0)
        }

        val title = TextView(this).apply {
            text = "PHILCO TV  •  AO VIVO"
            textSize = 20f
            setTextColor(0xFF65D6AD.toInt())
            setPadding(8, 8, 8, 18)
        }

        status = TextView(this).apply {
            text = "Carregando canais brasileiros…"
            textSize = 14f
            setTextColor(0xFFB8C2CC.toInt())
            setPadding(8, 0, 8, 12)
        }

        list = ListView(this).apply {
            dividerHeight = 1
            setBackgroundColor(0xFF171D23.toInt())
            setSelector(android.R.color.darker_gray)
            isFocusable = true

            onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    play(position)
                }
        }

        left.addView(title)
        left.addView(status)
        left.addView(
            list,
            LinearLayout.LayoutParams(330, 0, 1f)
        )

        playerView = PlayerView(this).apply {
            useController = true
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            setBackgroundColor(0xFF000000.toInt())
        }

        root.addView(
            left,
            LinearLayout.LayoutParams(
                350,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            playerView,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        setContentView(root)
    }

    private fun loadPlaylist() {
        Thread {
            var connection: HttpURLConnection? = null

            try {
                connection = URL(playlistUrl).openConnection()
                    as HttpURLConnection

                connection.connectTimeout = 15000
                connection.readTimeout = 20000
                connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 PhilcoTV"
                )

                val parsed = mutableListOf<Channel>()
                var name = "Canal"

                connection.inputStream.bufferedReader().use { reader ->
                    reader.forEachLine { raw ->
                        val line = raw.trim()

                        when {
                            line.startsWith("#EXTINF", true) -> {
                                name = line.substringAfterLast(",")
                                    .trim()
                                    .ifBlank { "Canal" }
                            }

                            line.startsWith("http://") ||
                            line.startsWith("https://") -> {
                                parsed.add(Channel(name, line))
                                name = "Canal"
                            }
                        }
                    }
                }

                runOnUiThread {
                    channels.clear()
                    channels.addAll(parsed)

                    if (channels.isEmpty()) {
                        status.text = "Nenhum canal encontrado."
                    } else {
                        status.text =
                            "${channels.size} canais brasileiros carregados"

                        list.adapter = ArrayAdapter(
                            this,
                            android.R.layout.simple_list_item_1,
                            channels.map { it.name }
                        )

                        list.requestFocus()
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    status.text =
                        "Erro ao carregar canais: ${e.message ?: "verifique a internet"}"
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    private fun play(index: Int) {
        if (index !in channels.indices) return

        selected = index

        if (player == null) {
            player = ExoPlayer.Builder(this).build().also {
                playerView.player = it

                it.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        status.text =
                            "Não foi possível reproduzir: ${channels.getOrNull(selected)?.name}"
                    }
                })
            }
        }

        player?.apply {
            setMediaItem(MediaItem.fromUri(channels[index].url))
            prepare()
            playWhenReady = true
        }

        status.text =
            "${index + 1}/${channels.size} • ${channels[index].name}"
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP) {
            play((selected + 1).coerceAtMost(channels.lastIndex))
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN) {
            play((selected - 1).coerceAtLeast(0))
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        if (::playerView.isInitialized) {
            playerView.player = null
        }

        player?.release()
        player = null

        super.onDestroy()
    }
}

package com.app.streamtest.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import com.app.streamtest.domain.catalog.StreamType
import com.app.streamtest.domain.catalog.StreamVersion

/** Builds the ExoPlayer and the MediaItem for a stream. */
object PlayerFactory {

    // ExoPlayer is Media3's player. It downloads the playlist/manifest, then the
    // video and audio chunks, decodes them and keeps a buffer ahead of the playhead.
    fun createPlayer(context: Context): ExoPlayer = ExoPlayer.Builder(context).build()

    // A MediaItem describes *what* to play: the URL, its format and, for protected
    // streams, how to get the decryption key.
    fun createMediaItem(stream: StreamVersion): MediaItem {
        val builder = MediaItem.Builder()
            .setUri(stream.url)
            // Media3 normally guesses the format from the URL ending (.m3u8 / .mpd).
            // Some of our URLs don't end that way (e.g. ".ism/.m3u8"), so we say it
            // explicitly and Media3 picks the right loader (HLS or DASH) for sure.
            .setMimeType(mimeTypeFor(stream.type))

        if (stream.type == StreamType.DASH_DRM) {
            // The video is encrypted with Widevine. Before it can play, the player sends a
            // request to the license server, which answers with the decryption key.
            builder.setDrmConfiguration(
                MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setLicenseUri(stream.licenseUrl)
                    .build(),
            )
        }
        return builder.build()
    }

    private fun mimeTypeFor(type: StreamType): String = when (type) {
        StreamType.HLS -> MimeTypes.APPLICATION_M3U8
        StreamType.DASH, StreamType.DASH_DRM -> MimeTypes.APPLICATION_MPD
    }
}

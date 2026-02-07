package com.example.localconnect.service;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import com.google.common.util.concurrent.ListenableFuture;
import com.example.localconnect.R;

public class MediaPlaybackService extends MediaSessionService {
    private ExoPlayer player;
    private MediaSession mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new ExoPlayer.Builder(this).build();
        
        // Handle audio focus and interruptions
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build();
        player.setAudioAttributes(audioAttributes, true);

        // Build media session
        mediaSession = new MediaSession.Builder(this, player).build();
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if ("PLAY_NOTICE_AUDIO".equals(intent.getAction())) {
                String audioUrl = intent.getStringExtra("EXTRA_AUDIO_URL");
                String title = intent.getStringExtra("EXTRA_NOTICE_TITLE");
                if (audioUrl != null) {
                    playAudio(audioUrl, title);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void playAudio(String url, String title) {
        MediaMetadata metadata = new MediaMetadata.Builder()
                .setTitle(title != null ? title : "Notice Audio")
                .build();

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(url)
                .setMediaMetadata(metadata)
                .build();

        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }
}

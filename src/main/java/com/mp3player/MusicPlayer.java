package com.mp3player;

import java.io.File;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class MusicPlayer {
    private MediaPlayer player;
    private boolean repetindo = false;

    public interface OnTrackEndCallback {
        void onEnd();
    }

    public interface OnReadyCallback {
        void onReady(Duration duration);
    }

    public interface OnTimeUpdateCallback {
        void onUpdate(Duration currentTime);
    }

    public void tocar( File arquivo, double volume, OnReadyCallback onReady, OnTimeUpdateCallback onTimeUpdate, OnTrackEndCallback onEnd) {
        parar();

        Media media = new Media(arquivo.toURI().toString());
        player = new MediaPlayer(media);
        player.setVolume(volume);

        player.setOnReady(() -> {
            if (onReady != null) {
                onReady.onReady(player.getTotalDuration());
            }
        });

        player.currentTimeProperty().addListener((obs, antigo, novo) -> {
            if (onTimeUpdate != null) {
                onTimeUpdate.onUpdate(novo);
            }
        });

        player.setOnEndOfMedia(() -> {
            if (repetindo) {
                player.seek(Duration.ZERO);
                player.play();
            } else if (onEnd != null) {
                onEnd.onEnd();
            }
        });

        player.play();
    }

    public void alternarPlayPause() {
        if (player != null) {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
            } else {
                player.play();
            }
        }
    }

    public void parar() {
        if (player != null) {
            player.stop();
            player.dispose();
            player = null;
        }
    }

    public void setVolume(double volume) {
        if (player != null) {
            player.setVolume(volume);
        }
    }

    public void buscarSegundos(double segundos) {
        if (player != null) {
            player.seek(Duration.seconds(segundos));
        }
    }

    public void buscarPorPorcentagem(double porcentagem) {
        if (player != null && player.getTotalDuration() != null) {
            double novaPosicao = porcentagem * player.getTotalDuration().toSeconds();
            player.seek(Duration.seconds(novaPosicao));
        }
    }

    public boolean isPlaying() {
        return player != null && player.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public boolean isRepetindo() {
        return repetindo;
    }

    public boolean alternarLoop() {
        this.repetindo = !this.repetindo;
        return this.repetindo;
    }

    public static String formatarTempo(Duration tempo) {
        if (tempo == null) return "00:00";
        int minutos = (int) tempo.toMinutes();
        int segundos = (int) tempo.toSeconds() % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }
}
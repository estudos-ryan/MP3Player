package com.mp3player;

import javafx.scene.layout.BorderPane;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.control.ScrollPane;
import java.util.ArrayList;
import javafx.util.Duration;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class App extends Application {
    private MediaPlayer player;

    VBox playlistBox = new VBox(5);
    private ArrayList<File> playlist = new ArrayList<>();
    private int musicaAtual = 0;
    private boolean repetindo = false;

    @Override
    public void start(Stage stage) {
        Label tempoAtual = new Label("00:00");
        Label duracao = new Label("00:00");
        Button anterior = new Button("⏮");
        Button proxima = new Button("⏭");
        Button loop = new Button("🔁");
        Slider volume = new Slider(0, 1, 0.5);
        VBox.setMargin(volume, new Insets(0, 20, 10, 20));
        Slider tempo = new Slider();
        Button abrir = new Button("Abrir");
        Button playPause = new Button("▶");
        Button parar = new Button("Parar");

        ScrollPane scroll = new ScrollPane();

        scroll.setContent(playlistBox);

        scroll.setFitToWidth(true);

        scroll.setPrefHeight(250);

        HBox controles = new HBox(10);
        controles.setAlignment(Pos.CENTER);
        controles.getChildren().addAll(anterior, playPause, parar, proxima, loop);

        HBox barraTempo = new HBox(8);
        barraTempo.setAlignment(Pos.CENTER);

        tempoAtual.setMinWidth(40);
        duracao.setMinWidth(40);

        barraTempo.getChildren().addAll(tempoAtual, tempo, duracao);

        loop.setOnAction(e -> {
            repetindo = !repetindo;

            if (repetindo) {
                loop.setText("🔂");
            } else {
                loop.setText("🔁");
            }
        });

        volume.valueProperty().addListener((obs, antigo, novoValor) -> {
            if (player != null) {
                player.setVolume(novoValor.doubleValue());
            }
        });

        tempo.valueProperty().addListener((obs, antigo, novo) -> {
            if (tempo.isValueChanging() && player != null) {
                player.seek(javafx.util.Duration.seconds(novo.doubleValue()));
            }
        });

        tempo.setOnMouseClicked(e -> {
            if (player != null) {
                double porcentagem = e.getX() / tempo.getWidth();

                double novaPosicao = porcentagem * player.getTotalDuration().toSeconds();

                player.seek(Duration.seconds(novaPosicao));
            }
        });

        abrir.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3", "*.mp3"));

            File arquivo = chooser.showOpenDialog(stage);

            if (arquivo != null) {
                playlist.add(arquivo);

                adicionarMusicaNaPlaylist(arquivo);

                if (playlist.size() == 1) {
                    musicaAtual = 0;

                    tocarMusica(arquivo, volume, tempo, tempoAtual, duracao, playPause);
                }
            }
        });

        proxima.setOnAction(e -> {
            if (!playlist.isEmpty()) {
                musicaAtual++;

                if (musicaAtual >= playlist.size()) {
                    musicaAtual = 0;
                }

                tocarMusica(playlist.get(musicaAtual), volume, tempo, tempoAtual, duracao, playPause);
            }
        });

        anterior.setOnAction(e -> {
            if (!playlist.isEmpty()) {
                musicaAtual--;

                if (musicaAtual < 0) {
                    musicaAtual = playlist.size() - 1;
                }

                tocarMusica(playlist.get(musicaAtual), volume, tempo, tempoAtual, duracao, playPause);
            }
        });

        playPause.setOnAction(e -> {
            if (player != null) {
                if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                    player.pause();
                    playPause.setText("▶");
                } else {
                    player.play();
                    playPause.setText("⏸");
                }
            }
        });

        parar.setOnAction(e -> {
            if (player != null) {
                player.stop();
            }
        });

        BorderPane layout = new BorderPane();

        layout.setPadding(new Insets(10));

        HBox topo = new HBox();
        topo.setAlignment(Pos.TOP_LEFT);
        topo.getChildren().add(abrir);

        layout.setTop(topo);

        VBox centro = new VBox(10);
        centro.setAlignment(Pos.CENTER);

        centro.getChildren().addAll(scroll, barraTempo, controles, volume);
        layout.setCenter(centro);
        Scene cena = new Scene(layout, 350, 480);

        stage.setTitle("MP3 Player");
        stage.setScene(cena);
        stage.show();
    }

    private void tocarMusica(File arquivo, Slider volume, Slider tempo, Label tempoAtual, Label duracao,
            Button playPause) {
        if (player != null) {
            player.stop();
            player.dispose();
        }

        Media media = new Media(arquivo.toURI().toString());

        player = new MediaPlayer(media);

        player.setOnPlaying(() -> {
            playPause.setText("⏸");
        });

        player.setOnPaused(() -> {
            playPause.setText("▶");
        });

        player.setOnStopped(() -> {
            playPause.setText("▶");
        });

        player.setOnEndOfMedia(() -> {
            if (repetindo) {
                player.seek(Duration.ZERO);
                player.play();
            } else {
                musicaAtual++;

                if (musicaAtual >= playlist.size()) {
                    musicaAtual = 0;
                }

                tocarMusica(playlist.get(musicaAtual), volume, tempo, tempoAtual, duracao, playPause);
            }
        });

        player.setVolume(volume.getValue());

        player.setOnReady(() -> {
            tempo.setMax(player.getTotalDuration().toSeconds());

            duracao.setText(formatarTempo(player.getTotalDuration()));
        });

        player.currentTimeProperty().addListener((obs, antigo, novo) -> {
            if (!tempo.isValueChanging()) {
                tempo.setValue(novo.toSeconds());
            }

            tempoAtual.setText(formatarTempo(novo));
        });

        playPause.setText("⏸");

        player.play();
    }

    private void adicionarMusicaNaPlaylist(File arquivo) {
        Label nome = new Label(String.format("%02d - %s", playlist.size(), arquivo.getName()));

        Button remover = new Button("❌");

        HBox linha = new HBox(15);

        linha.setPrefWidth(180);

        nome.setPrefWidth(200);

        linha.getChildren().addAll(nome, remover);

        remover.setOnAction(e -> {
            int indice = playlist.indexOf(arquivo);

            if (indice >= 0) {
                if (indice == musicaAtual && player != null) {
                    player.stop();
                }

                playlist.remove(indice);

                playlistBox.getChildren().remove(linha);

                if (musicaAtual >= playlist.size()) {
                    musicaAtual = 0;
                }
            }
        });

        playlistBox.getChildren().add(linha);
    }

    private String formatarTempo(Duration tempo) {
        int minutos = (int) tempo.toMinutes();
        int segundos = (int) tempo.toSeconds() % 60;

        return String.format("%02d:%02d", minutos, segundos);
    }

    public static void main(String[] args) {
        launch();
    }
}
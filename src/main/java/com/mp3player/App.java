package com.mp3player;

import java.io.File;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class App extends Application {
    private final MusicPlayer player = new MusicPlayer();
    private final Playlist playlist = new Playlist();

    private final VBox playlistBox = new VBox(5);
    private Button playPause;
    private Slider tempo;
    private Label tempoAtual;
    private Label duracao;
    private Slider volume;

    @Override
    public void start(Stage stage) {
        tempoAtual = new Label("00:00");
        duracao = new Label("00:00");
        tempoAtual.setMinWidth(40);
        duracao.setMinWidth(40);

        Button anterior = new Button("⏮");
        Button proxima = new Button("⏭");
        Button loop = new Button("🔁");
        playPause = new Button("▶");
        Button parar = new Button("Parar");

        Button btnArquivo = new Button("Arquivo");
        btnArquivo.setStyle("-fx-background-color: transparent; -fx-padding: 5 10 5 10; -fx-cursor: hand;");

        btnArquivo.setOnMouseEntered(e -> btnArquivo.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 5 10 5 10; -fx-cursor: hand;"));
        btnArquivo.setOnMouseExited(e -> btnArquivo.setStyle("-fx-background-color: transparent; -fx-padding: 5 10 5 10; -fx-cursor: hand;"));

        btnArquivo.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3", "*.mp3"));
            File arquivo = chooser.showOpenDialog(stage);

            if (arquivo != null) {
                playlist.adicionar(arquivo);
                adicionarMusicaNaPlaylistUI(arquivo);

                if (playlist.size() == 1) {
                    carregarETocarAtual();
                }
            }
        });

        HBox barraSuperior = new HBox(btnArquivo);
        barraSuperior.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #dcdcdc; -fx-border-width: 0 0 1 0;");

        volume = new Slider(0, 1, 0.5);
        VBox.setMargin(volume, new Insets(0, 20, 10, 20));
        tempo = new Slider();

        ScrollPane scroll = new ScrollPane(playlistBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(250);

        HBox controles = new HBox(10, anterior, playPause, parar, proxima, loop);
        controles.setAlignment(Pos.CENTER);

        HBox barraTempo = new HBox(8, tempoAtual, tempo, duracao);
        barraTempo.setAlignment(Pos.CENTER);

        loop.setOnAction(e -> {
            boolean loopAtivo = player.alternarLoop();
            loop.setText(loopAtivo ? "🔂" : "🔁");
        });

        volume.valueProperty().addListener((obs, antigo, novoValor) -> {
            player.setVolume(novoValor.doubleValue());
        });

        tempo.valueProperty().addListener((obs, antigo, novo) -> {
            if (tempo.isValueChanging()) {
                player.buscarSegundos(novo.doubleValue());
            }
        });

        tempo.setOnMouseClicked(e -> {
            double porcentagem = e.getX() / tempo.getWidth();
            player.buscarPorPorcentagem(porcentagem);
        });

        proxima.setOnAction(e -> {
            if (!playlist.isEmpty()) {
                playlist.proxima();
                carregarETocarAtual();
            }
        });

        anterior.setOnAction(e -> {
            if (!playlist.isEmpty()) {
                playlist.anterior();
                carregarETocarAtual();
            }
        });

        playPause.setOnAction(e -> {
            player.alternarPlayPause();
            playPause.setText(player.isPlaying() ? "⏸" : "▶");
        });

        parar.setOnAction(e -> {
            player.parar();
            playPause.setText("▶");
        });

        BorderPane layout = new BorderPane();

        layout.setTop(barraSuperior);

        VBox centro = new VBox(10, scroll, barraTempo, controles, volume);
        centro.setAlignment(Pos.CENTER);
        layout.setCenter(centro);

        Scene cena = new Scene(layout, 350, 400);
        stage.setTitle("MP3 Player");
        stage.setScene(cena);
        stage.show();
    }

    private void carregarETocarAtual() {
        File arquivo = playlist.getMusicaAtual();
        if (arquivo == null) return;

        player.tocar(
            arquivo,
            volume.getValue(),
            totalDuration -> {
                tempo.setMax(totalDuration.toSeconds());
                duracao.setText(MusicPlayer.formatarTempo(totalDuration));
            },
            currentTime -> {
                if (!tempo.isValueChanging()) {
                    tempo.setValue(currentTime.toSeconds());
                }
                tempoAtual.setText(MusicPlayer.formatarTempo(currentTime));
            },
            () -> {
                playlist.proxima();
                carregarETocarAtual();
            }
        );

        playPause.setText("⏸");
    }

    private void adicionarMusicaNaPlaylistUI(File arquivo) {
        Label nome = new Label(String.format("%02d - %s", playlist.size(), arquivo.getName()));
        Button remover = new Button("❌");

        javafx.scene.layout.Region espacador = new javafx.scene.layout.Region();
        HBox.setHgrow(espacador, javafx.scene.layout.Priority.ALWAYS);

        HBox linha = new HBox(10, nome, espacador, remover);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setPadding(new Insets(0, 10, 0, 5));

        remover.setOnAction(e -> {
            int indice = playlist.indexOf(arquivo);
            
            if (indice >= 0) {
                if (indice == playlist.getMusicaAtualIndex()) {
                    player.parar();
                playPause.setText("▶");
            }

            playlist.remover(indice);
            playlistBox.getChildren().remove(linha);
        }
    });
    
    playlistBox.getChildren().add(linha);
}
}
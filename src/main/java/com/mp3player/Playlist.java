package com.mp3player;

import java.io.File;
import java.util.ArrayList;

public class Playlist {
    private final ArrayList<File> arquivos = new ArrayList<>();
    private int musicaAtual = 0;

    public void adicionar(File arquivo) {
        arquivos.add(arquivo);
    }

    public void remover(int indice) {
        if (indice >= 0 && indice < arquivos.size()) {
            arquivos.remove(indice);
            if (musicaAtual >= arquivos.size()) {
                musicaAtual = 0;
            }
        }
    }

    public File getMusicaAtual() {
        if (arquivos.isEmpty()) {
            return null;
        }
        return arquivos.get(musicaAtual);
    }

    public File proxima() {
        if (arquivos.isEmpty()) return null;
        musicaAtual++;
        if (musicaAtual >= arquivos.size()) {
            musicaAtual = 0;
        }
        return arquivos.get(musicaAtual);
    }

    public File anterior() {
        if (arquivos.isEmpty()) return null;
        musicaAtual--;
        if (musicaAtual < 0) {
            musicaAtual = arquivos.size() - 1;
        }
        return arquivos.get(musicaAtual);
    }

    public boolean isEmpty() {
        return arquivos.isEmpty();
    }

    public int size() {
        return arquivos.size();
    }

    public int getMusicaAtualIndex() {
        return musicaAtual;
    }

    public void setMusicaAtualIndex(int index) {
        this.musicaAtual = index;
    }

    public int indexOf(File arquivo) {
        return arquivos.indexOf(arquivo);
    }
}
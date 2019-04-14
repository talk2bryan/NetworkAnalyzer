package com.maiwodi.networkanalyzer.persistence;

public interface AsyncResponse {
    default void processDownloadSpeed(double output) {
    }
}


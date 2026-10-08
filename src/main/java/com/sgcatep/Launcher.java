package com.sgcatep;

/**
 * Esta clase sirve como puente para iniciar la aplicación de manera limpia.
 * Evita el error de componentes faltantes de JavaFX al no extender directamente de Application
 * cuando queremos ejecutar el Main.
 */

public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
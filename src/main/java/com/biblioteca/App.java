package com.biblioteca;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.biblioteca.util.DatabaseConnection;

import java.io.IOException;


public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Verificar conexión a base de datos al inicio
        if (!DatabaseConnection.getInstance().testConnection()) {
            System.err.println("ERROR: No se pudo conectar a la base de datos");
            System.err.println("Verifica el archivo database.properties");
            System.exit(1);
        }

        System.out.println("Conexión a base de datos exitosa");

        // Cargar la pantalla de login
        loadScene("login", "Sistema de Biblioteca - Login", 600, 400);
    }


    public static void loadScene(String fxmlName, String title, int width, int height) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                App.class.getResource("/view/" + fxmlName + ".fxml"));

        if (fxmlLoader.getLocation() == null) {
            throw new IOException("No se encontró el archivo: /view/" + fxmlName + ".fxml");
        }

        Scene scene = new Scene(fxmlLoader.load(), width, height);

        // Cargar CSS si existe
        try {
            scene.getStylesheets().add(
                    App.class.getResource("/styles/application.css").toExternalForm());
        } catch (NullPointerException e) {
            System.out.println("No se encontró archivo CSS");
        }

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
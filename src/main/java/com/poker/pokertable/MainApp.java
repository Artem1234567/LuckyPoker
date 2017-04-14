package com.poker.pokertable;

import com.poker.pokertable.controllers.TableController;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    
    public static final String MAIN_FXML = "/fxml/Scene.fxml";
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Lucky Poker");
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_FXML));
        
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        ((TableController) loader.getController()).setPrimaryStage(primaryStage);
        
        primaryStage.setHeight(440);
        primaryStage.setWidth(740);
        
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> Platform.exit());
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

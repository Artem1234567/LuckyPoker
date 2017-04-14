package com.poker.pokertable.controllers;

import static com.poker.pokertable.constants.Constants.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.poker.pokertable.constants.ErrorConstants;

public class TableController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TableController.class.getName());

    private Stage primaryStage;
    
    private static final double STEP_X = 52;
    private double fromX = -154;
    
    private static final int TABLE_CARD_COUNT = 5;
    
    private Optional<List<Path>> cardPaths = null;
    
    @FXML
    private void startButtonAction(ActionEvent event) {
        cardPaths = loadCardsPaths();
        drawTable();
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    
    private Optional<List<Path>> loadCardsPaths() {
        List<Path> filePaths = null;
        try {
            filePaths = Files.list(Paths.get(CARDS_DIR)).collect(Collectors.toList());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            return Optional.ofNullable(filePaths);
        }
    }

    private void drawTable() {
        Optional<Image> tableImage = loadPokerTableImage();
        if (!tableImage.isPresent()) {
            LOGGER.severe(ErrorConstants.NOT_EXISTS_TABLE_IMAGE);
        } else {
            dealtTableCards(tableImage.get());
        }
    }
    
    private Optional<Image> loadPokerTableImage() {
        return loadImage(Paths.get(POKER_TABLE_PATH));
    }
    
    private Optional<Image> loadImage(Path path) {
        Image im = null;
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            im = new Image(fis);
        } catch (FileNotFoundException | NullPointerException ex) {
            LOGGER.log(Level.SEVERE, ErrorConstants.FILE_NOT_FOUND, path);
        } finally {
            return Optional.ofNullable(im);
        }
    }
    
    private ImageView createNewDummyCard() {
        Optional<Image> dummyCard = loadImage(Paths.get(DUMMY_CARD_PATH));
        ImageView dummyView = new ImageView(dummyCard.get());
        dummyView.setFitWidth(CARD_WIDTH);
        dummyView.setFitHeight(CARD_HEIGHT);
        dummyView.setTranslateY(DUMMY_START_Y);
        return dummyView;
    }
    
    private void dealtTableCards(Image tableImage) {
        Canvas canvas = new Canvas(740, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.drawImage(tableImage, 0, 20);
        gc.restore();
        
        StackPane stack = new StackPane();
        stack.setMaxSize(canvas.getWidth(), canvas.getHeight());
        stack.getChildren().add(canvas);
        
        // Add deck to table.
        ImageView deckView = createNewDummyCard();
        stack.getChildren().add(deckView);
        
        primaryStage.setScene(new Scene(stack, Color.BLACK));
        primaryStage.show();
        
        for (int i = 0; i < TABLE_CARD_COUNT; i++) {
            Double valueX = nextX(fromX, STEP_X);
            // get Bolvanka.
            ImageView dummyView = createNewDummyCard();
            stack.getChildren().add(dummyView);
            
            Optional<Path> card = getRandomCard();
            if (!card.isPresent()) {
                LOGGER.severe(ErrorConstants.CARD_NOT_FOUND);
                return;
            }
            
            Optional<Image> imageCard = loadImage(card.get());
            ImageView imageView = new ImageView(imageCard.get());
            imageView.setFitWidth(CARD_WIDTH);
            imageView.setFitHeight(CARD_HEIGHT);
            
            imageView.setTranslateX(valueX);
            imageView.setTranslateY(CARD_TO_Y_POSITION);
            stack.getChildren().add(imageView);
            
            RotateTransition rt2Default = createCardRotatorDefault(imageView);
            rt2Default.play();
            
            TranslateTransition tt1 = new TranslateTransition(Duration.millis(2000), dummyView);
            tt1.setCycleCount(1);
            tt1.setFromY(CARD_FROM_Y_POSITION);
            tt1.setToY(CARD_TO_Y_POSITION);
            tt1.setFromX(0);
            tt1.setToX(valueX);
            
            tt1.onFinishedProperty().set((EventHandler<ActionEvent>) (ActionEvent event) -> {
                RotateTransition rt01 = createDummyRotator(dummyView);
                rt01.onFinishedProperty().set((EventHandler<ActionEvent>) (ActionEvent ev) -> {
                    imageView.setTranslateX(valueX);
                    RotateTransition rt02 = createCardRotator(imageView);
                    rt02.play();
                });
                rt01.play();
            });
            tt1.play();
            
            ScaleTransition st1 = new ScaleTransition(Duration.millis(2000), dummyView);
            st1.setFromX(0);
            st1.setToX(1);
            st1.setFromY(0);
            st1.setToY(1);
            st1.setCycleCount(1);
            st1.play();
        }
    }
    
    private double nextX(double currentX, double stepX) {
        fromX = currentX + stepX;
        return fromX;
    }
    
    private void removeCard(Path card) {
        if (cardPaths.isPresent()) {
            cardPaths.get().remove(card);
        }
    }
    
    private Optional<Path> getRandomCard() {
        if (cardPaths.isPresent()) {
            int index = ThreadLocalRandom.current().nextInt(cardPaths.get().size());
            Path cardPath = cardPaths.get().get(index);
            removeCard(cardPath);
            return Optional.ofNullable(cardPath);
        }
        return Optional.empty();
    }
    
    private RotateTransition createDummyRotator(Node card) {
        RotateTransition rotator = new RotateTransition(Duration.millis(1000), card);
        rotator.setAxis(Rotate.Y_AXIS);
        rotator.setFromAngle(0);
        rotator.setToAngle(90);
        rotator.setInterpolator(Interpolator.LINEAR);
        rotator.setCycleCount(1);

        return rotator;
    }
    
    private RotateTransition createCardRotatorDefault(Node card) {
        RotateTransition rotator = new RotateTransition(Duration.millis(2000), card);
        rotator.setAxis(Rotate.Y_AXIS);
        rotator.setFromAngle(-90);
        rotator.setToAngle(-90);
        rotator.setInterpolator(Interpolator.LINEAR);
        rotator.setCycleCount(1);

        return rotator;
    }
    
    private RotateTransition createCardRotator(Node card) {
        RotateTransition rotator = new RotateTransition(Duration.millis(1000), card);
        rotator.setAxis(Rotate.Y_AXIS);
        rotator.setFromAngle(-90);
        rotator.setToAngle(0);
        rotator.setInterpolator(Interpolator.LINEAR);
        rotator.setCycleCount(1);

        return rotator;
    }
}

package tech.octopusdragon.cursordodge.application;

import java.io.IOException;

import tech.octopusdragon.cursordodge.application.scenes.GameOverSceneController;
import tech.octopusdragon.cursordodge.game.classes.GameOverReason;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class CursorDodgeApplication extends Application {
	
	// --- Constants ---
	// Window dimensions (proportion of smaller)
	public static final double DEFAULT_WINDOW_SIZE_PROPORTION = 0.5;
	// FXML file paths
	public static final String GAME_SCENE_FXML =
			"scenes/GameScene.fxml";
	public static final String TITLE_SCENE_FXML =
			"scenes/TitleScene.fxml";
	public static final String GAME_OVER_SCENE_FXML =
			"scenes/GameOverScene.fxml";
	public static final String HOW_TO_PLAY_SCENE_FXML =
			"scenes/HowToPlayScene.fxml";
	// Other file paths
	private static final String ICON_PATH = "resources/icon.png";
	
	// --- Variables ---
	// GUI components
	private static Stage stage;

	
	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;
		primaryStage.setTitle("Cursor Dodge");
		primaryStage.getIcons().add(new Image(
				getClass().getClassLoader().getResourceAsStream(ICON_PATH)));
		
		// Set default window size as a percentage of smaller screen dimension
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();	double screenW = screenBounds.getWidth(),
				screenH = screenBounds.getHeight(),
				windowSize = (screenH < screenW ? screenH : screenW) *
				DEFAULT_WINDOW_SIZE_PROPORTION;
		primaryStage.setWidth(windowSize);
		primaryStage.setHeight(windowSize);
		
		switchToTitleScene();
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	/**
	 * Sets the stage's current scene to a new scene with a root loaded with an
	 * FXML file
	 * @param fxmlPath The path of the FXML file
	 * @return The FXMLLoader associated with the new scene
	 */
	private static FXMLLoader switchToScene(String fxmlPath) {
		FXMLLoader loader = new FXMLLoader(
					CursorDodgeApplication.class.getResource(fxmlPath));
		try {
			if (stage.getScene() == null) {
				stage.setScene(new Scene(loader.load()));
			}
			else {
				stage.getScene().setRoot(loader.load());
			}
		} catch (IOException e) {
			System.out.println("Error loading FXML file");
			e.printStackTrace();
		}
		return loader;
	}
	
	/**
	 * Switches to the game scene
	 */
	public static void switchToGameScene() {
		switchToScene(GAME_SCENE_FXML);
	}
	
	/**
	 * Switches to the title scene
	 */
	public static void switchToTitleScene() {
		switchToScene(TITLE_SCENE_FXML);
	}
	
	/**
	 * Switches to the how to play scene
	 */
	public static void switchToHowToPlayScene() {
		switchToScene(HOW_TO_PLAY_SCENE_FXML);
	}
	
	/**
	 * Switches to the game over scene
	 */
	public static void switchToGameOverScene(GameOverReason reason) {
		FXMLLoader loader = switchToScene(GAME_OVER_SCENE_FXML);
		GameOverSceneController controller = loader.getController();
		controller.gameOverMessage(reason);
	}
	
	/**
	 * Switches to the game over scene
	 */
	public static void switchToGameOverScene(GameOverReason reason, double time,
			double sceneWidth, double sceneHeight,
			double screenWidth, double screenHeight) {
		FXMLLoader loader = switchToScene(GAME_OVER_SCENE_FXML);
		GameOverSceneController controller = loader.getController();
		controller.gameOverMessage(reason, time,
				sceneWidth, sceneHeight, screenWidth, screenHeight);
	}

}

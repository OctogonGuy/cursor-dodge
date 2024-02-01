package tech.octopusdragon.cursordodge.application.scenes;

import tech.octopusdragon.cursordodge.game.Game;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class GameSceneController {
	
	@FXML private StackPane root;
	@FXML private Label messageLabel;

	@FXML
	public void initialize() {
		root.getChildren().add(0, new Game(messageLabel));
	}

}
package tech.octopusdragon.cursordodge.application.scenes;

import tech.octopusdragon.cursordodge.application.CursorDodgeApplication;
import tech.octopusdragon.cursordodge.game.classes.GameOverReason;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GameOverSceneController {
	
	@FXML private Label messageLabel;
	
	
	@FXML
	private void switchToGameScene(ActionEvent event) {
		CursorDodgeApplication.switchToGameScene();
	}
	
	
	@FXML
	private void switchToTitleScene(ActionEvent event) {
		CursorDodgeApplication.switchToTitleScene();
	}
	
	
	public void gameOverMessage(GameOverReason reason) {
		messageLabel.setText(gameOverReasonString(reason));
	}
	
	
	public void gameOverMessage(GameOverReason reason, double time,
			double sceneWidth, double sceneHeight,
			double screenWidth, double screenHeight) {
		StringBuilder message = new StringBuilder();

		message.append(gameOverReasonString(reason));
		
		message.append("\n");
		message.append(String.format(
				"You lasted %.2f seconds " +
						"on a %dx%d window (%.1f%% of your screen area).",
				time,
				(int)sceneWidth,
				(int)sceneHeight,
				((sceneWidth * sceneHeight) / (screenWidth * screenHeight)
						* 100)));
		
		messageLabel.setText(message.toString());
	}
	
	
	private String gameOverReasonString(GameOverReason reason) {
		String reasonString;
		
		switch (reason) {
		case OBSTACLE:
			reasonString = "You hit an obstacle.";
			break;
		case DID_NOT_CATCH:
			reasonString = "You did not grab the catch circle in time.";
			break;
		case OUT_OF_BOUNDS:
			reasonString = "You moved your cursor out of bounds.";
			break;
		case RESIZE:
			reasonString = "You resized the window.";
			break;
		default:
			reasonString = "unspecified game over message";
		}
		
		return reasonString;
	}

}

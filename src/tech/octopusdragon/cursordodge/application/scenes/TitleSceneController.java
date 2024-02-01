package tech.octopusdragon.cursordodge.application.scenes;

import tech.octopusdragon.cursordodge.application.CursorDodgeApplication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class TitleSceneController {

	@FXML
	private void switchToGameScene(ActionEvent event) {
		CursorDodgeApplication.switchToGameScene();
	}

	@FXML
	private void switchToHowToPlayScene(ActionEvent event) {
		CursorDodgeApplication.switchToHowToPlayScene();
	}

}

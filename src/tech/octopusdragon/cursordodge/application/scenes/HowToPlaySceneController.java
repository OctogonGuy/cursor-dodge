package tech.octopusdragon.cursordodge.application.scenes;

import tech.octopusdragon.cursordodge.application.CursorDodgeApplication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class HowToPlaySceneController {
	
	@FXML
	private void switchToTitleScene(ActionEvent event) {
		CursorDodgeApplication.switchToTitleScene();
	}

}

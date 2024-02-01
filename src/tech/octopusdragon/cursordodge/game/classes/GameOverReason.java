package tech.octopusdragon.cursordodge.game.classes;

/**
 * Possible reasons for game over
 * @author Alex Gill
 *
 */
public enum GameOverReason {
	OBSTACLE,		// The cursor hit an obstacle
	DID_NOT_CATCH,	// The player did not grab the catchable circle in time
	OUT_OF_BOUNDS,	// The player moved the cursor outside of the window
	RESIZE			// The player resized the window
}

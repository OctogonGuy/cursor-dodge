package tech.octopusdragon.cursordodge.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import tech.octopusdragon.cursordodge.application.CursorDodgeApplication;
import tech.octopusdragon.cursordodge.game.classes.CollisionPoint;
import tech.octopusdragon.cursordodge.game.classes.Corner;
import tech.octopusdragon.cursordodge.game.classes.Edge;
import tech.octopusdragon.cursordodge.game.classes.GameOverReason;
import tech.octopusdragon.cursordodge.game.classes.Position;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

public class Game extends Pane {
	
	// --- Constants ---
	// Size of circles as percent of scene size
	private static final double CIRCLE_SIZE = 0.001;
	// The area around the cursor as a percent of scene size inside which
	// circles will not appear
	private static final double MOUSE_BOX_SIZE = 0.5;
	// Circles will bounce in the direction of the new angle +/- a random number
	// in this range divided by two
	private static final double NEW_ANGLE_RANGE = 22.5;
	// The number from which to count down
	private static final int COUNTDOWN_FROM = 3;
	// Duration in seconds of each count
	private static final double COUNTDOWN_DURATION = 0.5;
	// Paint of the obstacle circles
	private static final Paint OBSTACLE_CIRCLE_PAINT = Color.RED;
	// Initial speed of obstacle circles in pixels per second
	private static final double OBSTACLE_CIRCLE_INITIAL_SPEED = 100.0;
	// Acceleration of obstacle circles in pixels per second squared
	private static final double OBSTACLE_CIRCLE_ACCELERATION = 2;
	// Interval in seconds after which a new obstacle circle will be added
	private static final double OBSTACLE_CIRCLE_INITIAL_INTERVAL = 7.5;
	// Amount in seconds that will be added to the obstacle circle interval
	// after every appearance
	private static final double OBSTACLE_CIRCLE_INTERVAL_ADDER = 5.0;
	// Paint of the circles the player must grab
	private static final Paint CATCH_CIRCLE_PAINT = Color.BLUE;
	// Delay after which circles the user must grab will start being added
	private static final double CATCH_CIRCLE_DELAY = 22.5;
	// Interval in seconds after which a new circle the player must grab will be
	// added
	private static final double CATCH_CIRCLE_INTERVAL = 15.0;
	// Duration for which circles the user must grab will fade and after which,
	// if the user does not catch, will end the game
	private static final double CATCH_CIRCLE_INITIAL_DURATION = 5.0;
	// Value catch circle duration will be multiplied by after every time one
	// appears
	private static final double CATCH_CIRCLE_DURATION_MULTIPLIER = 0.95;
	// Paint of corner obstacle circles
	private static final Paint CORNER_CIRCLE_PAINT = Color.GREEN;
	// Interval in seconds after which corner obstacle circles will pop up
	private static final double CORNER_CIRCLE_INTERVAL = 2.0;
	// Duration of the enter and exit animations of the corner obstacle circle
	private static final double CORNER_CIRCLE_ANIMATION_DURATION = 0.15;
	// Duration corner obstacles will stay on screen
	private static final double CORNER_CIRCLE_DURATION = 0.15;
	// The chance of a corner circle appearing in the same corner as the user
	private static final double CORNER_CIRCLE_SAME_CIRCLE_CHANCE = 0.5;
	
	// --- Variables ---
	private double screenWidth;		// Screen width
	private double screenHeight;	// Screen height
	private double sceneWidth;		// Scene width
	private double sceneHeight;		// Scene height
	private double circleRadius;	// The radius of the circle
	
	// --- GUI components ---
	private Label messageLabel;				// Message label
	private Ellipse firstObstacleCircle;	// First obstacle circle
	
	// --- Game information ---
	// Property for the position of the cursor
	private ObjectProperty<Position> cursorPosProperty;
	// Whether the mouse is outside of the scene
	private BooleanProperty mouseExitedProperty;
	// Listens to whether the mouse is outside of the scene
	private ChangeListener<? super Boolean> mouseExitedListener;
	// Whether the window is outside of acceptable bounds
	private BooleanProperty windowOutOfBoundsProperty;
	// Listens to whether the window is outside of acceptable bounds
	private ChangeListener<? super Boolean> windowOutOfBoundsListener;
	// Whether the window is maximized
	private BooleanProperty maximizedProperty;
	// Property for whether or not the game is over
	private BooleanProperty gameOverProperty;
	// Current speed of obstacle circles
	private double obstacleCircleSpeed;
	// Property for interval at which new obstacle circles are created
	private DoubleProperty obstacleCircleIntervalProperty;
	// Duration over which catch circles will fade out
	private double catchCircleFadeDuration;
	// Time the user started the game
	private double startTime;
	// Time the user ended the game
	private double endTime;
	// Whether the corner circle timeline is currently playing
	private boolean cornerCirclesActive;

	
	/**
	 * Instantiates a new cursor dodge game
	 * @param messageLabel The message label that will be used to display
	 * messages to the player
	 */
	public Game(Label messageLabel) {
		super();
		this.messageLabel = messageLabel;
		
		// Initialize game properties
		cursorPosProperty = new SimpleObjectProperty<Position>(new Position());
		mouseExitedProperty = new SimpleBooleanProperty();
		windowOutOfBoundsProperty = new SimpleBooleanProperty();
		maximizedProperty = new SimpleBooleanProperty();
		gameOverProperty = new SimpleBooleanProperty(false);
		obstacleCircleSpeed = OBSTACLE_CIRCLE_INITIAL_SPEED;
		obstacleCircleIntervalProperty = new SimpleDoubleProperty(
				OBSTACLE_CIRCLE_INITIAL_INTERVAL);
		catchCircleFadeDuration = CATCH_CIRCLE_INITIAL_DURATION;
		
		Platform.runLater(() -> {
			
			// Initialize some properties
			Scene scene = this.getScene();
			Stage stage = (Stage)scene.getWindow();
			scene.setOnMouseExited(event -> {
				mouseExitedProperty.set(true);
			});
			scene.setOnMouseEntered(event -> {
				mouseExitedProperty.set(false);
			});
			windowOutOfBoundsProperty.bind(Bindings.createBooleanBinding(() -> {
				if (!gameOverProperty.get())
					return windowOutOfBounds();
				else
					return false;
			}, stage.xProperty(), stage.yProperty(),
					stage.widthProperty(), stage.heightProperty()));
			maximizedProperty.bind(stage.maximizedProperty());
			
			// Only continue if the window is not maximized
			if (maximizedProperty.get()) {
				messageLabel.setText("Please unmaximize the window.");
				maximizedProperty.addListener((obs, oldVal, newVal) -> {
					if (!newVal) {
						messageLabel.setText(
								"Please move your cursor inside the window.");
						Platform.runLater(() -> {
							// Build the pane
							buildPane();
							// Start prompt
							displayMoveCursor();
						});
					}
				});
			}
			else {
				// Build the pane
				buildPane();
				// Start prompt
				displayMoveCursor();
			}
		});
	}
	
	
	/**
	 * Constructs this pane
	 */
	private void buildPane() {
		
		// Initialize variables
		Scene scene = this.getScene();
		Rectangle2D bounds = Screen.getPrimary().getBounds();
		
		screenWidth = bounds.getWidth();
		screenHeight = bounds.getHeight();
		sceneWidth = scene.getWidth();
		sceneHeight = scene.getHeight();
		
		double screenArea = screenWidth * screenHeight;
		circleRadius = Math.sqrt(screenArea * CIRCLE_SIZE) / 2;
		
		
		// Create one new circle immediately
		firstObstacleCircle = newObstacleCircle();
		
		// If the mouse moves, recalculate its position
		this.getScene().setOnMouseMoved(new MouseMoveHandler());
		
		// If the window is resized, end the game before it begins
		ChangeListener<Number> resizeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				gameOver(GameOverReason.RESIZE);
			}
		};
		Window window = this.getScene().getWindow();
		window.widthProperty().addListener(resizeListener);
		window.heightProperty().addListener(resizeListener);
		gameOverProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue == true) {
				window.widthProperty().removeListener(resizeListener);
				window.heightProperty().removeListener(resizeListener);
			}
		});
	}
	

	/**
	 * Creates and returns a new obstacle circle
	 * @return The obstacle circle
	 */
	private Ellipse newObstacleCircle() {
		Ellipse circle = new Ellipse(circleRadius, circleRadius);
		circle.setFill(OBSTACLE_CIRCLE_PAINT);
		
		// Place starting point at random position excluding an area around the
		// cursor
		Position startPosition = randomPositionExcludeCursorVicinity();
		circle.setTranslateX(startPosition.getX());
		circle.setTranslateY(startPosition.getY());
		
		// Game over if player hits circle
		circle.addEventHandler(MouseEvent.MOUSE_ENTERED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				gameOver(GameOverReason.OBSTACLE);
			}
		});
		this.getChildren().add(0, circle);	// Index 0 so behind catch circle
		
		return circle;
	}
	
	
	/**
	 * Creates and returns a new catch circle
	 * @return The catch circle
	 */
	private Ellipse newCatchCircle() {
		Ellipse circle = new Ellipse(circleRadius, circleRadius);
		circle.setFill(CATCH_CIRCLE_PAINT);
		
		// Place at random position
		Position position = randomPosition();
		circle.setTranslateX(position.getX());
		circle.setTranslateY(position.getY());
		
		// Remove circle if player hits it
		circle.addEventHandler(MouseEvent.MOUSE_ENTERED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				((Pane)circle.getParent()).getChildren().remove(circle);
			}
		});
		
		this.getChildren().add(circle);
		
		return circle;
	}
	
	
	/**
	 * Creates and returns a new corner circle
	 * @return The corner circle
	 */
	private Ellipse newCornerCircle() {
		Ellipse circle = new Ellipse(circleRadius, circleRadius);
		circle.setFill(CORNER_CIRCLE_PAINT);
		
		// Game over if player hits circle
		circle.addEventHandler(MouseEvent.MOUSE_ENTERED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				gameOver(GameOverReason.OBSTACLE);
			}
		});
		
		this.getChildren().add(circle);
		
		return circle;
	}
	
	
	/**
	 * Creates and returns an animation of the given obstacle circle moving
	 * in a straight line in a random direction until it hits the edge of the
	 * scene. Upon finishing, it will create a new animation moving in a new
	 * direction as if bouncing against the edge of the window.
	 * @param circle The obstacle circle
	 * @return The animation
	 */
	private Animation newObstacleCircleAnimation(Ellipse circle) {
		return newCircleAnimation(circle, randomAngle());
	}
	
	
	/**
	 * Creates and returns an animation of the given obstacle circle moving
	 * in a straight line in the given direction until it hits the edge of the
	 * scene. Upon finishing, it will create a new animation moving in a new
	 * direction as if bouncing against the edge of the window.
	 * @param circle The obstacle circle
	 * @param angle The angle in which the circle will move
	 * @return The animation
	 */
	private Animation newCircleAnimation(Ellipse circle, double angle) {
		TranslateTransition tt = new TranslateTransition();
		tt.setNode(circle);
		tt.setInterpolator(Interpolator.LINEAR);
		CollisionPoint collisionPoint = collisionPoint(circle, angle);
		tt.setDuration(Duration.seconds(time(
				new Position(circle.getTranslateX(), circle.getTranslateY()),
				new Position(collisionPoint.getX(), collisionPoint.getY()))));
		tt.setToX(collisionPoint.getX());
		tt.setToY(collisionPoint.getY());
		
		// Start the next animation upon finishing
		tt.setOnFinished(e -> {
			double newAngle = newAngle(collisionPoint, angle);
			newCircleAnimation(circle, newAngle).play();
		});
		
		// Stop the animation on game over
		gameOverProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue == true) {
				tt.stop();
			}
		});
		
		return tt;
	}
	
	
	/**
	 * Creates and returns an animation of a catch circle fading out to
	 * complete transparency over a duration of time. If the player does not
	 * move the cursor to the circle in time, it will cause a game over.
	 * @param circle The circle
	 * @return The animation
	 */
	private Animation newCatchCircleAnimation(Ellipse circle) {
		FadeTransition ft = new FadeTransition(
				Duration.seconds(catchCircleFadeDuration),
				circle);
		ft.setInterpolator(Interpolator.LINEAR);
		ft.setToValue(0.0);
		
		// Cause game over if the animation completes
		ft.setOnFinished(e -> {
			gameOver(GameOverReason.DID_NOT_CATCH);
		});
		
		// Stop the animation on mouse entered
		circle.addEventHandler(MouseEvent.MOUSE_ENTERED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				ft.stop();
			}
		});
		
		// Stop the animation on game over
		gameOverProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue == true) {
				ft.stop();
			}
		});
		
		return ft;
	}
	
	
	/**
	 * Creates and returns an animation of a corner circle popping in and out of
	 * the screen at a random corner. The circle will be removed upon completion
	 * @param circle The circle
	 * @param cursorCorner The corner the cursor is in
	 * @return The animation
	 */
	private Animation newCornerCircleAnimation(Ellipse circle, Corner cursorCorner) {
		Map<Corner, Position> onscreenPositions = new HashMap<>();
		onscreenPositions.put(Corner.TOP_LEFT, new Position(0.0, 0.0));
		onscreenPositions.put(Corner.TOP_RIGHT, new Position(sceneWidth, 0.0));
		onscreenPositions.put(Corner.BOTTOM_LEFT, new Position(0.0, sceneHeight));
		onscreenPositions.put(Corner.BOTTOM_RIGHT, new Position(sceneWidth, sceneHeight));
		Map<Corner, Position> offscreenPositions = new HashMap<>();
		offscreenPositions.put(Corner.TOP_LEFT, new Position(-circleRadius, -circleRadius));
		offscreenPositions.put(Corner.TOP_RIGHT, new Position(sceneWidth + circleRadius, -circleRadius));
		offscreenPositions.put(Corner.BOTTOM_LEFT, new Position(-circleRadius, sceneHeight + circleRadius));
		offscreenPositions.put(Corner.BOTTOM_RIGHT, new Position(sceneWidth + circleRadius, sceneHeight + circleRadius));
		Corner corner;
		Random rand = new Random();
		if (rand.nextDouble() < CORNER_CIRCLE_SAME_CIRCLE_CHANCE) {
			corner = cursorCorner;
		}
		else {
			List<Corner> cornersLeft = new ArrayList<>(Arrays.asList(Corner.values()));
			cornersLeft.remove(cursorCorner);
			corner = cornersLeft.get(rand.nextInt(cornersLeft.size()));
		}
		Position onscreenPosition = onscreenPositions.get(corner);
		Position offscreenPosition = offscreenPositions.get(corner);
		
		TranslateTransition enter = new TranslateTransition(
				Duration.seconds(CORNER_CIRCLE_ANIMATION_DURATION));
		enter.setInterpolator(Interpolator.LINEAR);
		enter.setFromX(offscreenPosition.getX());
		enter.setFromY(offscreenPosition.getY());
		enter.setToX(onscreenPosition.getX());
		enter.setToY(onscreenPosition.getY());
		
		TranslateTransition exit = new TranslateTransition(
				Duration.seconds(CORNER_CIRCLE_ANIMATION_DURATION));
		exit.setInterpolator(Interpolator.LINEAR);
		exit.setFromX(onscreenPosition.getX());
		exit.setFromY(onscreenPosition.getY());
		exit.setToX(offscreenPosition.getX());
		exit.setToY(offscreenPosition.getY());
		
		SequentialTransition animation = new SequentialTransition (circle,
			enter,
			new PauseTransition(
					Duration.seconds(CORNER_CIRCLE_DURATION)),
			exit
		);
		
		// Remove the circle upon finishing
		animation.setOnFinished(e -> {
			((Pane)circle.getParent()).getChildren().remove(circle);
		});
		
		// Stop the timeline on game over
		gameOverProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue == true) {
				animation.stop();
			}
		});
		
		return animation;
	}
	
	
	/**
	 * Calculates the point at which a circle will collide with the edge of the
	 * scene, given an angle of movement.
	 * @param circle The circle
	 * @param angle The angle of the direction of movement
	 * @return The collision point
	 * @throws IllegalArgumentException If an invalid angle was given and the
	 * collision point could not be calculated
	 */
	private CollisionPoint collisionPoint(Ellipse circle, double angle)
			throws IllegalArgumentException {
		double fromX = circle.getTranslateX();
		double fromY = circle.getTranslateY();
		
		// Create a list of possible intersections
		List<CollisionPoint> possibleIntersections =
				new ArrayList<CollisionPoint>();
		
		// Calculate the slope to create line of movement
		double dirAngleRadians = Math.toRadians(angle);
		double slope = Math.tan(dirAngleRadians);
		
		if (slope != 0.0) {
			double xIntersection;
			// Check if the top edge intersects with the line
			xIntersection = fromX + fromY / slope;
			xIntersection -= circle.getRadiusY() / slope;
			if (xIntersection - circle.getRadiusY() >= 0.0 &&
					xIntersection + circle.getRadiusY() <= sceneWidth) {
				possibleIntersections.add(new CollisionPoint(Edge.TOP,
						xIntersection, 0.0 + circle.getRadiusY()));
			}
			
			// Check if the bottom edge intersects with the line
			xIntersection = fromX - (sceneHeight - fromY) / slope;
			xIntersection += circle.getRadiusY() / slope;
			if (xIntersection - circle.getRadiusY() >= 0.0 &&
					xIntersection + circle.getRadiusY() <= sceneWidth) {
				possibleIntersections.add(new CollisionPoint(Edge.BOTTOM,
						xIntersection, sceneHeight - circle.getRadiusY()));
			}
		}
		if (slope != Double.POSITIVE_INFINITY) {
			double yIntersection;
			// Check if the left edge intersects with the line
			yIntersection = fromY + slope * fromX;
			yIntersection -= circle.getRadiusX() * slope;
			if (yIntersection - circle.getRadiusX() >= 0.0 &&
					yIntersection + circle.getRadiusX() <= sceneHeight) {
				possibleIntersections.add(new CollisionPoint(Edge.LEFT,
						0.0 + circle.getRadiusX(), yIntersection));
			}
			
			// Check if the right edge intersects with the line
			yIntersection = fromY - slope * (sceneWidth - fromX);
			yIntersection += circle.getRadiusX() * slope;
			if (yIntersection - circle.getRadiusX() >= 0.0 &&
					yIntersection + circle.getRadiusX() <= sceneHeight) {
				possibleIntersections.add(new CollisionPoint(Edge.RIGHT,
						sceneWidth - circle.getRadiusX(), yIntersection));
			}
		}
		
		// If there is one possible intersection, return it
		if (possibleIntersections.size() == 1) {
			return possibleIntersections.get(0);
		}
		
		// If there are multiple, find the one with the smallest angle between
		// the direction of the line and the edge and return it
		else if (possibleIntersections.size() > 1) {
			double smallestAngle = Double.MAX_VALUE;
			CollisionPoint smallestIntersection = null;
			for (CollisionPoint curIntersection : possibleIntersections) {
				double curAngle = Double.MAX_VALUE;
				switch (curIntersection.getEdge()) {
				case TOP:
					curAngle = Math.abs(dirAngleRadians - Math.PI / 2);
					break;
				case RIGHT:
					curAngle = Math.min(
							Math.abs(dirAngleRadians),
							Math.abs(dirAngleRadians - 2 * Math.PI));
					break;
				case BOTTOM:
					curAngle = Math.abs(dirAngleRadians - 3 * Math.PI / 2);
					break;
				case LEFT:
					curAngle = Math.abs(dirAngleRadians - Math.PI);
					break;
				}

				if (curAngle < smallestAngle) {
					smallestAngle = curAngle;
					smallestIntersection = curIntersection;
				}
			}
			
			return smallestIntersection;
		}
		
		// Throw an exception if none of these equations worked
		throw new IllegalArgumentException(
				String.format("fromX: %f\nfromY: %f\nangle: %f\nOne of these " +
						"numbers did not allow for a correct collision point " +
						"equation.", fromX, fromY, angle));
	}

	
	/**
	 * Calculates the reflection of the angle given the edge of a collision
	 * point. A bit of randomness is added.
	 * @param collisionPoint The collision point of the circle
	 * @param angle The incoming angle
	 * @return The outgoing reflected angle
	 */
	private double newAngle(CollisionPoint collisionPoint, double angle) {
		
		// Get the angle of the axis which the angle will be reflected upon
		double reflectionAxis = 0.0;
		switch (collisionPoint.getEdge()) {
		case TOP:
			reflectionAxis = 90;
			break;
		case RIGHT:
			reflectionAxis = 360;
			break;
		case BOTTOM:
			reflectionAxis = 270;
			break;
		case LEFT:
			reflectionAxis = 180;
			break;
		}
		
		// Calculate the reflected angle
		double reflectedAngle =
				(180 + reflectionAxis - (angle - reflectionAxis)) % 360;
		
		// Get the range of acceptable values for the new angle
		double rangeMin = reflectedAngle - NEW_ANGLE_RANGE / 2;
		double rangeMax = reflectedAngle + NEW_ANGLE_RANGE / 2;
		switch (collisionPoint.getEdge()) {
		case TOP:
			rangeMin = Math.max(rangeMin, 180);
			rangeMax = Math.min(rangeMax, 360);
			break;
		case RIGHT:
			rangeMin = Math.max(rangeMin, 90);
			rangeMax = Math.min(rangeMax, 270);
			break;
		case BOTTOM:
			rangeMin = Math.max(rangeMin, 0);
			rangeMax = Math.min(rangeMax, 180);
			break;
		case LEFT:
			if (reflectedAngle > 0 && reflectedAngle < 90) {
				rangeMin = Math.max(rangeMin, -90);
				rangeMax = Math.min(rangeMax, 90);
			}
			else if (reflectedAngle > 270 && reflectedAngle < 360) {
				rangeMin = Math.max(rangeMin, 270);
				rangeMax = Math.min(rangeMax, 450);
			}
			break;
		}
		
		// The new angle is a random angle between the min and max range
		Random rand = new Random();
		double newAngle = rangeMin + (rangeMax - rangeMin) * rand.nextDouble();
		newAngle = (newAngle % 360 + 360) % 360;
		return newAngle;
	}
	
	
	/**
	 * Given the current velocity of the circles, calculates the time it would
	 * take for a circle to travel from an initial position to a final position
	 * @param initialPos The initial position
	 * @param finalPos The final position
	 * @return The time
	 */
	private double time(Position initialPos, Position finalPos) {
		
		// Calculate the distance traveled
		double distanceX = Math.abs(initialPos.getX() - finalPos.getX());
		double distanceY = Math.abs(initialPos.getY() - finalPos.getY());
		double distance = Math.sqrt(
				Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
		
		// Calculate the time based on that
		double time = distance / obstacleCircleSpeed;
		return time;
	}
	
	
	/**
	 * @return A random angle in degrees
	 */
	private double randomAngle() {
		Random rand = new Random();
		return rand.nextDouble() * 360.0;
	}
	
	
	/**
	 * @return A random position
	 */
	private Position randomPosition() {
		Random rand = new Random();
		double x = circleRadius +
				(sceneWidth - 2 * circleRadius) * rand.nextDouble();
		double y = circleRadius + 
				(sceneHeight - 2 * circleRadius) * rand.nextDouble();
		return new Position(x, y);
	}
	
	
	/**
	 * @return A random position excluding an area around the current position
	 * of the cursor
	 */
	private Position randomPositionExcludeCursorVicinity() {
		Random rand = new Random();
		Position cursorPos = cursorPosProperty.get();
		
		// If the cursor is not on the scene yet, just return a random position
		// on the scene
		if (cursorPos.isEmpty()) {
			return randomPosition();
		}
		
		// Create a list of valid positions where the center of a circle can
		// be placed
		List<Position> validPositions = new ArrayList<Position>();
		for (int x = (int) circleRadius;
				x < sceneWidth - circleRadius; x++) {
			for (int y = (int) circleRadius;
					y < sceneHeight - circleRadius; y++) {
				// Add a position to the list if it is not inside the cursor
				// box
				if (!(x >= cursorPos.getX() - sceneWidth * MOUSE_BOX_SIZE / 2 &&
					x <= cursorPos.getY() + sceneWidth * MOUSE_BOX_SIZE / 2 &&
					y >= cursorPos.getY() - sceneHeight * MOUSE_BOX_SIZE / 2 &&
					y <= cursorPos.getY() + sceneHeight * MOUSE_BOX_SIZE / 2)) {
					validPositions.add(new Position(x, y));
				}
			}
		}
		
		// Return a random position from the list
		return validPositions.get(rand.nextInt(validPositions.size()));
	}
	
	
	/**
	 * Changes the text of the message label to a prompt telling the player to
	 * move the cursor. Adds a listener that detects whether the cursor has been
	 * moved. If so, calls the countdown method.
	 */
	private void displayMoveCursor() {
		// The default text of the label should show the prompt, but if the
		// window is outside of bounds, display specialized text
		if (windowOutOfBounds()) {
			messageLabel.setText(
					"Please move the window fully inside the screen.");
			
			// Start the game when the user moves the window to a valid position
			windowOutOfBoundsProperty.addListener(
					new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> obs,
						Boolean oldVal, Boolean newVal) {
					windowOutOfBoundsProperty.removeListener(this);
					displayCountdownAndStartGame();
				};
			});
		}
		
		// Start the game when the user moves the cursor inside
		else {
			cursorPosProperty.addListener(new ChangeListener<Position>() {
				@Override
				public void changed(ObservableValue<? extends Position> obs,
						Position oldVal, Position newVal) {
					cursorPosProperty.removeListener(this);
					displayCountdownAndStartGame();
				};
			});
		}
	}
	
	
	/**
	 * Changes the text of the message label to a prompt telling the player to
	 * move the cursor back in bounds or move the window back in bounds,
	 * depending on what needs to happen. Adds a listener that detects whether
	 * the cursor or window has been moved in bounds. If so, calls the countdown
	 * method.
	 */
	private void displayCorrectionNeeded() {
		if (windowOutOfBounds()) {
			messageLabel.setText(
					"Please move the window back inside the screen.");
			windowOutOfBoundsProperty.addListener(
					new ChangeListener<Boolean>() {
				@Override public void changed(
						ObservableValue<? extends Boolean> obs,
						Boolean oldVal, Boolean newVal) {
				if (!newVal) {
					windowOutOfBoundsProperty.removeListener(this);
					displayCountdownAndStartGame();
				}
			}});
		}
		
		else if (mouseExitedProperty.get()) {
			messageLabel.setText(
					"Please move the cursor back inside the window.");
			ChangeListener<? super Boolean> alsoOutOfBoundsListener =
					(obs, oldVal, newVal) -> {
				if (newVal) {
					messageLabel.setText(
							"Please move the window back inside the screen.");
				}
				else {
					messageLabel.setText(
							"Please move the cursor back inside the window.");
				}
			};
			
			windowOutOfBoundsProperty.addListener(alsoOutOfBoundsListener);
			mouseExitedProperty.addListener(
					new ChangeListener<Boolean>() {
				@Override public void changed(
						ObservableValue<? extends Boolean> obs,
						Boolean oldVal, Boolean newVal) {
				if (!newVal && !windowOutOfBounds()) {
					windowOutOfBoundsProperty.removeListener(
							alsoOutOfBoundsListener);
					mouseExitedProperty.removeListener(this);
					displayCountdownAndStartGame();
				}
			}});
		}

		windowOutOfBoundsProperty.removeListener(windowOutOfBoundsListener);
		mouseExitedProperty.removeListener(mouseExitedListener);
	}
	
	
	/**
	 * Changes the text of the message label to a countdown after which the game
	 * will start. Also adds a listener that detects whether the cursor or
	 * window moves out of bounds If so, stops the countdown and calls the out
	 * of bounds prompt method.
	 */
	private void displayCountdownAndStartGame() {
		// Inner class to keep track of the current count and return the
		// countdown timeline so that it can be stopped if need be.
		class Countdown {
			private int count;					// Current count
			private Timeline countdownTimeline;	// Countdown timeline
			
			/**
			 * Starts a new countdown, displaying it on the message label
			 */
			public Countdown() {
				count = COUNTDOWN_FROM;
				
				messageLabel.setText(String.valueOf(count));
				
				countdownTimeline = new Timeline(
						new KeyFrame(
								Duration.seconds(COUNTDOWN_DURATION),
								new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent e) {
						count--;
						
						if (count == 0) {
							messageLabel.setText("Go!");
							startGame();
						} else if (count > 0) {
							messageLabel.setText(String.valueOf(count));
						}
					}
				}));
				countdownTimeline.setCycleCount(COUNTDOWN_FROM + 1);
				countdownTimeline.setOnFinished(e -> {
					messageLabel.setVisible(false);
				});
				
				countdownTimeline.play();
			}
			
			/**
			 * @return The countdown timeline
			 */
			public Timeline getCountdownTimeline() {
				return countdownTimeline;
			}
		}
		Countdown countdown = new Countdown();
		
		// Stop the countdown and show a correction prompt if the cursor goes
		// out of bounds or window goes out of bounds
		mouseExitedListener = (obs, oldVal, newVal) -> {
			countdown.getCountdownTimeline().stop();
			displayCorrectionNeeded();
		};
		mouseExitedProperty.addListener(mouseExitedListener);
		windowOutOfBoundsListener = (obs, oldVal, newVal) -> {
			countdown.getCountdownTimeline().stop();
			displayCorrectionNeeded();
		};
		windowOutOfBoundsProperty.addListener(windowOutOfBoundsListener);
		
		if (maximizedProperty.get() ||
				mouseExitedProperty.get() ||
				windowOutOfBoundsProperty.get()) {
			countdown.getCountdownTimeline().stop();
			displayCorrectionNeeded();
		}
		
		countdown.getCountdownTimeline().setOnFinished(event -> {
			messageLabel.setVisible(false);
			mouseExitedProperty.removeListener(mouseExitedListener);
			windowOutOfBoundsProperty.removeListener(windowOutOfBoundsListener);
		});
		
	}
	
	
	/**
	 * Starts a new game
	 */
	private void startGame() {
		
		// Now, if the mouse exits the pane, end the game
		mouseExitedProperty.addListener((obs, oldVal, newVal) -> {
			if (newVal) gameOver(GameOverReason.OUT_OF_BOUNDS);
		});
		
		// Start moving the first obstacle circle
		newObstacleCircleAnimation(firstObstacleCircle).play();
		
		// Create the first task that creates a new obstacle circle after an
		// interval
		Timeline firstNewObstacleCircleTimeline = new Timeline(
				new KeyFrame(
						Duration.seconds(obstacleCircleIntervalProperty.get()),
						new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Ellipse circle = newObstacleCircle();
				newObstacleCircleAnimation(circle).play();
				
				// Multiply the obstacle circle interval
				obstacleCircleIntervalProperty.set(
						obstacleCircleIntervalProperty.get() +
						OBSTACLE_CIRCLE_INTERVAL_ADDER);
			}
		}));
		
		// Create a listener that creates the remaining tasks that create new
		// obstacle circles after increasingly large intervals
		obstacleCircleIntervalProperty.addListener(
				(observable, oldValue, newValue) -> {
			Timeline newObstacleCircleTimeline = new Timeline(
					new KeyFrame(
							Duration.seconds(
									obstacleCircleIntervalProperty.get()),
							new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					Ellipse circle = newObstacleCircle();
					newObstacleCircleAnimation(circle).play();
					
					// Multiply the obstacle circle interval
					obstacleCircleIntervalProperty.set(
							obstacleCircleIntervalProperty.get() +
							OBSTACLE_CIRCLE_INTERVAL_ADDER);
				}
			}));
			newObstacleCircleTimeline.play();
			gameOverProperty.addListener((obs, oldVal, newVal) -> {
				if (newVal == true) {
					newObstacleCircleTimeline.stop();
				}
			});
		});
		
		// Create an initial task that creates a circle the user must grab after
		// a delay.
		Timeline initialCatchCircleTimeline = new Timeline(
				new KeyFrame(Duration.seconds(CATCH_CIRCLE_DELAY),
						new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Ellipse circle = newCatchCircle();
				newCatchCircleAnimation(circle).play();
			}
		}));

		// After the initial task, create a new task that repeatedly creates new
		// circles the user must grab after an interval
		Timeline newCatchCircleTimeline = new Timeline(
				new KeyFrame(Duration.seconds(CATCH_CIRCLE_INTERVAL),
						new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Ellipse circle = newCatchCircle();
				newCatchCircleAnimation(circle).play();
				catchCircleFadeDuration *= CATCH_CIRCLE_DURATION_MULTIPLIER;
			}
		}));
		newCatchCircleTimeline.setCycleCount(Timeline.INDEFINITE);
		initialCatchCircleTimeline.setOnFinished(e -> {
			newCatchCircleTimeline.play();
		});
		
		// If the cursor position reaches any of the corners, start the corner
		// obstacle circles appearing
		cursorPosProperty.addListener((obs, oldVal, newVal) -> {
			if (cursorInCorner() && !cornerCirclesActive) {
				new CornerCircleTimeline().play();
			}
		});
		
		// Create a task that constantly updates game values
		Timeline updateTimeline = new Timeline(
				new KeyFrame(Duration.seconds(1.0),
						new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				obstacleCircleSpeed += OBSTACLE_CIRCLE_ACCELERATION;
			}
		}));
		updateTimeline.setCycleCount(Timeline.INDEFINITE);
		
		// Stop the timelines on game over
		gameOverProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue == true) {
				firstNewObstacleCircleTimeline.stop();
				initialCatchCircleTimeline.stop();
				newCatchCircleTimeline.stop();
				updateTimeline.stop();
			}
		});
		
		// Start the animations
		firstNewObstacleCircleTimeline.play();
		initialCatchCircleTimeline.play();
		updateTimeline.play();
		
		// Record the start time
		startTime = System.nanoTime();
	}
	
	
	/**
	 * Ends the game
	 * @param reason The reason the game has ended
	 */
	private void gameOver(GameOverReason reason) {
		if (gameOverProperty.get()) return;
		
		// Record the end timer
		endTime = System.nanoTime();
		
		gameOverProperty.set(true);
		
		double time = (endTime - startTime) / 1e9;
		
		// If the game has not started yet (screen resized, etc.), don't show
		// a score.
		if (startTime == 0.0) {
			CursorDodgeApplication.switchToGameOverScene(reason);
		}
		
		else {
			CursorDodgeApplication.switchToGameOverScene(reason, time,
					sceneWidth, sceneHeight, screenWidth, screenHeight);
		}
	}
	
	
	/**
	 * Updates a variable with the current position of the cursor whenever it
	 * moves
	 * @author Alex Gill
	 *
	 */
	private class MouseMoveHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent e) {
			cursorPosProperty.set(new Position(e.getSceneX(), e.getSceneY()));
		}
	}
	
	
	/**
	 * Creates a timeline that regularly causes circles appear in the corners
	 * of the window. Automatically stops if the user moves out of the corner
	 * or the game ends.
	 * @author Alex Gill
	 *
	 */
	private class CornerCircleTimeline {
		private Timeline timeline;
		ChangeListener<Boolean> gameOverListener;
		ChangeListener<Position> cursorMoveListener;
		public CornerCircleTimeline() {
			// Create timeline that makes corner circles appear regularly
			timeline = new Timeline(new KeyFrame(Duration.ZERO,
							new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					Ellipse circle = newCornerCircle();
					Animation animation = newCornerCircleAnimation(circle, cursorCorner());
					// Stop the timeline upon game over
					gameOverListener = new ChangeListener<Boolean>() {
						@Override
						public void changed(ObservableValue<? extends Boolean> obs,
								Boolean oldVal, Boolean newVal) {
						if (newVal) {
							timeline.stop();
							removeListeners();
							cornerCirclesActive = false;
						}
					}};
					// Stop timeline upon player moving away
					cursorMoveListener = new ChangeListener<Position>() {
						@Override
						public void changed(ObservableValue<? extends Position> obs,
								Position oldVal, Position newVal) {
						if (!cursorInCorner()) {
							timeline.stop();
							removeListeners();
							cornerCirclesActive = false;
						}
					}};
					addListeners();
					animation.play();
				}
			}), new KeyFrame(Duration.seconds(CORNER_CIRCLE_INTERVAL)));
			timeline.setCycleCount(Timeline.INDEFINITE);
		}
		private void addListeners() {
			gameOverProperty.addListener(gameOverListener);
			cursorPosProperty.addListener(cursorMoveListener);
		}
		private void removeListeners() {
			gameOverProperty.removeListener(gameOverListener);
			cursorPosProperty.removeListener(cursorMoveListener);
		}
		public void play() {
			timeline.play();
			cornerCirclesActive = true;
		}
	}
	
	
	/**
	 * @return whether the window is outside of the screen bounds
	 */
	private boolean windowOutOfBounds() {
		Stage stage = (Stage)this.getScene().getWindow();
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		double x = stage.getX();
		double y = stage.getY();
		double w = stage.getWidth();
		double h = stage.getHeight();
		boolean outOfBounds = false;
		if (x <= screenBounds.getMinX()) {
			outOfBounds = true;
		}
		else if (x + w >= screenBounds.getMaxX()) {
			outOfBounds = true;
		}
		else if (y <= screenBounds.getMinY()) {
			outOfBounds = true;
		}
		else if (y + h >= screenBounds.getMaxY()) {
			outOfBounds = true;
		}
		return outOfBounds;
	}
	
	
	/**
	 * @return Whether the cursor is in the corner of the window
	 */
	private boolean cursorInCorner() {
		Position cursorPos = cursorPosProperty.get();
		return ((cursorPos.getX() <= circleRadius || cursorPos.getX() >= sceneWidth - circleRadius) &&
				(cursorPos.getY() <= circleRadius || cursorPos.getY() >= sceneHeight - circleRadius));
	}
	
	
	/**
	 * @return The corner the curser is in, or null if none
	 */
	private Corner cursorCorner() {
		Position cursorPos = cursorPosProperty.get();
		Corner corner;
		if (cursorPos.getX() <= circleRadius && cursorPos.getY() <= circleRadius) {
			corner = Corner.TOP_LEFT;
		}
		else if (cursorPos.getX() >= sceneWidth - circleRadius && cursorPos.getY() <= circleRadius) {
			corner = Corner.TOP_RIGHT;
		}
		else if (cursorPos.getX() <= circleRadius && cursorPos.getY() >= sceneHeight - circleRadius) {
			corner = Corner.BOTTOM_LEFT;
		}
		else if (cursorPos.getX() >= sceneWidth - circleRadius && cursorPos.getY() >= sceneHeight - circleRadius) {
			corner = Corner.BOTTOM_RIGHT;
		}
		else {
			corner = null;
		}
		return corner;
	}
	
}

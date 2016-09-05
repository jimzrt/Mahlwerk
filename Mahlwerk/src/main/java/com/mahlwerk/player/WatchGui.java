package com.mahlwerk.player;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mahlwerk.base.Board;
import com.mahlwerk.base.Game;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece;
import com.mahlwerk.base.Game.Gamephase;
import com.mahlwerk.base.Game.Gamestate;
import com.mahlwerk.base.Piece.PieceColor;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Player that acts as Watching Board, no interaction possible
 * @author James Tophoven
 *
 */
public class WatchGui implements IPlayerHandler {

	public boolean active = false;
	Image blackCircle = new Image(getClass().getResourceAsStream("/img/goBlack3.png"));
	private Board board;
	SimpleStringProperty bottomLabel1 = new SimpleStringProperty();

	SimpleStringProperty bottomLabel2 = new SimpleStringProperty();

	SimpleStringProperty bottomLabel3 = new SimpleStringProperty();
	List<Button> buttons = new ArrayList<Button>();

	// public PieceColor color;
	HumanPlayerGuiController controller;

	Task<Void> dynamicTimeTask = new Task<Void>() {
		@Override
		protected Void call() throws Exception {
			while (true) {
				Platform.runLater(() -> {
					controller.bottomLabel3.setText("Zeit: " + game.getRemainingTime(getColor()) + " sek");
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					break;
				}
			}
			return null;
		}
	};

	private FillTransition fadeT = new FillTransition();
	private FillTransition fillT2 = new FillTransition();
	public Game game;
	private GridPane grid;

	AudioClip mediaPlayer = null;
	AudioClip mediaPlayerSlide = null;

	ExecutorService pool = Executors.newSingleThreadExecutor();

	private Rectangle rect;
	private Rectangle rect2;
	private AnchorPane root;

	private ArrayList<Button> stoneListBlack;

	private ArrayList<Button> stoneListWhite;

	List<StackPane> tiles = new ArrayList<StackPane>();

	SimpleStringProperty topLabel = new SimpleStringProperty();

	Image whiteCircle = new Image(getClass().getResourceAsStream("/img/goWhite3.png"));

	public WatchGui() {

		try {
			mediaPlayer = new AudioClip(getClass().getResource("/sound/klack.wav").toURI().toString());
			mediaPlayerSlide = new AudioClip(getClass().getResource("/sound/slide.wav").toURI().toString());

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}

	public WatchGui(Game game, PieceColor color) {
		this.game = game;
		this.board = game.board;

	}

	@Override
	public void close() {
		pool.shutdown();
	}

	@Override
	public PieceColor getColor() {
		return PieceColor.EMPTY;
	}

	public ImageView getImageForColor(PieceColor color) {
		switch (color) {
		case BLACK:
			return new ImageView(blackCircle);
		case WHITE:
			return new ImageView(whiteCircle);
		case EMPTY:
			return null;
		}
		return null;
	}

	private void handleButtonAction(MouseEvent event) {

		System.out.println((Piece) ((Button) event.getSource()).getUserData());

	}

	public void initializeGui() {
		try {
			FXMLLoader loader = new FXMLLoader();

			loader.setLocation(HumanPlayerGui.class.getResource("/fxml/HumanPlayerGuiFXML.fxml"));
			root = (AnchorPane) loader.load();

			controller = loader.getController();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void makeMove(Move move) {

		if (move.moveFrom != null && move.moveTo != null) {
			movePiece(move.moveFrom, move.moveTo);
		} else {
			if (move.moveFrom != null) {
				removePiece(move.moveFrom);
			}
			if (move.moveTo != null) {
				setPiece(move.moveTo);
			}
		}
		if (move.removePiece != null) {
			removePiece(move.removePiece);
		}

	}

	public synchronized void movePiece(Piece move1, Piece move2) {
		Piece moveFrom = new Piece(move1);
		moveFrom.color = PieceColor.EMPTY;
		Piece moveTo = new Piece(move2);
		mediaPlayerSlide.stop();

		Button buttonFrom = null;
		Button buttonTo = null;

		for (Button button : buttons) {
			if (((Piece) button.getUserData()).x == moveFrom.x && ((Piece) button.getUserData()).y == moveFrom.y) {

				buttonFrom = button;
			} else if (((Piece) button.getUserData()).x == moveTo.x && ((Piece) button.getUserData()).y == moveTo.y) {
				buttonTo = button;
			}

		}

		buttonFrom.setUserData(moveFrom);
		buttonTo.setUserData(moveTo);
		final Button buttonFromF = buttonFrom;
		final Button buttonToF = buttonTo;

		Point2D buttonFBounds = buttonFromF.localToScreen(0, 0);

		buttonToF.setTranslateX(buttonToF.screenToLocal(buttonFBounds).getX());
		buttonToF.setTranslateY(buttonToF.screenToLocal(buttonFBounds).getY());

		TranslateTransition tt = new TranslateTransition(Duration.seconds(.5), buttonToF);
		mediaPlayerSlide.setRate(1);

		tt.setInterpolator(Interpolator.LINEAR);

		tt.setToX(0);
		tt.setToY(0);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				synchronized (board) {
					buttonFromF.setGraphic(getImageForColor(board.getPieceColor(move1.x, move1.y)));
					buttonToF.setGraphic(getImageForColor(board.getPieceColor(move2.x, move2.y)));
				}
				tt.play();
				mediaPlayerSlide.play();
				tt.onFinishedProperty().set((ActionEvent event) -> {

					buttonToF.translateXProperty().set(0);
					buttonToF.translateYProperty().set(0);

				});

			}

		});

	}

	public void removePiece(Piece removePiece) {
		Piece move = new Piece(removePiece);
		for (Button button : buttons) {
			if (((Piece) button.getUserData()).x == move.x && ((Piece) button.getUserData()).y == move.y) {
				button.setUserData(move);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						FadeTransition fade = new FadeTransition(Duration.millis(500), button);
						fade.setFromValue(1.0);
						fade.setToValue(0);
						fade.onFinishedProperty().set((ActionEvent event) -> {

							synchronized (board) {
								button.setGraphic(getImageForColor(board.getPieceColor(removePiece.x, removePiece.y)));
								button.opacityProperty().set(1);

							}
						});

						fade.play();

					}
				});
				break;
			}
		}

	}

	@Override
	public void revertLastMove() {

		synchronized (this) {

			Move move = game.board.getLastMove();

			if (move != null) {

				if (move.moveFrom == null && move.moveTo != null) {
					if (move.moveTo.color == PieceColor.BLACK) {

						Button testbutton = new Button();
						testbutton.getStyleClass().add("boardButton");
						testbutton.setGraphic(new ImageView(blackCircle));
						Platform.runLater(() -> {
							controller.bottomBar.getChildren().add(testbutton);
						});
						if (stoneListBlack.isEmpty()) {
							testbutton.setTranslateY(0);

						} else {
							testbutton
									.setTranslateY(stoneListBlack.get(stoneListBlack.size() - 1).getTranslateY() + 35);

						}
						testbutton.setTranslateX(20);
						stoneListBlack.add(testbutton);

					} else if (move.moveTo.color == PieceColor.WHITE) {
						Button testbutton = new Button();
						testbutton.getStyleClass().add("boardButton");
						testbutton.setGraphic(new ImageView(whiteCircle));
						Platform.runLater(() -> {
							controller.topBar.getChildren().add(testbutton);
						});
						if (stoneListWhite.isEmpty()) {
							testbutton.setTranslateY(0);

						} else {
							testbutton
									.setTranslateY(stoneListWhite.get(stoneListWhite.size() - 1).getTranslateY() + 35);

						}
						testbutton.setTranslateX(-50);
						stoneListWhite.add(testbutton);
					}

				}

				if (move.removePiece != null) {
					if (move.removePiece.color == PieceColor.BLACK) {

						Button testbutton = new Button();
						testbutton.getStyleClass().add("boardButton");
						testbutton.setGraphic(new ImageView(blackCircle));
						Platform.runLater(() -> {
							controller.bottomBar.getChildren().add(testbutton);
						});
						if (stoneListBlack.isEmpty()) {
							testbutton.setTranslateY(0);

						} else {
							testbutton
									.setTranslateY(stoneListBlack.get(stoneListBlack.size() - 1).getTranslateY() + 35);

						}
						testbutton.setTranslateX(20);
						stoneListBlack.add(testbutton);

					} else if (move.removePiece.color == PieceColor.WHITE) {
						Button testbutton = new Button();
						testbutton.getStyleClass().add("boardButton");
						testbutton.setGraphic(new ImageView(whiteCircle));
						Platform.runLater(() -> {
							controller.topBar.getChildren().add(testbutton);
						});
						if (stoneListWhite.isEmpty()) {
							testbutton.setTranslateY(0);

						} else {
							testbutton
									.setTranslateY(stoneListWhite.get(stoneListWhite.size() - 1).getTranslateY() + 35);

						}
						testbutton.setTranslateX(-50);
						stoneListWhite.add(testbutton);
					}

				}

			}
		}

	}

	@Override
	public void run() {
		System.out.println("Hello Friend - Watch Gui started");
		game.addObserver(this);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Stage stage = new Stage();
				start(stage);
			}
		});

	}

	@Override
	public void setColor(PieceColor color) {

	}

	@Override
	public void setGame(Game game) {
		this.game = game;
		this.board = game.board;
	}

	public synchronized void setPiece(Piece moveTo) {

		Piece move = new Piece(moveTo);
		mediaPlayer.stop();

		if (moveTo.color == PieceColor.WHITE && !stoneListWhite.isEmpty()) {

			Button stone = stoneListWhite.remove(stoneListWhite.size() - 1);
			Platform.runLater(() -> {
				controller.topBar.getChildren().remove(stone);
			});

		} else if (moveTo.color == PieceColor.BLACK && !stoneListBlack.isEmpty()) {
			Button stone = stoneListBlack.remove(stoneListBlack.size() - 1);
			Platform.runLater(() -> {
				controller.bottomBar.getChildren().remove(stone);
			});

		}

		for (Button button : buttons) {
			if (((Piece) button.getUserData()).x == move.x && ((Piece) button.getUserData()).y == move.y) {

				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						button.toFront();
						button.setUserData(move);

						button.setTranslateX(
								-button.localToScene(0, 0).getX() + Math.round(Math.random()) * root.getWidth());
						button.setTranslateY(
								-button.localToScene(0, 0).getY() + Math.round(Math.random()) * root.getHeight());

						synchronized (board) {
							button.setGraphic(getImageForColor(board.getPieceColor(move.x, move.y)));
						}

						TranslateTransition tt = new TranslateTransition(Duration.seconds(.5), button);

						tt.setInterpolator(Interpolator.LINEAR);

						tt.setToX(0);
						tt.setToY(0);

						tt.play();
						tt.onFinishedProperty().set((ActionEvent event) -> {

							mediaPlayer.play();

						});

					}

				});
				break;

			}
		}

	}

	public void start(Stage primaryStage) {

		primaryStage.setOnCloseRequest(e -> {
			game.removePlayer(this);
			close();
			System.out.println("closing");
			return;
		});

		initializeGui();
		grid = controller.grid;

		for (int row = 0; row < Board.SIZE; row++) {
			for (int col = 0; col < Board.SIZE; col++) {
				StackPane square = new StackPane();

				Button botton = new Button();
				botton.getStyleClass().add("boardButton");

				botton.setUserData(new Piece(PieceColor.EMPTY, col, row));
				botton.setOnMouseClicked(this::handleButtonAction);
				buttons.add(botton);

				square.getChildren().add(botton);
				tiles.add(square);
				grid.add(square, col, row);

			}
		}

		controller.topLabel.textProperty().bind(topLabel);
		controller.bottomLabel1.textProperty().bind(bottomLabel1);
		controller.bottomLabel2.textProperty().bind(bottomLabel2);

		pool.submit(dynamicTimeTask);

		controller.revertButton.setOnAction((event) -> {
			game.onMoveRevert();
		});

		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setResizable(true);
		primaryStage.setMinWidth(600);
		primaryStage.setMinHeight(500);
		primaryStage.setTitle("Zuschauen");
		primaryStage.getIcons().add(new Image(
				getClass().getResourceAsStream("/img/goBlack3.png")));

		// create a listener
		final ChangeListener<Number> listener = new ChangeListener<Number>() {
			final Timer timer = new Timer(); // uses a timer to call your resize
												// method
			TimerTask task = null; // task to execute after defined delay
			final long delayTime = 150; // delay that has to pass in order to
										// consider an operation done

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, final Number newValue) {

				if (controller.gridParent.getChildren().get(0) instanceof Line) {
					controller.gridParent.getChildren().remove(0, 7);
				}
				
				Insets inset = null;
				double DEF_PAD = 6.0;
				double w = primaryStage.getWidth();
				double h = primaryStage.getHeight();
				double extra = DEF_PAD + 0.5 * Math.abs(w - h);
				if (w > h) {
					inset = new Insets(DEF_PAD, extra, DEF_PAD, extra);
				} else if (h > w) {
					inset = new Insets(extra, DEF_PAD, extra, DEF_PAD);
				} else {
					inset = new Insets(DEF_PAD);
				}

				grid.paddingProperty().set(inset);
				
				if (task != null) { // there was already a task scheduled from
									// the previous operation ...
					task.cancel(); // cancel it, we have a new size to consider
				}

				task = new TimerTask() // create new task that calls your resize
										// operation
				{
					@Override
					public void run() {


						// here you can place your resize code
						//System.out.println("resize to " + primaryStage.getWidth() + " " + primaryStage.getHeight());
						Platform.runLater(() -> {

							
							
							drawLines();

						});

					}
				};
				// schedule new task
				timer.schedule(task, delayTime);
			}
		};

		// finally we have to register the listener
		primaryStage.widthProperty().addListener(listener);
		primaryStage.heightProperty().addListener(listener);

		primaryStage.show();
		rect = new Rectangle(controller.topBar.getWidth(), controller.topBar.getHeight());
		rect.setFill(Color.rgb(55, 55, 55));
		rect.heightProperty().bind(controller.topBar.heightProperty());

		rect2 = new Rectangle(controller.bottomBar.getWidth(), controller.bottomBar.getHeight());
		rect2.setFill(Color.rgb(55, 55, 55));
		rect2.heightProperty().bind(controller.bottomBar.heightProperty());

		controller.topBar.getChildren().add(rect);
		controller.bottomBar.getChildren().add(rect2);

		stoneListWhite = new ArrayList<Button>();
		for (int i = 0; i < 9; i++) {
			Button testbutton = new Button();
			testbutton.getStyleClass().add("boardButton");
			testbutton.setGraphic(new ImageView(whiteCircle));
			controller.topBar.getChildren().add(testbutton);
			testbutton.setTranslateY(i * 35);
			testbutton.setTranslateX(-50);
			stoneListWhite.add(testbutton);
		}
		stoneListBlack = new ArrayList<Button>();
		for (int i = 0; i < 9; i++) {
			Button testbutton = new Button();
			testbutton.getStyleClass().add("boardButton");
			testbutton.setGraphic(new ImageView(blackCircle));
			controller.bottomBar.getChildren().add(testbutton);
			testbutton.setTranslateY(i * 35);
			testbutton.setTranslateX(20);
			stoneListBlack.add(testbutton);
		}


	}

	public void drawLines() {



		Bounds bounds = tiles.get(0).getBoundsInLocal();
		int width = (int) bounds.getWidth();
		int height = (int) bounds.getHeight();

		Rectangle rect = new Rectangle();
		rect.setMouseTransparent(true);
		rect.setStrokeWidth(5);
		rect.setFill(Color.TRANSPARENT);

		rect.getStyleClass().add("line");

		rect.setX(controller.gridParent.screenToLocal(tiles.get(0).localToScreen(width / 2, height / 2)).getX());
		rect.setY(controller.gridParent.screenToLocal(tiles.get(0).localToScreen(width / 2, height / 2)).getY());
		rect.setWidth((tiles.get(6).localToScreen(width / 2, height / 2)
				.subtract(tiles.get(0).localToScreen(width / 2, height / 2))).getX());
		rect.setHeight((tiles.get(48).localToScreen(width / 2, height / 2)
				.subtract(tiles.get(0).localToScreen(width / 2, height / 2))).getY());
		rect.toBack();
		controller.gridParent.getChildren().add(rect);
		rect.toBack();

		Rectangle rect2 = new Rectangle();
		rect2.setMouseTransparent(true);
		rect2.setStrokeWidth(5);
		rect2.setFill(Color.TRANSPARENT);
		// rect2.setStroke(Color.rgb(90, 46,46));
		rect2.getStyleClass().add("line");
		rect2.setX(controller.gridParent.screenToLocal(tiles.get(8).localToScreen(width / 2, height / 2)).getX());
		rect2.setY(controller.gridParent.screenToLocal(tiles.get(8).localToScreen(width / 2, height / 2)).getY());
		rect2.setWidth((tiles.get(12).localToScreen(width / 2, height / 2)
				.subtract(tiles.get(8).localToScreen(width / 2, height / 2))).getX());
		rect2.setHeight((tiles.get(36).localToScreen(width / 2, height / 2)
				.subtract(tiles.get(8).localToScreen(width / 2, height / 2))).getY());
		controller.gridParent.getChildren().add(rect2);
		rect2.toBack();

		Rectangle rect3 = new Rectangle();
		rect3.setMouseTransparent(true);
		rect3.setStrokeWidth(5);
		rect3.setFill(Color.TRANSPARENT);
		// rect3.setStroke(Color.rgb(90, 46,46));
		rect3.getStyleClass().add("line");
		rect3.setX(controller.gridParent.screenToLocal(tiles.get(16).localToScreen(width / 2, height / 2)).getX());
		rect3.setY(controller.gridParent.screenToLocal(tiles.get(16).localToScreen(width / 2, height / 2)).getY());
		rect3.setWidth((tiles.get(18).localToScreen(width / 2, height / 2)
				.subtract(tiles.get(16).localToScreen(width / 2, height / 2))).getX());
		rect3.setHeight((tiles.get(30).localToScreen(width / 2, height / 2)
				.subtract(tiles.get(16).localToScreen(width / 2, height / 2))).getY());
		controller.gridParent.getChildren().add(rect3);
		rect3.toBack();

		Line line = new Line();
		line.setStrokeWidth(2);
		// line.setStroke(Color.rgb(90, 46,46));
		line.getStyleClass().add("line");
		line.setStartX(controller.gridParent.screenToLocal(tiles.get(3).localToScreen(width / 2, height / 2)).getX());
		line.setStartY(controller.gridParent.screenToLocal(tiles.get(3).localToScreen(width / 2, height / 2)).getY());
		line.setEndX(controller.gridParent.screenToLocal(tiles.get(17).localToScreen(width / 2, height / 2)).getX());
		line.setEndY(controller.gridParent.screenToLocal(tiles.get(17).localToScreen(width / 2, height / 2)).getY());
		controller.gridParent.getChildren().add(line);
		line.toBack();

		Line line2 = new Line();
		line2.setStrokeWidth(2);
		// line2.setStroke(Color.rgb(90, 46,46));
		line2.getStyleClass().add("line");
		line2.setStartX(controller.gridParent.screenToLocal(tiles.get(21).localToScreen(width / 2, height / 2)).getX());
		line2.setStartY(controller.gridParent.screenToLocal(tiles.get(21).localToScreen(width / 2, height / 2)).getY());
		line2.setEndX(controller.gridParent.screenToLocal(tiles.get(23).localToScreen(width / 2, height / 2)).getX());
		line2.setEndY(controller.gridParent.screenToLocal(tiles.get(23).localToScreen(width / 2, height / 2)).getY());
		controller.gridParent.getChildren().add(line2);
		line2.toBack();

		Line line3 = new Line();
		line3.setStrokeWidth(2);
		// line3.setStroke(Color.rgb(90, 46,46));
		line3.getStyleClass().add("line");
		line3.setStartX(controller.gridParent.screenToLocal(tiles.get(25).localToScreen(width / 2, height / 2)).getX());
		line3.setStartY(controller.gridParent.screenToLocal(tiles.get(25).localToScreen(width / 2, height / 2)).getY());
		line3.setEndX(controller.gridParent.screenToLocal(tiles.get(27).localToScreen(width / 2, height / 2)).getX());
		line3.setEndY(controller.gridParent.screenToLocal(tiles.get(27).localToScreen(width / 2, height / 2)).getY());
		controller.gridParent.getChildren().add(line3);
		line3.toBack();

		Line line4 = new Line();
		line4.setStrokeWidth(2);
		// line4.setStroke(Color.rgb(90, 46,46));
		line4.getStyleClass().add("line");
		line4.setStartX(controller.gridParent.screenToLocal(tiles.get(31).localToScreen(width / 2, height / 2)).getX());
		line4.setStartY(controller.gridParent.screenToLocal(tiles.get(31).localToScreen(width / 2, height / 2)).getY());
		line4.setEndX(controller.gridParent.screenToLocal(tiles.get(45).localToScreen(width / 2, height / 2)).getX());
		line4.setEndY(controller.gridParent.screenToLocal(tiles.get(45).localToScreen(width / 2, height / 2)).getY());
		controller.gridParent.getChildren().add(line4);
		line4.toBack();

	}
	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof Move) {
			makeMove((Move) arg);
			return;
		}

		if (arg instanceof String) {
			if (((String) arg).equals("revert")) {
				revertLastMove();
			}
			return;
		}

		if (arg instanceof Gamestate || arg instanceof Gamephase) {
			Platform.runLater(() -> {

				bottomLabel2.set("Züge: " + board.moveCounter);

				if (arg instanceof Gamestate) {

					if (game.gameState == Gamestate.PlayerBlackTurn) {
						fadeT.stop();
						fillT2.stop();

						fadeT = new FillTransition(Duration.millis(1000), rect, Color.rgb(120, 120, 120),
								Color.rgb(55, 55, 55));
						fadeT.setInterpolator(Interpolator.LINEAR);
						fadeT.play();
						fillT2 = new FillTransition(Duration.millis(1000), rect2, Color.rgb(120, 120, 120),
								Color.rgb(55, 55, 55));
						fillT2.setInterpolator(Interpolator.LINEAR);
						fillT2.play();

						controller.topBar.setBackground(new Background(
								new BackgroundFill(Color.rgb(55, 55, 55), CornerRadii.EMPTY, Insets.EMPTY)));
						controller.bottomBar.setBackground(new Background(
								new BackgroundFill(Color.rgb(55, 55, 55), CornerRadii.EMPTY, Insets.EMPTY)));

					} else if (game.gameState == Gamestate.PlayerWhiteTurn) {

						fadeT.stop();
						fillT2.stop();

						fadeT = new FillTransition(Duration.millis(1000), rect, Color.rgb(55, 55, 55),
								Color.rgb(120, 120, 120));
						fadeT.setInterpolator(Interpolator.LINEAR);
						fadeT.play();
						fillT2 = new FillTransition(Duration.millis(1000), rect2, Color.rgb(55, 55, 55),
								Color.rgb(120, 120, 120));
						fillT2.setInterpolator(Interpolator.LINEAR);
						fillT2.play();

					}
					if (game.gameState == Gamestate.PlayerBlackTurn) {
						topLabel.set("Schwarz ist am Zug!");
					} else if (game.gameState == Gamestate.PlayerWhiteTurn) {
						topLabel.set("Weiß ist am Zug!");

					} else if (game.gameState == Gamestate.GameOver) {
						topLabel.set("Game Over!");
					}

				} else if (arg instanceof Gamephase) {

					if (game.gamePhase == Gamephase.PlayerBlackMill || game.gamePhase == Gamephase.PlayerWhiteMill) {
						bottomLabel1.set("Mühle!");
						// textField.setText("Mühle!");
						fadeT.stop();
						fillT2.stop();

						fadeT = new FillTransition(Duration.millis(300), rect, Color.rgb(55, 55, 55),
								Color.rgb(120, 120, 120));
						fadeT.setInterpolator(Interpolator.LINEAR);
						fadeT.setCycleCount(5);
						fadeT.setAutoReverse(true);
						fadeT.play();

						fillT2 = new FillTransition(Duration.millis(300), rect2, Color.rgb(55, 55, 55),
								Color.rgb(120, 120, 120));
						fillT2.setInterpolator(Interpolator.LINEAR);
						fillT2.setCycleCount(5);
						fadeT.setAutoReverse(true);
						fillT2.play();

					} else if (game.gamePhase == Gamephase.SetStones) {
						bottomLabel1.set("Phase 1: Steine setzen");
					} else if (game.gamePhase == Gamephase.MoveStones) {
						bottomLabel1.set("Phase 2: Steine versetzen");
					} else if (game.gamePhase == Gamephase.Endgame) {
						bottomLabel1.set("Phase 3: Endspiel");
					} else if (game.gamePhase == Gamephase.PlayerBlackWin) {
						bottomLabel1.set("Schwarz hat gewonnen!");
						fadeT.stop();
						fillT2.stop();
						fadeT = new FillTransition(Duration.millis(1000), rect, (Color) rect.getFill(),
								Color.DARKSEAGREEN);
						fadeT.setInterpolator(Interpolator.LINEAR);
						fadeT.play();
						fillT2 = new FillTransition(Duration.millis(1000), rect2, (Color) rect2.getFill(),
								Color.DARKSEAGREEN);
						fillT2.setInterpolator(Interpolator.LINEAR);
						fillT2.play();
						grid.setEffect(new GaussianBlur());
					} else if (game.gamePhase == Gamephase.PlayerWhiteWin) {
						bottomLabel1.set("Weiß hat gewonnen!");
						fadeT.stop();
						fillT2.stop();
						fadeT = new FillTransition(Duration.millis(1000), rect, (Color) rect.getFill(),
								Color.DARKSEAGREEN);
						fadeT.setInterpolator(Interpolator.LINEAR);
						fadeT.play();
						fillT2 = new FillTransition(Duration.millis(1000), rect2, (Color) rect2.getFill(),
								Color.DARKSEAGREEN);
						fillT2.setInterpolator(Interpolator.LINEAR);
						fillT2.play();
						grid.setEffect(new GaussianBlur());

					}

				}
			});
		}

	}

}

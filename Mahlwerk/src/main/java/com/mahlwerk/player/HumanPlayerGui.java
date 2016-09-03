package com.mahlwerk.player;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import com.mahlwerk.base.Board;
import com.mahlwerk.base.Game;
import com.mahlwerk.base.Game.Gamephase;
import com.mahlwerk.base.Game.Gamestate;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece;
import com.mahlwerk.base.Piece.PieceColor;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;

public class HumanPlayerGui implements IPlayerHandler {


	public boolean active = false;
	Image blackCircle = new Image(getClass().getResourceAsStream("/img/goBlack3.png"));
	private Board board;
	SimpleStringProperty bottomLabel1 = new SimpleStringProperty();

	SimpleStringProperty bottomLabel2 = new SimpleStringProperty();

	SimpleStringProperty bottomLabel3 = new SimpleStringProperty();
	List<Button> buttons = new ArrayList<Button>();

	public PieceColor color;
	HumanPlayerGuiController controller;

	Task<Void> dynamicTimeTask = new Task<Void>() {
		@Override
		protected Void call() throws Exception {
			while (true) {
				Platform.runLater(() -> {controller.bottomLabel3.setText("Zeit: " + game.getRemainingTime(getColor()) + " sek");});
				//updateMessage("" + );
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

	private boolean highlighted;
	private Piece highlightedPiece;



	private Gamephase myMill;
	private Gamestate myTurn;

	private Gamephase myWin;
	private Gamephase otherMill;
	private Gamestate otherTurn;
	private Gamephase otherWin;
	private Rectangle rect;
	private Rectangle rect2;
	// private BorderPane border;
	private AnchorPane root;

	AudioClip sound = null;
	AudioClip soundSlide = null;
	AudioClip mediaPlayer = null;
	AudioClip mediaPlayerSlide = null;
	private ArrayList<Button> stoneListBlack;

	private ArrayList<Button> stoneListWhite;

	List<StackPane> tiles = new ArrayList<StackPane>();

	SimpleStringProperty topLabel = new SimpleStringProperty();

	Image whiteCircle = new Image(getClass().getResourceAsStream("/img/goWhite3.png"));

	public HumanPlayerGui() {

		try {
			mediaPlayer =  new AudioClip(getClass().getResource("/sound/klack.wav").toURI().toString());
			mediaPlayerSlide = new AudioClip(getClass().getResource("/sound/slide.wav").toURI().toString());
			//mediaPlayer =  new MediaPlayer(sound);
			//mediaPlayerSlide = new MediaPlayer(soundSlide);
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}



	@Override
	public void continueTimer() {
		// TODO Auto-generated method stub

	}

//	private int evalBlockedPieces(PieceColor color) {
//		return board.getBlockedCount(Piece.toggleColor(color)) - board.getBlockedCount(color);
//	}
//
//	private int evalDoubleMillCount(PieceColor color) {
//		return board.doubleMillCount(color) - board.doubleMillCount(Piece.toggleColor(color));
//	}
//
//	private int evalNumberOfMorrises(PieceColor color) {
//		if (color == PieceColor.WHITE)
//			return board.playerWhiteMillCount - board.playerBlackMillCount;
//		else
//			return board.playerBlackMillCount - board.playerWhiteMillCount;
//
//	}
//
//	private int evalNumberOfPieces(PieceColor color) {
//		return board.getRemainingPieces(color) - board.getRemainingPieces(Piece.toggleColor(color));
//	}
//
//	private int evalThreePieceConfiguration(PieceColor color) {
//		return board.threePieceConfigurationCount(color) - board.threePieceConfigurationCount(Piece.toggleColor(color));
//	}
//
//	private int evalTwoPieceConfiguration(PieceColor color) {
//		return board.twoPieceConfigurationCount(color) - board.twoPieceConfigurationCount(Piece.toggleColor(color));
//	}
//
//	private int evaluateBoard(PieceColor color) {
//
//		if (color == PieceColor.BLACK ? (board.blackStonesSet < Game.initialPieces)
//				: (board.whiteStonesSet < Game.initialPieces)) {
//
//			return 26 * evalNumberOfMorrises(color) + 1 * evalBlockedPieces(color) + 6 * evalNumberOfPieces(color)
//			+ 12 * evalTwoPieceConfiguration(color) + 7 * evalThreePieceConfiguration(color)
//			+ 1086 * evalWin(color);
//
//		} else {
//			if (board.getRemainingPieces(color) < 4) {
//				return 43 * evalNumberOfMorrises(color) + 10 * evalBlockedPieces(color) + 8 * evalNumberOfPieces(color)
//				+ 7 * evalTwoPieceConfiguration(color) + 42 * evalDoubleMillCount(color)
//				+ 1086 * evalWin(color);
//
//			} else {
//				return 43 * evalNumberOfMorrises(color) + 10 * evalBlockedPieces(color) + 8 * evalNumberOfPieces(color)
//				+ 7 * evalTwoPieceConfiguration(color) + 42 * evalDoubleMillCount(color)
//				+ 1086 * evalWin(color);
//
//			}
//		}
//
//	}
//
//	private int evalWin(PieceColor color) {
//		if (!board.endReached()) {
//			return 0;
//		} else {
//			if (board.canMove(color, true) && board.getRemainingPieces(color) > 2) {
//				return 1;
//			} else {
//				return -1;
//			}
//		}
//	}

	@Override
	public PieceColor getColor() {
		return this.color;
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

		//System.out.println((Piece) ((Button) event.getSource()).getUserData());

		if (game.gameState != myTurn)
			return;
		boolean canMoveAnywhere = game.board.getRemainingPieces(this.getColor()) < 4;

		Piece piece = (Piece) ((Button) event.getSource()).getUserData();
		Move move = null;

		switch (game.gamePhase) {
		case SetStones:
			move = new Move(null, new Piece(this.getColor(), piece.x, piece.y), null);
			if (game.isMoveValid(this, move)) {
				game.onMoveMake(this, move);
			}
			break;
		case MoveStones:
		case Endgame:
			removeHighlightTiles();

			if (!highlighted) {
				if (board.getPieceColor(piece.x, piece.y) == this.getColor()) {

					if (game.board.getAdjacentEmtpyCount(piece) != 0 || canMoveAnywhere) {
						if (game.board.getRemainingPieces(this.getColor()) < 4) {
							for (Piece highlightPiece : game.board.getEmpty()) {
								highlightTile(highlightPiece.x + highlightPiece.y * Board.SIZE);
							}

						} else {
							for (Piece highlightPiece : game.board.getAdjacentEmtpy(piece)) {

								highlightTile(highlightPiece.x + highlightPiece.y * Board.SIZE);
							}
						}
						highlighted = true;
						highlightedPiece = piece;// new Piece(PieceColor.EMPTY,
						// piece.x, piece.y);

					}
				}
			} else {
				highlighted = false;
				move = new Move(new Piece(this.getColor(), highlightedPiece.x, highlightedPiece.y),
						new Piece(this.getColor(), piece.x, piece.y), null);
				if (game.isMoveValid(this, move)) {
					game.onMoveMake(this, move);

				}

			}
			break;
		case PlayerBlackMill:
		case PlayerWhiteMill:
			if (game.gamePhase == myMill) {
				move = new Move(null, null, piece);
				if (game.isMoveValid(this, move)) {
					game.onMoveMake(this, move);
				}
			}
			break;
		default:
			break;
		}

//		System.out.format(
//				"Human player - Number of Morris: %d, blockedPieces: %d, numberOfPieces: %d, twoPieces: %d, threePieces: %d, doubleMill: %d, win: %d",
//				evalNumberOfMorrises(color), evalBlockedPieces(color), evalNumberOfPieces(color),
//				evalTwoPieceConfiguration(color), evalThreePieceConfiguration(color), evalDoubleMillCount(color),
//				evalWin(color));
//		System.out.println("");
//		System.out.println("AI Score: " + evaluateBoard(color));

	}

	private void highlightTile(int index) {
		final String cssDefault = "-fx-background-color: rgba(0, 100, 100, 0.3);\n" + "-fx-background-radius: 50;\n"
				+ "-fx-background-insets: 5;\n";

		tiles.get(index).setStyle(cssDefault);

	}

	public void initializeGui() {
		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();

			loader.setLocation(HumanPlayerGui.class.getResource("/fxml/HumanPlayerGuiFXML.fxml"));
			root = (AnchorPane) loader.load();

			controller = loader.getController();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
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
		//mediaPlayerSlide.seek(Duration.ZERO);

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
		Point2D buttonTBounds = buttonToF.localToScreen(0, 0);

		double offsetX = buttonTBounds.getX() - buttonFBounds.getX();
		double offsetY = buttonTBounds.getY() - buttonFBounds.getY();

		double length = Math.max(Math.abs(offsetX), Math.abs(offsetY));
		length = length / 420;

		TranslateTransition tt = new TranslateTransition(Duration.seconds(.5), buttonFromF);
		mediaPlayerSlide.setRate(1);

		tt.setInterpolator(Interpolator.LINEAR);

		tt.setToX(buttonFromF.screenToLocal(buttonTBounds).getX());
		tt.setToY(buttonFromF.screenToLocal(buttonTBounds).getY());

		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				tt.play();
				mediaPlayerSlide.play();
				tt.onFinishedProperty().set((ActionEvent event) -> {

					synchronized (board) {
						buttonFromF.setGraphic(getImageForColor(board.getPieceColor(move1.x, move1.y)));
						buttonToF.setGraphic(getImageForColor(board.getPieceColor(move2.x, move2.y)));
					}

					buttonFromF.translateXProperty().set(0);
					buttonFromF.translateYProperty().set(0);

				});

			}

		});

	}

	private void removeHighlightTiles() {
		for (StackPane tile : tiles)
			tile.setStyle("");

	}

	public void removePiece(Piece removePiece) {
		Piece move = new Piece(removePiece);
		for (Button button : buttons) {
			if (((Piece) button.getUserData()).x == move.x && ((Piece) button.getUserData()).y == move.y) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						button.setUserData(move);

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
	public void run() {
		System.out.println("Hello Friend -  Gui started");
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
		this.color = color;
		this.myTurn = color == PieceColor.BLACK ? Gamestate.PlayerBlackTurn : Gamestate.PlayerWhiteTurn;
		this.otherTurn = myTurn == Gamestate.PlayerBlackTurn ? Gamestate.PlayerWhiteTurn : Gamestate.PlayerBlackTurn;
		this.myMill = myTurn == Gamestate.PlayerBlackTurn ? Gamephase.PlayerBlackMill : Gamephase.PlayerWhiteMill;
		this.otherMill = myMill == Gamephase.PlayerBlackMill ? Gamephase.PlayerWhiteMill : Gamephase.PlayerBlackMill;
		this.myWin = color == PieceColor.BLACK ? Gamephase.PlayerBlackWin : Gamephase.PlayerWhiteWin;
		this.otherWin = color == PieceColor.WHITE ? Gamephase.PlayerBlackWin : Gamephase.PlayerWhiteWin;

	}

	@Override
	public void setGame(Game game) {
		this.game = game;
		this.board = game.board;
	}

	public synchronized void setPiece(Piece moveTo) {

		Piece move = new Piece(moveTo);
	//	mediaPlayer.seek(Duration.ZERO);
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
		
		 Thread t2 = new Thread(dynamicTimeTask);
				 t2.setName("Tesk Time Updater");
				 t2.setDaemon(true);
				 t2.start();
		

		controller.revertButton.setOnAction((event) -> {
			
			synchronized (this) {
				
		
			Move move = game.board.getLastMove();
					
			if(move != null){
				
				if(move.moveFrom == null && move.moveTo != null){
				if(move.moveTo.color == PieceColor.BLACK){
					
					Button testbutton = new Button();
					testbutton.getStyleClass().add("boardButton");
					testbutton.setGraphic(new ImageView(blackCircle));
					Platform.runLater(() -> {
						controller.bottomBar.getChildren().add(testbutton);
					});
					if(stoneListBlack.isEmpty()){
						testbutton.setTranslateY(0);

					} else {
						testbutton.setTranslateY(stoneListBlack.get(stoneListBlack.size() - 1).getTranslateY() + 35);

					}
					testbutton.setTranslateX(20);
					stoneListBlack.add(testbutton);
					


				} else if(move.moveTo.color == PieceColor.WHITE){
					Button testbutton = new Button();
					testbutton.getStyleClass().add("boardButton");
					testbutton.setGraphic(new ImageView(whiteCircle));
					Platform.runLater(() -> {
						controller.topBar.getChildren().add(testbutton);
					});
					if(stoneListWhite.isEmpty()){
						testbutton.setTranslateY(0);

					} else {
						testbutton.setTranslateY(stoneListWhite.get(stoneListWhite.size() - 1).getTranslateY() + 35);

					}					testbutton.setTranslateX(-50);
					stoneListWhite.add(testbutton);
				}
				
				}
			
				game.onMoveRevert();

			}
			}
//			Move move = board.getLastMove();
//			if(move != null){
//					}
//			}
//			game.board.revertLastMove();
//			System.out.format(
//					"Human player - Number of Morris: %d, blockedPieces: %d, numberOfPieces: %d, twoPieces: %d, threePieces: %d, doubleMill: %d, win: %d",
//					evalNumberOfMorrises(color), evalBlockedPieces(color), evalNumberOfPieces(color),
//					evalTwoPieceConfiguration(color), evalThreePieceConfiguration(color), evalDoubleMillCount(color),
//					evalWin(color));
//			System.out.println("");
//			System.out.println("AI Score: " + evaluateBoard(color)); // game.onPlayerRevert(this);
		});
		// Thread t2 = new Thread(dynamicTimeTask);
		// t2.setName("Tesk Time Updater");
		// t2.setDaemon(true);
		// t2.start();

		// grid.getStylesheets().clear();
		//
		// border.setCenter(root);
		// root.setPadding(new Insets(0, 0, 0, 0));
		primaryStage.setScene(new Scene(root));
		primaryStage.setResizable(false);
		primaryStage.sizeToScene();
		primaryStage.setTitle(color == PieceColor.BLACK ? "Schwarz" : "Weiß");
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(color == PieceColor.BLACK ? "/img/goBlack3.png" : "/img/goWhite3.png")));

		primaryStage.show();

		rect = new Rectangle(controller.topBar.getWidth(), controller.topBar.getHeight());
		rect.setFill(Color.rgb(55, 55, 55));

		rect2 = new Rectangle(controller.bottomBar.getWidth(), controller.bottomBar.getHeight());
		rect2.setFill(Color.rgb(55, 55, 55));

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
	@Override
	public void startPlayer() {

	}
	@Override
	public void stopTimer() {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void update(Observable o, Object arg) {
		
		if(arg instanceof Move){
			makeMove((Move) arg);
			return;
		}

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

					controller.topBar.setBackground(
							new Background(new BackgroundFill(Color.rgb(55, 55, 55), CornerRadii.EMPTY, Insets.EMPTY)));
					controller.bottomBar.setBackground(
							new Background(new BackgroundFill(Color.rgb(55, 55, 55), CornerRadii.EMPTY, Insets.EMPTY)));

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
				if (game.gameState == myTurn) {
					topLabel.set("Du bist am Zug!");
				} else if (game.gameState == otherTurn) {
					topLabel.set("Dein Gegner ist am Zug!");

				} else if (game.gameState == Gamestate.GameOver) {
					topLabel.set("Game Over!");
				}

			} else if (arg instanceof Gamephase) {

				if (game.gamePhase == myMill || game.gamePhase == otherMill) {
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

				} else if (game.gamePhase == otherMill) {
					bottomLabel1.set("Dein Gegner hat eine Mühle!");
				} else if (game.gamePhase == Gamephase.SetStones) {
					bottomLabel1.set("Phase 1: Steine setzen");
				} else if (game.gamePhase == Gamephase.MoveStones) {
					bottomLabel1.set("Phase 2: Steine versetzen");
				} else if (game.gamePhase == Gamephase.Endgame) {
					bottomLabel1.set("Phase 3: Endspiel");
				} else if (game.gamePhase == myWin) {
					bottomLabel1.set("Du hast gewonnen!");
					fadeT.stop();
					fillT2.stop();
					fadeT = new FillTransition(Duration.millis(1000), rect, (Color) rect.getFill(), Color.DARKSEAGREEN);
					fadeT.setInterpolator(Interpolator.LINEAR);
					fadeT.play();
					fillT2 = new FillTransition(Duration.millis(1000), rect2, (Color) rect2.getFill(),
							Color.DARKSEAGREEN);
					fillT2.setInterpolator(Interpolator.LINEAR);
					fillT2.play();
					grid.setEffect(new GaussianBlur());
				} else if (game.gamePhase == otherWin) {
					bottomLabel1.set("Du hast verloren!");
					fadeT.stop();
					fillT2.stop();
					fadeT = new FillTransition(Duration.millis(2000), rect, (Color) rect.getFill(), Color.INDIANRED);
					fadeT.setInterpolator(Interpolator.LINEAR);
					fadeT.play();
					fillT2 = new FillTransition(Duration.millis(2000), rect2, (Color) rect2.getFill(), Color.INDIANRED);
					fillT2.setInterpolator(Interpolator.LINEAR);
					fillT2.play();

					grid.setEffect(new GaussianBlur());

				}

			}
		});

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}

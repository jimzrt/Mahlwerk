package com.mahlwerk.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mahlwerk.base.Game;
import com.mahlwerk.base.Game.Gamestate;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece;
import com.mahlwerk.base.Piece.PieceColor;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Player that acts as TCP Server/Client for network playing
 * @author james
 *
 */
public class HumanPlayerTCPServer implements IPlayerHandler {

	private PieceColor color;
	private boolean connected;

	TCPController controller;
	private Game game;
	ServerSocket hostServer;
	BufferedReader in;
	private String ip;
	BlockingQueue<Move> moveQueue = new ArrayBlockingQueue<Move>(10);
	private Gamestate myturn;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	PrintWriter out;
	ExecutorService pool = Executors.newSingleThreadExecutor();

	private Runnable r = new Runnable() {
		@Override
		public void run() {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			makeValidMove();
		}
	};

	AnchorPane root;

	private boolean server;
	Socket socket;

	private boolean terminated;

	public HumanPlayerTCPServer(String ip, boolean server) {
		this.ip = ip;
		this.server = server;
	}

	@Override
	public void close() {

		if (oos != null) {
			try {
				oos.writeObject(new Boolean(true));
			} catch (IOException e1) {

			}
		}

		terminated = true;
		pool.shutdown();
		if (oos != null) {
			try {
				oos.close();

			} catch (IOException e) {

			}
			oos = null;
		}
		if (ois != null) {
			try {
				ois.close();

			} catch (IOException e) {

			}
			ois = null;
		}

		try {
			if (socket != null)
				socket.close();
			if (hostServer != null)
				hostServer.close();

		} catch (IOException e) {

		}

		socket = null;

	}

	@Override
	public PieceColor getColor() {
		return color;
	}

	public void initializeGui() {

		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(getClass().getResource("/fxml/ChatGui.fxml"));

		try {
			root = (AnchorPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		controller = loader.getController();

		controller.sendButton.setOnAction((e) -> {
			if (!controller.textField.getText().isEmpty()) {
				controller.addText(color, controller.textField.getText());
				try {
					if (oos != null) {
						oos.writeObject(controller.textField.getText());
						oos.flush();
					}

				} catch (IOException e1) {
					System.out.println("Schließe outputstream");
					oos = null;

				}
				controller.textField.setText("");
			}

		});

		Runnable task = () -> {

			Object o = null;
			while (!terminated) {
				if (ois != null) {
					try {
						o = ois.readObject();
					} catch (ClassNotFoundException | IOException e1) {

						System.out.println("StreamTask beendet");
						break;
					}
					if (o instanceof Move) {
						moveQueue.add((Move) o);
						System.out.println("added move");

					} else if (o instanceof String) {
						System.out.println("added string");

						final String string = (String) o;

						if (string.equals("revert")) {
							game.onMoveRevert();

						} else {
							Platform.runLater(() -> {
								controller.addText(Piece.toggleColor(color), string);
							});
						}

					} else if (o instanceof Boolean) {
						System.out.println("got bool, close");
						Platform.runLater(() -> {
							controller.addText(PieceColor.EMPTY, "Verbindung geschlossen");
						});
						game.removePlayer(this);
						close();
						break;
					}

				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}

		};

		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();

	}


	public synchronized void makeValidMove() {

		waitForConnection();

		if (game.gameState == myturn || game.gameState == Gamestate.GameOver) {

			try {
				if (game.board.getLastMove() != null) {

					oos.writeObject(game.board.getLastMove());
					oos.flush();

				}
				Move move = moveQueue.take();

				game.onMoveMake(this, move);

			} catch (IOException | InterruptedException e) {
				close();
			}
		}
	}

	@Override
	public void revertLastMove() {

		waitForConnection();
		try {
			if (oos != null) {
				oos.writeObject("revert");
				oos.flush();
			}

		} catch (IOException e1) {
			System.out.println("Schließe outputstream");
			oos = null;

		}

	}

	@Override
	public void run() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Stage stage = new Stage();
				start(stage);
			}
		});

		if (server) {
			try {

				Platform.runLater(() -> {
					controller.addText(PieceColor.EMPTY, "Hallo Server. Verbinde...");
				});
				System.out.println("Verbinde...");
				hostServer = new ServerSocket(1337);
				socket = hostServer.accept();
				connected = true;
				System.out.println("Client verbunden!");
				System.out.println("");
				Platform.runLater(() -> {
					controller.addText(PieceColor.EMPTY, "Verbunden!");
				});

			} catch (IOException e) {
				System.out.println("Konnte nicht verbinden");

			}
		} else {
			Platform.runLater(() -> {
				controller.addText(PieceColor.EMPTY, "Hallo Client. Verbinde...");
			});

			while (true && !terminated) {

				try {
					socket = new Socket(ip, 1337);
				} catch (IOException e) {
					System.out.println("Konnte nicht mit Server " + ip + ":1337 verbinden");
					System.out.println("Neuer Versuch.");
					System.out.println("");

					try {
						Thread.sleep(1500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				if (socket != null)
					break;
			}
			System.out.println("Client verbunden!");
			Platform.runLater(() -> {
				controller.addText(PieceColor.EMPTY, "Verbunden!");
			});
			connected = true;

		}

		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());

		} catch (IOException e) {

			System.out.println("closing sockets");
			close();
		}

	}

	@Override
	public void setColor(PieceColor color) {

		this.color = color;
		if (color == PieceColor.BLACK)
			myturn = Gamestate.PlayerBlackTurn;
		else
			myturn = Gamestate.PlayerWhiteTurn;

	}

	@Override
	public void setGame(Game game) {
		this.game = game;
	}

	public void start(Stage primaryStage) {

		primaryStage.setOnCloseRequest(e -> {

			game.removePlayer(this);
			close();
			System.out.println("closing");
			return;
		});

		initializeGui();

		primaryStage.setScene(new Scene(root));

		primaryStage.setResizable(false);
		primaryStage.sizeToScene();
		primaryStage.setTitle("Chattoo");
		primaryStage.getIcons().add(new Image(
				getClass().getResourceAsStream(color == PieceColor.BLACK ? "/img/goBlack3.png" : "/img/goWhite3.png")));

		primaryStage.show();
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth() - 30));
		primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight() - 30));

	}

	@Override
	public void update(Observable o, Object arg) {

		if (terminated)
			return;

		if (arg instanceof String) {
			if (((String) arg).equals("revert")) {
				revertLastMove();
				return;
			}
		}

		if (!pool.isShutdown())
			pool.submit(r);

	}

	public void waitForConnection() {

		while (!terminated && !connected) {
			System.out.println("Nicht vebunden");
			System.out.println("warte...");
			System.out.println("");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

}

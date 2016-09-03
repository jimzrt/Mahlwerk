package com.mahlwerk.base;

import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mahlwerk.base.Piece.PieceColor;
import com.mahlwerk.player.IPlayerHandler;
import com.mahlwerk.player.WatchGui;
import com.mahlwerk.util.CountdownTask;

public class Game extends Observable {
	public enum Gamephase {
		Endgame, MoveStones, PlayerBlackMill, PlayerBlackWin, PlayerWhiteMill, PlayerWhiteWin, SetStones
	}

	public enum Gamestate {
		GameOver, Pause, PlayerBlackTurn, PlayerWhiteTurn, Welcome
	}

	public static final int initialPieces = 9;
	public static final int initialTime = 300;

	private IPlayerHandler blackPlayerHandler;
	public Board board = new Board();
	public Gamephase gamePhase = Gamephase.SetStones;

	public Gamestate gameState = Gamestate.Welcome;

	int pieceCounter = 0;
	ExecutorService pool = Executors.newCachedThreadPool();
	private Gamephase prevoiousGamePhase;

	int timePlayerOne = initialTime;
	int timePlayerTwo = initialTime;
	private CountdownTask taskPlayerOneCountdown = new CountdownTask(timePlayerOne);
	private CountdownTask taskPlayerTwoCountdown = new CountdownTask(timePlayerTwo);

	
	Timer timer = new Timer();
	public IPlayerHandler watchgui;

	private IPlayerHandler whitePlayerHandler;

	private synchronized void changeGamePhase(Gamephase gamePhase) {

		prevoiousGamePhase = this.gamePhase;
		this.gamePhase = gamePhase;

		setChanged();
		notifyObservers(gamePhase);

	}

	public synchronized void changeGameState(Gamestate gameState) {

		if (this.gameState == Gamestate.PlayerBlackTurn)
			pauseTimer(blackPlayerHandler);
		else if (this.gameState == Gamestate.PlayerWhiteTurn)
			pauseTimer(whitePlayerHandler);

		this.gameState = gameState;

		setChanged();
		notifyObservers(gameState);

		switch (gameState) {
		case PlayerBlackTurn:
			this.continueTimer(blackPlayerHandler);
			break;
		case PlayerWhiteTurn:
			this.continueTimer(whitePlayerHandler);
			break;
		case GameOver:
			if (whitePlayerHandler != null)
				whitePlayerHandler.close();
			if (blackPlayerHandler != null)
				blackPlayerHandler.close();
			if (watchgui != null)
				watchgui.close();
			whitePlayerHandler = null;
			blackPlayerHandler = null;
			watchgui = null;
			timer.cancel();
			taskPlayerOneCountdown.cancel();
			taskPlayerTwoCountdown.cancel();
			pool.shutdown();
			System.out.println("Game Over!");
			if (gamePhase == Gamephase.PlayerBlackWin) {
				System.out.println("Schwarz hat gewonnen!");
			} else if (gamePhase == Gamephase.PlayerWhiteMill) {
				System.out.println("Weiß hat gewonnen!");
			}
		default:
			break;
		}

	}

	private void changeTurn(PieceColor color) {
		if (color == PieceColor.BLACK) {
			changeGameState(Gamestate.PlayerWhiteTurn);
		} else {
			changeGameState(Gamestate.PlayerBlackTurn);
		}

	}

	private void continueTimer(IPlayerHandler player) {
		if (player == blackPlayerHandler) {
			taskPlayerTwoCountdown.paused = false;
		} else if (player == whitePlayerHandler) {
			taskPlayerOneCountdown.paused = false;
		}
	}

	public int getRemainingTime(PieceColor color) {
		if (color == PieceColor.WHITE) {
			return taskPlayerOneCountdown.countdown;
		} else if (color == PieceColor.BLACK) {
			return taskPlayerTwoCountdown.countdown;

		} else {
			return taskPlayerOneCountdown.countdown + taskPlayerTwoCountdown.countdown;
		}
	}

	public synchronized boolean isMoveValid(IPlayerHandler player, Move move) {
		if (player.getColor() != PieceColor.WHITE && gameState == Gamestate.PlayerWhiteTurn) {
			System.out.println("White's Turn!");
			return false;
		}
		if (player.getColor() != PieceColor.BLACK && gameState == Gamestate.PlayerBlackTurn) {
			System.out.println("Black's Turn!");
			return false;
		}
		if (move.moveTo != null && board.getPieceColor(move.moveTo.x, move.moveTo.y) != PieceColor.EMPTY) {
			System.out.println("Not Empty!");
			return false;
		}
		if (move.moveTo != null && !board.validPositions[move.moveTo.x + move.moveTo.y * Board.SIZE]) {
			System.out.println("Not a valid position!");
			return false;
		}
		if (gamePhase == Gamephase.MoveStones && move.moveFrom == null) {
			System.out.println("No piece selected to move!");
			return false;
		}
		if (move.moveFrom != null && move.moveFrom.color != player.getColor()) {
			System.out.println("Selected piece has wrong color!");
			return false;
		}
		if (move.moveTo != null && move.moveTo.color != player.getColor()) {
			System.out.println("Destination piece has wrong color!");
			return false;
		}
		if (move.removePiece != null && move.removePiece.color != Piece.toggleColor(player.getColor())) {
			System.out.println("Piece to be removed has wrong color!");
			return false;
		}
		if (move.removePiece != null && !board.isRemovable(move.removePiece)) {
			System.out.println("Piece is not removable!");
			return false;
		}
		if (move.moveFrom != null && move.moveTo != null && board.getRemainingPieces(player.getColor()) > 3) {
			List<Piece> moves = board.getAdjacentEmtpy(move.moveFrom);
			for (Piece piece : moves) {
				if (piece.x == move.moveTo.x && piece.y == move.moveTo.y)
					return true;
			}
			System.out.println("Piece is not adjacent!");
			return false;
		}

		return true;
	}

	public synchronized void onMoveMake(IPlayerHandler player, Move move) {
		if (!isMoveValid(player, move) || gameState == Gamestate.GameOver) {
			return;
		}

		board.makeMove(move);
		setChanged();
		notifyObservers(move);

		if (board.endReached() || !board.canMove(Piece.toggleColor(player.getColor()), false)) {

			if (player == whitePlayerHandler) {
				changeGamePhase(Gamephase.PlayerWhiteWin);
			} else {
				changeGamePhase(Gamephase.PlayerBlackWin);
			}
			changeGameState(Gamestate.GameOver);

			return;
		}

		if (board.checkMill(move.moveTo) && move.removePiece == null) {
			if (player == whitePlayerHandler) {
				changeGamePhase(Gamephase.PlayerWhiteMill);
			} else {
				changeGamePhase(Gamephase.PlayerBlackMill);
			}
			return;
		} else if (gamePhase == Gamephase.PlayerBlackMill || gamePhase == Gamephase.PlayerWhiteMill) {
			changeGamePhase(prevoiousGamePhase);
		}

		changeTurn(player.getColor());
		if (gamePhase == Gamephase.SetStones) {

			if (board.blackStonesSet == initialPieces && board.whiteStonesSet == initialPieces) {
				System.out.println("Move Phase begins");
				changeGamePhase(Gamephase.MoveStones);
			}
		}
		if (gamePhase == Gamephase.MoveStones) {

			if (board.getRemainingPieces(player.getColor()) < 4
					|| board.getRemainingPieces(Piece.toggleColor(player.getColor())) < 4) {
				System.out.println("Endgame begins");
				changeGamePhase(Gamephase.Endgame);
			}
		}
		System.out.print("\033[H\033[2J");

		board.print();
	}

	public synchronized void onMoveRevert() {
		Move move = board.getLastMove();
		if (move != null) {
			changeTurn(Piece.toggleColor(move.moveTo.color));

			if (move.removePiece != null) {
				setChanged();
				notifyObservers(new Move(null, move.removePiece, null));
			}
			if (move.moveFrom != null && move.moveTo != null) {
				setChanged();
				notifyObservers(new Move(move.moveTo, move.moveFrom, null));
			} else {
				if (move.moveTo != null) {
					setChanged();
					notifyObservers(new Move(null, null, move.moveTo));
				}
			}

			board.revertLastMove();
		}

	}

	private void pauseTimer(IPlayerHandler player) {
		if (player == blackPlayerHandler) {
			taskPlayerTwoCountdown.paused = true;
		} else if (player == whitePlayerHandler) {
			taskPlayerOneCountdown.paused = true;
		}

	}

	public void removePlayer(IPlayerHandler player) {
		if (gameState != Gamestate.GameOver) {
			if (player == blackPlayerHandler)
				changeGamePhase(Gamephase.PlayerWhiteWin);
			else if (player == whitePlayerHandler)
				changeGamePhase(Gamephase.PlayerBlackWin);

			changeGameState(Gamestate.GameOver);

		}
	}

	public synchronized void revertGamePhase() {
		this.gamePhase = prevoiousGamePhase;
	}

	public void setPlayer(IPlayerHandler player) {
		switch (player.getColor()) {
		case BLACK:
			this.blackPlayerHandler = player;
			break;
		case WHITE:
			this.whitePlayerHandler = player;
			break;
		default:
			throw new IllegalArgumentException("Invalid pieceColor: " + player.getColor());
		}
	}

	public void startGame(boolean watchGui) {

		if (watchGui) {

			watchgui = new WatchGui();
			watchgui.setGame(this);

			addObserver(watchgui);

			pool.submit(watchgui);
		}

		addObserver(whitePlayerHandler);
		pool.submit(whitePlayerHandler);

		addObserver(blackPlayerHandler);
		pool.submit(blackPlayerHandler);

		board.print();
		timer.scheduleAtFixedRate(taskPlayerOneCountdown, 0, 1000);
		timer.scheduleAtFixedRate(taskPlayerTwoCountdown, 0, 1000);

		changeGameState(Gamestate.PlayerWhiteTurn);
		changeGamePhase(Gamephase.SetStones);

		System.out.println();

	}

}

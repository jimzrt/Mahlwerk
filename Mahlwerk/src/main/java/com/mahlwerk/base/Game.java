package com.mahlwerk.base;

import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mahlwerk.base.Piece.PieceColor;
import com.mahlwerk.player.IPlayerHandler;
import com.mahlwerk.player.WatchGui;
import com.mahlwerk.util.CountSecondsTask;

/**
 * Game Representation
 * Keeps track of Gamephase and -state
 * Uses Observer-Pattern to notify all Players of changes
 * 
 * @author James Tophoven
 *
 */
public class Game extends Observable {
	public enum Gamephase {
		Endgame, MoveStones, PlayerBlackMill, PlayerBlackWin, PlayerWhiteMill, PlayerWhiteWin, SetStones
	}

	public enum Gamestate {
		GameOver, Pause, PlayerBlackTurn, PlayerWhiteTurn, Welcome
	}

	public static final int initialPieces = 9;

	private IPlayerHandler playerBlackHandler;
	private IPlayerHandler playerWhiteHandler;
	private IPlayerHandler watchgui;
	
	public Board board = new Board();
	public Gamephase gamePhase = Gamephase.SetStones;
	public Gamestate gameState = Gamestate.PlayerWhiteTurn;

	private ExecutorService pool = Executors.newCachedThreadPool();
	private Gamephase prevoiousGamePhase;


	private CountSecondsTask taskPlayerOneCountdown = new CountSecondsTask();
	private CountSecondsTask taskPlayerTwoCountdown = new CountSecondsTask();

	private Timer timer = new Timer();
	
	public void startGame(boolean watchGui) {

		if (watchGui) {

			watchgui = new WatchGui();
			watchgui.setGame(this);

			addObserver(watchgui);

			pool.submit(watchgui);
		}

		addObserver(playerWhiteHandler);
		pool.submit(playerWhiteHandler);

		addObserver(playerBlackHandler);
		pool.submit(playerBlackHandler);

		board.print();
		timer.scheduleAtFixedRate(taskPlayerOneCountdown, 0, 1000);
		timer.scheduleAtFixedRate(taskPlayerTwoCountdown, 0, 1000);

		changeGameState(Gamestate.PlayerWhiteTurn);
		changeGamePhase(Gamephase.SetStones);


	}


	private void changeGamePhase(Gamephase gamePhase) {

		prevoiousGamePhase = this.gamePhase;
		this.gamePhase = gamePhase;

		setChanged();
		notifyObservers(gamePhase);

	}

	private  void changeGameState(Gamestate gameState) {

		if (this.gameState == Gamestate.PlayerBlackTurn)
			pauseTimer(playerBlackHandler);
		else if (this.gameState == Gamestate.PlayerWhiteTurn)
			pauseTimer(playerWhiteHandler);

		this.gameState = gameState;

		setChanged();
		notifyObservers(gameState);

		switch (gameState) {
		case PlayerBlackTurn:
			this.continueTimer(playerBlackHandler);
			break;
		case PlayerWhiteTurn:
			this.continueTimer(playerWhiteHandler);
			break;
		case GameOver:
			if (playerWhiteHandler != null)
				playerWhiteHandler.close();
			if (playerBlackHandler != null)
				playerBlackHandler.close();
			if (watchgui != null)
				watchgui.close();
			playerWhiteHandler = null;
			playerBlackHandler = null;
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
		if (player == playerBlackHandler) {
			taskPlayerTwoCountdown.paused = false;
		} else if (player == playerWhiteHandler) {
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

			if (player == playerWhiteHandler) {
				changeGamePhase(Gamephase.PlayerWhiteWin);
			} else {
				changeGamePhase(Gamephase.PlayerBlackWin);
			}
			changeGameState(Gamestate.GameOver);

			return;
		}

		if (board.checkMill(move.moveTo) && move.removePiece == null) {
			if (player == playerWhiteHandler) {
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

		board.print();

	}

	public synchronized void onMoveRevert() {
		Move move = board.getLastMove();
		if (move != null) {

			setChanged();
			notifyObservers("revert");

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

			if (gamePhase == Gamephase.MoveStones) {

				if (board.blackStonesSet < initialPieces || board.whiteStonesSet < initialPieces) {
					System.out.println("Set Stone Phase begins");
					changeGamePhase(Gamephase.SetStones);
				}
			}

			if (gamePhase == Gamephase.Endgame) {

				if (board.getRemainingPieces(playerBlackHandler.getColor()) > 3
						&& board.getRemainingPieces(playerWhiteHandler.getColor()) > 3) {
					System.out.println("Move Phase begins");
					changeGamePhase(Gamephase.MoveStones);
				}
			}
		}

	}

	private void pauseTimer(IPlayerHandler player) {
		if (player == playerBlackHandler) {
			taskPlayerTwoCountdown.paused = true;
		} else if (player == playerWhiteHandler) {
			taskPlayerOneCountdown.paused = true;
		}

	}

	public void removePlayer(IPlayerHandler player) {
		if (gameState != Gamestate.GameOver) {
			if (player == playerBlackHandler)
				changeGamePhase(Gamephase.PlayerWhiteWin);
			else if (player == playerWhiteHandler)
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
			this.playerBlackHandler = player;
			break;
		case WHITE:
			this.playerWhiteHandler = player;
			break;
		default:
			throw new IllegalArgumentException("Invalid pieceColor: " + player.getColor());
		}
	}



}

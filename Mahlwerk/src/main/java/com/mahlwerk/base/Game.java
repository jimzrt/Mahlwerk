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
import com.sun.swing.internal.plaf.synth.resources.synth;


public class Game extends Observable{
	public enum Gamestate {
		Welcome, PlayerWhiteTurn, PlayerBlackTurn, Pause, GameOver
	}

	public enum Gamephase {
		SetStones, MoveStones, Endgame, PlayerWhiteMill, PlayerBlackMill, PlayerBlackWin, PlayerWhiteWin
	}

	public Gamestate gameState = Gamestate.Welcome;
	public Gamephase gamePhase = Gamephase.SetStones;

	private IPlayerHandler whitePlayerHandler;
	private IPlayerHandler blackPlayerHandler;
	public IPlayerHandler watchgui;

	public Board board = new Board();

	public static final int initialPieces = 9;
	public static final int initialTime = 300;
	int pieceCounter = 0;
	int timePlayerOne = initialTime;
	int timePlayerTwo = initialTime;
	



	Timer timer = new Timer();
	private CountdownTask taskPlayerOneCountdown = new CountdownTask(timePlayerOne);
	private CountdownTask taskPlayerTwoCountdown = new CountdownTask(timePlayerTwo);
	private Gamephase prevoiousGamePhase;

	ExecutorService pool = Executors.newCachedThreadPool();

	
	public void startGame(boolean watchGui) {

		if(watchGui){
			
			watchgui = new WatchGui();
			watchgui.setGame(this);

			addObserver(watchgui);
			
		//	Thread threadV= new Thread(watchgui);
		//	threadV.setDaemon(true);
		//	threadV.start();
			pool.submit(watchgui);
		}
		
		addObserver(whitePlayerHandler);
//		Thread threadB= new Thread(blackPlayerHandler);
//		threadB.setDaemon(true);
//		threadB.start();
		pool.submit(whitePlayerHandler);
		
		addObserver(blackPlayerHandler);
//		Thread threadW= new Thread(whitePlayerHandler);
//		threadW.setDaemon(true);
//		threadW.start();
		pool.submit(blackPlayerHandler);

		board.print();
		timer.scheduleAtFixedRate(taskPlayerOneCountdown, 0, 1000);
		timer.scheduleAtFixedRate(taskPlayerTwoCountdown, 0, 1000);
		
		changeGameState(Gamestate.PlayerWhiteTurn);
		changeGamePhase(Gamephase.SetStones);

		
		
		System.out.println();

	}
	
	
	public void setPlayer(IPlayerHandler player) {
	//	this.addObserver(player);
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
	
	public synchronized void onMoveMake(IPlayerHandler player, Move move){
		if(!isMoveValid(player,move) || gameState == Gamestate.GameOver){
			return;
		}
		
		//if(move.moveFrom != null)
			//move.moveFrom.color = PieceColor.EMPTY;
		board.makeMove(move);
		setChanged();
		notifyObservers(move);
		
		//whitePlayerHandler.makeMove(move);
		//blackPlayerHandler.makeMove(move);
		//if(watchgui != null)
		//	watchgui.makeMove(move);
		
		if(board.endReached() || !board.canMove(Piece.toggleColor(player.getColor()), false)){

			if(player == whitePlayerHandler){
				changeGamePhase(Gamephase.PlayerWhiteWin);
			} else {
				changeGamePhase(Gamephase.PlayerBlackWin);
			}
			changeGameState(Gamestate.GameOver);

			return;
		}
		
		if(board.checkMill(move.moveTo) && move.removePiece == null){
			if(player == whitePlayerHandler){
				changeGamePhase(Gamephase.PlayerWhiteMill);
			} else {
				changeGamePhase(Gamephase.PlayerBlackMill);
			}
			return;
		} else if(gamePhase == Gamephase.PlayerBlackMill || gamePhase == Gamephase.PlayerWhiteMill){
			board.print();
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
			
			if (board.getRemainingPieces(player.getColor()) < 4 || board.getRemainingPieces(Piece.toggleColor(player.getColor())) <4) {
				System.out.println("Endgame begins");
				changeGamePhase(Gamephase.Endgame);
			}
		}
		
		board.print();
	}

	private void changeTurn(PieceColor color) {
		if(color==PieceColor.BLACK){
			changeGameState(Gamestate.PlayerWhiteTurn);
		} else {
			changeGameState(Gamestate.PlayerBlackTurn);
		}
		
	}

	public synchronized boolean isMoveValid(IPlayerHandler player, Move move) {
		if(player.getColor() != PieceColor.WHITE && gameState == Gamestate.PlayerWhiteTurn ){
			System.out.println("White's Turn!");
			return false;
		}
		if(player.getColor() != PieceColor.BLACK && gameState == Gamestate.PlayerBlackTurn){
			System.out.println("Black's Turn!");
			return false;
		}
		if(move.moveTo != null && board.getPieceColor(move.moveTo.x, move.moveTo.y) != PieceColor.EMPTY){
			System.out.println("Not Empty!");
			return false;
		}
		if(move.moveTo != null && !board.validPositions[move.moveTo.x + move.moveTo.y * Board.SIZE]){
			System.out.println("Not a valid position!");
			return false;
		}
		if(gamePhase == Gamephase.MoveStones && move.moveFrom == null){
			System.out.println("No piece selected to move!");
			return false;
		}
		if(move.moveFrom != null && move.moveFrom.color != player.getColor()){
			System.out.println("Selected piece has wrong color!");
			return false;
		}
		if(move.moveTo != null && move.moveTo.color != player.getColor()){
			System.out.println("Destination piece has wrong color!");
			return false;
		}
		if(move.removePiece != null && move.removePiece.color != Piece.toggleColor(player.getColor())){
			System.out.println("Piece to be removed has wrong color!");
			return false;
		}
		if(move.removePiece != null && !board.isRemovable(move.removePiece)){
			System.out.println("Piece is not removable!");
			return false;
		}
		if(move.moveFrom != null && move.moveTo != null && board.getRemainingPieces(player.getColor()) > 3){
			List<Piece> moves = board.getAdjacentEmtpy(move.moveFrom);
			for(Piece piece : moves){
				if(piece.x == move.moveTo.x && piece.y == move.moveTo.y)
					return true;
			}
			System.out.println("Piece is not adjacent!");
			return false;
		}
		
		return true;
	}


	private synchronized void changeGamePhase(Gamephase gamePhase) {

		prevoiousGamePhase = this.gamePhase;
		this.gamePhase = gamePhase;
		
	

		setChanged();
		notifyObservers(gamePhase);


	}
	
	public synchronized void changeGameState(Gamestate gameState) {
		

		
		
		if(this.gameState == Gamestate.PlayerBlackTurn)
			pauseTimer(blackPlayerHandler);
		else if(this.gameState == Gamestate.PlayerWhiteTurn)
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
			if(whitePlayerHandler!= null)
				whitePlayerHandler.close();
			if(blackPlayerHandler!= null)
				blackPlayerHandler.close();
			if(watchgui != null)
				watchgui.close();
			whitePlayerHandler = null;
			blackPlayerHandler = null;
			watchgui = null;
			timer.cancel();
			taskPlayerOneCountdown.cancel();
			taskPlayerTwoCountdown.cancel();
			pool.shutdown();
			System.out.println("Game Over!");
			if(gamePhase == Gamephase.PlayerBlackWin){
				System.out.println("Schwarz hat gewonnen!");
			} else if(gamePhase == Gamephase.PlayerWhiteMill){
				System.out.println("Wei� hat gewonnen!");
			}
		default:
			break;
		}
		
		

		

	}
	
	public void removePlayer(IPlayerHandler player) {
		if(gameState != Gamestate.GameOver){
		if(player == blackPlayerHandler)
			changeGamePhase(Gamephase.PlayerWhiteWin);
		else if(player == whitePlayerHandler)
			changeGamePhase(Gamephase.PlayerBlackWin);
		
		
		changeGameState(Gamestate.GameOver);
		
		}
	}
	
	public int getRemainingTime(PieceColor color) {
		if (color == PieceColor.WHITE) {
			return taskPlayerOneCountdown.countdown;
		} else if (color == PieceColor.BLACK) {
			return taskPlayerTwoCountdown.countdown;

		} else{
			return taskPlayerOneCountdown.countdown + taskPlayerTwoCountdown.countdown;
		}
	}
	
	
	private void continueTimer(IPlayerHandler player) {
		if(player == blackPlayerHandler){
			taskPlayerTwoCountdown.paused = false;
		} else if(player == whitePlayerHandler){
			taskPlayerOneCountdown.paused = false;
		}		
	}

	private void pauseTimer(IPlayerHandler player) {
		if(player == blackPlayerHandler){
			taskPlayerTwoCountdown.paused = true;
		} else if(player == whitePlayerHandler){
			taskPlayerOneCountdown.paused = true;
		}	
		
	}

	public synchronized void revertGamePhase() {
		this.gamePhase = prevoiousGamePhase;
	}


	public synchronized void onMoveRevert() {
		Move move = board.getLastMove();
		if(move != null){
			changeTurn(Piece.toggleColor(move.moveTo.color));

			if(move.removePiece != null){
				setChanged();
				notifyObservers(new Move(null, move.removePiece, null));
			}
			if(move.moveFrom != null && move.moveTo != null){
				setChanged();
				notifyObservers(new Move(move.moveTo, move.moveFrom, null));
			}else {
				if(move.moveTo != null){
					setChanged();
					notifyObservers(new Move(null, null, move.moveTo));
				}
			}
			
			board.revertLastMove();
		}
		
	}


}
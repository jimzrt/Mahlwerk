package com.mahlwerk.player;

import java.lang.Thread.State;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.mahlwerk.base.Board;
import com.mahlwerk.base.Game;
import com.mahlwerk.base.Game.Gamephase;
import com.mahlwerk.base.Game.Gamestate;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece;
import com.mahlwerk.base.Piece.PieceColor;

public class AiPlayerRandomConsole implements IPlayerHandler {

	Game game;
	PieceColor color;

	Gamestate turnState;

	Gamephase millPhase;

	private Board board;
	private PieceColor otherColor;
	private Gamestate myTurn;
	private Gamestate otherTurn;
	private Gamephase myMill;
	private Gamephase otherMill;


	@Override
	public void startPlayer() {

	}

	@Override
	public void setGame(Game game) {

		this.game = game;
		this.board = game.board;
	}

	@Override
	public void continueTimer() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopTimer() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColor(PieceColor color) {

		this.color = color;
		if (color == PieceColor.BLACK) {
			turnState = Gamestate.PlayerBlackTurn;
			millPhase = Gamephase.PlayerBlackMill;
		} else {
			turnState = Gamestate.PlayerWhiteTurn;
			millPhase = Gamephase.PlayerWhiteMill;
		}

		this.otherColor = color == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
		this.myTurn = color == PieceColor.BLACK ? Gamestate.PlayerBlackTurn : Gamestate.PlayerWhiteTurn;
		this.otherTurn = myTurn == Gamestate.PlayerBlackTurn ? Gamestate.PlayerWhiteTurn : Gamestate.PlayerBlackTurn;
		this.myMill = myTurn == Gamestate.PlayerBlackTurn ? Gamephase.PlayerBlackMill : Gamephase.PlayerWhiteMill;
		this.otherMill = myMill == Gamephase.PlayerBlackMill ? Gamephase.PlayerWhiteMill : Gamephase.PlayerBlackMill;

	}

	@Override
	public PieceColor getColor() {

		return color;
	}

	@Override
	public void run() {
		System.out.println("superior outstanding artificial intelligence started");
		// game.addObserver(this);
		//
		//
		// try {
		// Thread.sleep(500);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//


		//	makeValidMove();
		

	}

	private synchronized void makeValidMove() {

		if (game.gameState == turnState) {
			if (game.gamePhase == Gamephase.SetStones) {

				List<Piece> emptyPieces = game.board.getEmpty();
				Piece newPiece = new Piece(
						emptyPieces.get((int) Math.round((Math.random() * (emptyPieces.size() - 1)))));
				newPiece.color = this.getColor();

				game.onMoveMake(this, new Move(null, newPiece, null));// (this,
																		// emptyPieces.get((int)
																		// Math.round((Math.random()
																		// *
																		// (emptyPieces.size()-1)))));
			} else if (game.gamePhase == Gamephase.MoveStones || game.gamePhase == Gamephase.Endgame) {

				List<Piece> colorPieces = game.board.getPiecesByColor(color);
				Piece newPiece = null;
				Piece pieceFrom = null;
				while (newPiece == null) {

					pieceFrom = colorPieces.get((int) Math.round((Math.random() * (colorPieces.size() - 1))));
					List<Piece> possibleMoves = null;
					if (game.gamePhase == Gamephase.Endgame && game.board.getRemainingPieces(getColor()) < 4) {
						possibleMoves = game.board.getEmpty();
					} else {
						possibleMoves = game.board.getAdjacentEmtpy(pieceFrom);

					}
					if (!possibleMoves.isEmpty()) {
						newPiece = possibleMoves.get((int) Math.round((Math.random() * (possibleMoves.size() - 1))));
					}
				}

				newPiece.color = this.getColor();
				game.onMoveMake(this, new Move(pieceFrom, newPiece, null));

			} else if (game.gamePhase == millPhase) {

				List<Piece> otherColorPieces = game.board
						.getPiecesByColor(color == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK);
				Piece piece = null;
				Piece pieceFrom = null;
				while (piece == null) {

					pieceFrom = otherColorPieces.get((int) Math.round((Math.random() * (otherColorPieces.size() - 1))));
					if (board.isRemovable(pieceFrom))
						piece = pieceFrom;

				}

				game.onMoveMake(this, new Move(null, null, pieceFrom));// (this,
																		// pieceFrom);

			}
		}

		// System.out.println("Going to sleep now...");

	}

	@Override
	public void makeMove(Move move) {

	}

	public void onGameStateChange(Gamestate gamestate) {
		// TODO Auto-generated method stub
		System.out.println("RandomConsole - gamestate changed!");
		// if(gamestate == turnState){
		makeValidMove();
		// }
	}

	public void onGamePhaseChange(Gamephase gamephase) {
		// TODO Auto-generated method stub
		System.out.println("RandomConsole - gamephase changed!");
		makeValidMove();

	}

	ExecutorService pool = Executors.newSingleThreadExecutor();

	private Runnable r = new Runnable() {
        public void run() {
       	 try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            makeValidMove();
        }
    };

    Future task;
	@Override
	public synchronized void update(Observable o, Object arg) {
		
		if(arg instanceof Move){
			
		//	if(task != null)
			//	task.cancel(true);
	}

		//     thread = new Thread(r);
		if(!pool.isShutdown())
			    task= pool.submit(r);

			

		// TODO Auto-generated method stub
	//	if(arg  instanceof Gamephase){
	//		onGamePhaseChange((Gamephase) arg);
	//	} else if(arg instanceof Gamestate){
	//		 onGameStateChange((Gamestate) arg);
	//	}
		
	}

	@Override
	public void close() {
		pool.shutdown();
		// TODO Auto-generated method stub
		
	}

}

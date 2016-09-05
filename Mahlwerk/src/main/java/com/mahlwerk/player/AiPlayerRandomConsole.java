package com.mahlwerk.player;

import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.mahlwerk.base.Game;
import com.mahlwerk.base.Game.Gamephase;
import com.mahlwerk.base.Game.Gamestate;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece;
import com.mahlwerk.base.Piece.PieceColor;

/**
 * Simple Player that uses random (but valid) moves
 * @author James Tophoven
 *
 */
public class AiPlayerRandomConsole implements IPlayerHandler {

	Game game;
	PieceColor color;
	Gamestate turnState;
	Gamephase millPhase;

	@Override
	public void setGame(Game game) {

		this.game = game;
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

	}

	@Override
	public PieceColor getColor() {

		return color;
	}

	@Override
	public void run() {
		System.out.println("superior outstanding artificial intelligence started");
	}

	private synchronized void makeValidMove() {

		if (game.gameState == turnState) {
			if (game.gamePhase == Gamephase.SetStones) {
				List<Piece> emptyPieces = game.board.getEmpty();
				Piece newPiece = new Piece(
						emptyPieces.get((int) Math.round((Math.random() * (emptyPieces.size() - 1)))));
				newPiece.color = this.getColor();

				game.onMoveMake(this, new Move(null, newPiece, null));

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
					if (game.board.isRemovable(pieceFrom))
						piece = pieceFrom;

				}

				game.onMoveMake(this, new Move(null, null, pieceFrom));

			}
		}

	}


	ExecutorService pool = Executors.newSingleThreadExecutor();

	private Runnable r = new Runnable() {
		@Override
		public void run() {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			makeValidMove();
		}
	};

	@Override
	public synchronized void update(Observable o, Object arg) {



			if (!pool.isShutdown()){
				pool.submit(r);

			

		}

	}

	@Override
	public void close() {
		pool.shutdown();

	}

	@Override
	public void revertLastMove() {

	}

}

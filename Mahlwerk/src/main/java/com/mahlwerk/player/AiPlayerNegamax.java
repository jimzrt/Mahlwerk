package com.mahlwerk.player;

import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.mahlwerk.ai.AlphaBetaPruning;
import com.mahlwerk.base.Game;
import com.mahlwerk.base.Game.Gamephase;
import com.mahlwerk.base.Game.Gamestate;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece.PieceColor;

public class AiPlayerNegamax implements IPlayerHandler {

	private PieceColor color;
	private int config = 0;
	private Game game;
	private Gamestate myTurn;

	ExecutorService pool = Executors.newSingleThreadExecutor();

	private Runnable r = new Runnable() {
		public void run() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			makeValidMove();
		}
	};

	AlphaBetaPruning solver;

	public AiPlayerNegamax(int i) {
		config = i;
	}

	@Override
	public void close() {
		pool.shutdown();
	}

	@Override
	public void continueTimer() {

	}

	@Override
	public PieceColor getColor() {

		return color;
	}

	@Override
	public void makeMove(Move move) {
		solver.makeMove(move);

	}

	private synchronized void makeValidMove() {

		if (game.gameState == myTurn) {

			if (game.gamePhase == Gamephase.SetStones) {
				// setting stones
				Move bestMove = solver.getBestSetNegaMax();
				if (game.gameState == myTurn) {
					game.onMoveMake(this, bestMove);

				}

			} else if (game.gamePhase == Gamephase.MoveStones || game.gamePhase == Gamephase.Endgame) {

				Move bestMove = solver.getSuperMoveNegamax();
				if (game.gameState == myTurn) {

					game.onMoveMake(this, bestMove);
				}

			}
		}

	}

	@Override
	public void run() {
		System.out.println("superior outstanding artificial intelligence started");

		game.addObserver(this);

		solver = new AlphaBetaPruning(game, this.color, 15, config);

	}

	@Override
	public void setColor(PieceColor color) {

		this.color = color;
		this.myTurn = color == PieceColor.BLACK ? Gamestate.PlayerBlackTurn : Gamestate.PlayerWhiteTurn;

	}

	@Override
	public void setGame(Game game) {

		this.game = game;
	}

	@Override
	public void startPlayer() {

	}

	@Override
	public void stopTimer() {

	}

	@Override
	public void update(Observable o, Object arg) {

		if (arg instanceof Move) {

			solver.terminate = true;
			solver.makeMove((Move) arg);
			solver.terminate = false;
			return;
		}

		if (!pool.isShutdown())
			pool.submit(r);

	}

}

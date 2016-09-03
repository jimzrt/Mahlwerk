package com.mahlwerk.ai;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mahlwerk.base.Board;
import com.mahlwerk.base.Game;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece;
import com.mahlwerk.base.Piece.PieceColor;

public class AlphaBetaPruning {

	private static int INFINITY = 99999;
	private Board board = new Board();
	private PieceColor color;
	int counter = 0;
	private int maxDepth;
	private long maxTime;

	private PieceColor otherColor;

	public boolean terminate = false;

	public AlphaBetaPruning(Game game, PieceColor color, int maxDepth, long maxTime) {
		this.color = color;
		this.otherColor = color == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
		this.maxTime = maxTime;
		this.maxDepth = maxDepth;
	}

	private int evalBlockedPieces(PieceColor color) {
		return board.getBlockedCount(Piece.toggleColor(color)) - board.getBlockedCount(color);
	}

	private synchronized int evalClosedMorris(PieceColor color) {
		Move move = board.getLastMove();
		if (move == null)
			return 0;

		if (move.removePiece == null)
			return 0;
		if (move.removePiece != null && move.removePiece.color == Piece.toggleColor(color))
			return 1;
		else if (move.removePiece != null && move.removePiece.color == color)
			return -1;
		else
			return 0;
	}

	private int evalDoubleMillCount(PieceColor color) {
		return board.doubleMillCount(color) - board.doubleMillCount(Piece.toggleColor(color));
	}

	private int evalNumberOfMorrises(PieceColor color) {
		if (color == PieceColor.WHITE)
			return board.playerWhiteMillCount - board.playerBlackMillCount;
		else
			return board.playerBlackMillCount - board.playerWhiteMillCount;

	}

	private int evalNumberOfPieces(PieceColor color) {
		return board.getRemainingPieces(color) - board.getRemainingPieces(Piece.toggleColor(color));
	}

	private int evalThreePieceConfiguration(PieceColor color) {
		return board.threePieceConfigurationCount(color) - board.threePieceConfigurationCount(Piece.toggleColor(color));
	}

	private int evalTwoPieceConfiguration(PieceColor color) {
		return board.twoPieceConfigurationCount(color) - board.twoPieceConfigurationCount(Piece.toggleColor(color));
	}

	private int evaluateBoard(PieceColor color) {

		if (board.getPiecesSetCount(color) < Game.initialPieces) {
			return 14 * evalClosedMorris(color) + 37 * evalNumberOfMorrises(color) + 4 * evalBlockedPieces(color)
					+ 14 * evalNumberOfPieces(color) + 20 * evalTwoPieceConfiguration(color)
					+ 2 * evalThreePieceConfiguration(color);

		} else {
			if (board.getRemainingPieces(color) > 3) {
				return 16 * evalClosedMorris(color) + 43 * evalNumberOfMorrises(color) + 11 * evalBlockedPieces(color)
						+ 8 * evalNumberOfPieces(color) + 7 * evalTwoPieceConfiguration(color)
						+ 42 * evalZwillMillCount(color) + 1086 * evalWin(color);

			} else {
				return 16 * evalClosedMorris(color) + 43 * evalNumberOfMorrises(color) + 11 * evalBlockedPieces(color)
						+ 8 * evalNumberOfPieces(color) + 7 * evalTwoPieceConfiguration(color)
						+ 42 * evalZwillMillCount(color) + 1086 * evalWin(color);

			}
		}

	}

	private int evalWin(PieceColor color) {

		if (!board.canMove(color, false))
			return -1;

		if (!board.canMove(Piece.toggleColor(color), false))
			return 1;

		if (board.endReached() && board.getRemainingPieces(color) > 2)
			return 1;
		if (board.endReached() && board.getRemainingPieces(color) <= 2)
			return -1;

		return 0;

	}

	private int evalZwillMillCount(PieceColor color) {
		return board.zwickMillCount(color) - board.zwickMillCount(Piece.toggleColor(color));
	}

	public void executeMove(Move move) {
		board.makeMove(move);
	}

	public Move getBestSetNegaMax() {

		long currentTime = System.currentTimeMillis();
		int[] minMax = iterativeDeepeningNega();
		System.out.println("took " + (System.currentTimeMillis() - currentTime) + " ms");
		System.out.format(
				" AI Nega - Number of Morris: %d, blockedPieces: %d, numberOfPieces: %d, twoPieces: %d, threePieces: %d, doubleMill: %d, zwickmill: %d, win: %d",
				evalNumberOfMorrises(color), evalBlockedPieces(color), evalNumberOfPieces(color),
				evalTwoPieceConfiguration(color), evalThreePieceConfiguration(color), evalDoubleMillCount(color),
				evalZwillMillCount(color), evalWin(color));

		System.out.println();
		System.out.println("AI Score: " + (evaluateBoard(color)));

		Piece newPiece = new Piece(color, minMax[3], minMax[4]);
		Piece removePiece = null;
		if (minMax[5] != -1)
			removePiece = new Piece(Piece.toggleColor(color), minMax[5], minMax[6]);
		return new Move(null, newPiece, removePiece);
	}

	public Set<Move> getPossibleMoves(PieceColor color) {

		Set<Move> possibleMoves = new HashSet<Move>();
		int stonesSet = color == PieceColor.BLACK ? board.blackStonesSet : board.whiteStonesSet;

		// set phase
		if (stonesSet < Game.initialPieces) {
			List<Piece> moves = board.getEmpty();
			for (Piece piece : moves) {
				Piece move = new Piece(color, piece.x, piece.y);
				if (board.checkMill(move)) {

					List<Piece> opponentPieces = board.getPiecesByColor(Piece.toggleColor(color));
					for (Piece opponentPiece : opponentPieces) {
						if (!board.isRemovable(opponentPiece))
							continue;

						possibleMoves.add(new Move(null, move, opponentPiece));
					}
				} else {
					possibleMoves.add(new Move(null, move, null));
				}
			}
		} else {

			// move phase and endphase
			List<Piece> moves = board.getPiecesByColor(color);
			for (Piece piece : moves) {

				List<Piece> adjPieces = null;
				if (board.getRemainingPieces(color) > 3) {
					adjPieces = board.getAdjacentEmtpy(piece);

				} else {
					adjPieces = board.getEmpty();
				}
				for (Piece adjPiece : adjPieces) {

					board.setPieceColor(piece, PieceColor.EMPTY);// .removeMove(piece);
					Piece newTo = new Piece(color, adjPiece.x, adjPiece.y);

					if (board.checkMill(newTo)) {
						List<Piece> opponentPieces = board.getPiecesByColor(Piece.toggleColor(color));
						for (Piece opponentPiece : opponentPieces) {
							if (!board.isRemovable(opponentPiece))
								continue;

							possibleMoves.add(new Move(piece, newTo, opponentPiece));
						}
					} else {
						possibleMoves.add(new Move(piece, newTo, null));
					}

					board.setPieceColor(piece, color);// .removeMove(piece);

				}
			}

		}

		return possibleMoves;

	}

	public Move getSuperMoveNegamax() {

		long currentTime = System.currentTimeMillis();
		int[] minMax = iterativeDeepeningNega();
		System.out.println("took " + (System.currentTimeMillis() - currentTime) + " ms");

		System.out.format(
				" AI Nega - Number of Morris: %d, blockedPieces: %d, numberOfPieces: %d, twoPieces: %d, threePieces: %d, doubleMill: %d, zwickmill: %d, win: %d",
				evalNumberOfMorrises(color), evalBlockedPieces(color), evalNumberOfPieces(color),
				evalTwoPieceConfiguration(color), evalThreePieceConfiguration(color), evalDoubleMillCount(color),
				evalZwillMillCount(color), evalWin(color));

		System.out.println();
		System.out.println("AI Score: " + (evaluateBoard(color)));
		Piece newPieceFrom = new Piece(color, minMax[1], minMax[2]);
		Piece newPieceTo = new Piece(color, minMax[3], minMax[4]);

		Piece removePiece = null;
		if (minMax[5] != -1)
			removePiece = new Piece(Piece.toggleColor(color), minMax[5], minMax[6]);

		System.out.println(board.getPiecesSetCount(color) + " in: " + Game.initialPieces);
		return new Move(newPieceFrom, newPieceTo, removePiece);

	}

	public synchronized int[] iterativeDeepeningNega() {

		int[] minMax = null;
		long currentTime = System.currentTimeMillis();
		int[] bestMove = null;

		int bestMoveX = -1;
		int bestMoveY = -1;
		int bestMoveX2 = -1;
		int bestMoveY2 = -1;
		int bestRemoveX = -1;
		int bestRemoveY = -1;

		for (int i = 1; i < maxDepth; i++) {
			int bestScore = -INFINITY;

			Iterator<Move> possibleMoves = getPossibleMoves(this.color).iterator();
			while (possibleMoves.hasNext()) {
				Move move = possibleMoves.next();

				executeMove(move);

				int score = -negamaxSet(i, -INFINITY, INFINITY, this.otherColor,
						this.maxTime - (System.currentTimeMillis() - currentTime));

				if (this.maxTime - (System.currentTimeMillis() - currentTime) <= 0 || terminate) {
					revertMove(move);
					// return bestMove;
					break;
				}

				if (score > bestScore) {
					bestScore = score;
					if (move.moveFrom != null) {
						bestMoveX = move.moveFrom.x;
						bestMoveY = move.moveFrom.y;
					} else {
						bestMoveX = -1;
						bestMoveY = -1;
					}
					if (move.moveTo != null) {
						bestMoveX2 = move.moveTo.x;
						bestMoveY2 = move.moveTo.y;
					} else {
						bestMoveX2 = -1;
						bestMoveX2 = -1;
					}
					if (move.removePiece != null) {
						bestRemoveX = move.removePiece.x;
						bestRemoveY = move.removePiece.y;
					} else {
						bestRemoveX = -1;
						bestRemoveY = -1;
					}

				}

				revertMove(move);

			}

			minMax = new int[] { bestScore, bestMoveX, bestMoveY, bestMoveX2, bestMoveY2, bestRemoveX, bestRemoveY };

			if (this.maxTime - (System.currentTimeMillis() - currentTime) <= 0) {

				System.out.println("Zeit abgelaufen");
				break;
			}
			bestMove = minMax;
			System.out.println("depth nega: " + i + ", score: " + bestMove[0]);
			if (bestMove[0] > 1000)
				break;
		}
		return bestMove;

	}

	public synchronized void makeMove(Move move) {
		board.makeMove(move);

	}

	public int negamaxSet(int depth, int alpha, int beta, PieceColor color, long timeLeft) {

		if (timeLeft <= 0 || depth == 0 || board.endReached() || !board.canMove(color, false)
				|| !board.canMove(Piece.toggleColor(color), false) || terminate) {

			return (evaluateBoard(color));

		}
		long currentTime = System.currentTimeMillis();

		int bestValue = -INFINITY;
		Set<Move> possMoves = getPossibleMoves(color);
		Iterator<Move> moves = possMoves.iterator();
		while (moves.hasNext()) {
			Move move = moves.next();

			executeMove(move);

			int value = -negamaxSet(depth - 1, -beta, -alpha, Piece.toggleColor(color),
					timeLeft - (System.currentTimeMillis() - currentTime));
			if (timeLeft - (System.currentTimeMillis() - currentTime) <= 0) {
				revertMove(move);
				moves = null;
				return bestValue;
			}
			if (value > bestValue) {
				bestValue = value;

			}
			if (value > alpha) {
				alpha = value;
			}

			revertMove(move);

			if (alpha >= beta)
				break;

		}
		moves = null;

		return bestValue;

	}

	public void revertMove(Move move) {

		board.revertMove(move);
	}

}

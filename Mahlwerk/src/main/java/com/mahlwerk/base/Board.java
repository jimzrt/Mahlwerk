package com.mahlwerk.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import com.mahlwerk.base.Piece.PieceColor;

public class Board {
	public static final int SIZE = 7;
	int[][] adjacencyMap = new int[SIZE * SIZE][];
	public int blackStonesRemoved = 0;

	public int blackStonesSet = 0;

	public long hashId;
	History history = new History();

	public boolean lastMoveMill = false;
	int[][][] millMap = new int[SIZE * SIZE][][];
	public int moveCounter = 0;
	private int[] pieces = new int[SIZE * SIZE];

	public int playerBlackMillCount = 0;

	public int playerWhiteMillCount = 0;
	public boolean[] validPositions = new boolean[] { 
			true, false, false, true, false, false, true, 
			false, true, false, true, false, true, false,
			false, false, true, true, true, false, false, 
			true, true, true, false, true, true, true, 
			false, false, true, true, true, false, false,
			false, true, false, true, false, true, false, 
			true, false, false, true, false, false, true

	};

	public int whiteStonesRemoved = 0;

	public int whiteStonesSet = 0;

	//long[][] zobrist = new long[SIZE * SIZE][3];

	public Board() {

//		for (int square = 0; square < zobrist.length; square++)
//			for (int side = 0; side < zobrist[square].length; side++)
//				zobrist[square][side] = (long) (Math.random() * Long.MAX_VALUE);

		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (validPositions[i + j * SIZE])
					pieces[i + j * SIZE] = 0;
				else
					pieces[i + j * SIZE] = -1;

			}
		}

		buildAdjacencyMap();

		buildMillMap();

	//	hashId = computeKey();

	}

	private void buildAdjacencyMap() {

		adjacencyMap[0] = new int[] { 3, 21 };
		adjacencyMap[3] = new int[] { 0, 6, 10 };
		adjacencyMap[6] = new int[] { 3, 27 };

		adjacencyMap[8] = new int[] { 10, 22 };
		adjacencyMap[10] = new int[] { 3, 8, 12, 17 };
		adjacencyMap[12] = new int[] { 10, 26 };

		adjacencyMap[16] = new int[] { 17, 23 };
		adjacencyMap[17] = new int[] { 16, 10, 18 };
		adjacencyMap[18] = new int[] { 17, 25 };

		adjacencyMap[21] = new int[] { 0, 22, 42 };
		adjacencyMap[22] = new int[] { 21, 8, 23, 36 };
		adjacencyMap[23] = new int[] { 16, 22, 30 };
		adjacencyMap[25] = new int[] { 18, 26, 32 };
		adjacencyMap[26] = new int[] { 25, 12, 27, 40 };
		adjacencyMap[27] = new int[] { 6, 26, 48 };

		adjacencyMap[30] = new int[] { 23, 31 };
		adjacencyMap[31] = new int[] { 30, 38, 32 };
		adjacencyMap[32] = new int[] { 25, 31 };

		adjacencyMap[36] = new int[] { 38, 22 };
		adjacencyMap[38] = new int[] { 36, 31, 40, 45 };
		adjacencyMap[40] = new int[] { 26, 38 };

		adjacencyMap[42] = new int[] { 45, 21 };
		adjacencyMap[45] = new int[] { 42, 38, 48 };
		adjacencyMap[48] = new int[] { 45, 27 };

	}

	private void buildMillMap() {

		millMap[0] = new int[][] { new int[] { 3, 6 }, new int[] { 21, 42 } };
		millMap[3] = new int[][] { new int[] { 0, 6 }, new int[] { 10, 17 } };
		millMap[6] = new int[][] { new int[] { 0, 3 }, new int[] { 27, 48 } };

		millMap[8] = new int[][] { new int[] { 10, 12 }, new int[] { 22, 36 } };
		millMap[10] = new int[][] { new int[] { 8, 12 }, new int[] { 3, 17 } };
		millMap[12] = new int[][] { new int[] { 8, 10 }, new int[] { 26, 40 } };
		millMap[16] = new int[][] { new int[] { 17, 18 }, new int[] { 23, 30 } };
		millMap[17] = new int[][] { new int[] { 16, 18 }, new int[] { 3, 10 } };
		millMap[18] = new int[][] { new int[] { 16, 17 }, new int[] { 25, 32 } };
		millMap[21] = new int[][] { new int[] { 22, 23 }, new int[] { 0, 42 } };
		millMap[22] = new int[][] { new int[] { 21, 23 }, new int[] { 8, 36 } };
		millMap[23] = new int[][] { new int[] { 21, 22 }, new int[] { 16, 30 } };
		millMap[25] = new int[][] { new int[] { 26, 27 }, new int[] { 18, 32 } };
		millMap[26] = new int[][] { new int[] { 25, 27 }, new int[] { 12, 40 } };
		millMap[27] = new int[][] { new int[] { 25, 26 }, new int[] { 6, 48 } };
		millMap[30] = new int[][] { new int[] { 31, 32 }, new int[] { 16, 23 } };
		millMap[31] = new int[][] { new int[] { 30, 32 }, new int[] { 38, 45 } };
		millMap[32] = new int[][] { new int[] { 30, 31 }, new int[] { 18, 25 } };
		millMap[36] = new int[][] { new int[] { 38, 40 }, new int[] { 8, 22 } };
		millMap[38] = new int[][] { new int[] { 36, 40 }, new int[] { 31, 45 } };
		millMap[40] = new int[][] { new int[] { 36, 38 }, new int[] { 12, 26 } };

		millMap[42] = new int[][] { new int[] { 45, 48 }, new int[] { 0, 21 } };
		millMap[45] = new int[][] { new int[] { 42, 48 }, new int[] { 31, 38 } };
		millMap[48] = new int[][] { new int[] { 42, 45 }, new int[] { 6, 27 } };

	}

	public synchronized boolean canMove(PieceColor color, boolean anywhere) {
		if (whiteStonesSet < Game.initialPieces || blackStonesSet < Game.initialPieces) {
			return true;
		}

		if (anywhere) {
			if (getEmpty().isEmpty())
				return false;
			else
				return true;
		} else {
			List<Piece> piecesByColor = getPiecesByColor(color);
			for (Piece piece : piecesByColor) {

				if (getAdjacentEmtpyCount(piece) != 0) {
					return true;
				}

			}
		}
		return false;
	}

	public synchronized boolean checkMill(Piece piece) {

		if (piece == null || piece.color == PieceColor.EMPTY)
			return false;

		int[][] indexes = millMap[piece.x + piece.y * SIZE];

		for (int[] i : indexes) {

			if (getPieceColor(i[0]) == piece.color && getPieceColor(i[1]) == piece.color)
				return true;

		}

		return false;
	}

	public synchronized boolean checkMill(Piece piece, PieceColor color) {

		if (piece == null || color == PieceColor.EMPTY)
			return false;

		int[][] indexes = millMap[piece.x + piece.y * SIZE];

		for (int[] i : indexes) {

			if (getPieceColor(i[0]) == color && getPieceColor(i[1]) == color)
				return true;

		}

		return false;
	}

	private int colorToValue(PieceColor color) {
		switch (color) {
		case EMPTY:
			return 0;
		case WHITE:
			return 1;
		case BLACK:
			return 2;
		}
		return -1;
	}

//	long computeKey() {
//		long hashKey = 0;
//		for (int square = 0; square < pieces.length; square++) {
//			if (validPositions[square]) {
//				int side = pieces[square];
//			//	hashKey ^= zobrist[square][side];
//			}
//
//		}
//		return hashKey;
//	}

	public synchronized void decrementMillCount(PieceColor color) {
		switch (color) {
		case WHITE:
			playerWhiteMillCount--;
			break;
		case BLACK:
			playerBlackMillCount--;
			break;
		case EMPTY:
			System.out.println("ERROR - cannot decrement emtpy mill count!");
		default:
			break;
		}
	}

	public synchronized int doubleMillCount(PieceColor color) {
		int millCount = 0;
		if (color == PieceColor.BLACK)
			millCount = playerBlackMillCount;
		else if (color == PieceColor.WHITE)
			millCount = playerWhiteMillCount;

		if (millCount == 0)
			return 0;
		int count = 0;

		Set<Integer> millPieces = new HashSet<Integer>();

		List<Piece> piecesColor = getPiecesByColor(color);
		for (Piece newPiece : piecesColor) {

			if (checkMill(newPiece)) {
				int[][] indexes = millMap[newPiece.x + newPiece.y * SIZE];

				for (int[] i : indexes) {

					if (getPieceColor(i[0]) == newPiece.color && getPieceColor(i[1]) == newPiece.color) {
						millPieces.add(i[0]);
						millPieces.add(i[1]);
						millPieces.add(newPiece.x + newPiece.y * SIZE);
						count++;
					}

				}

			}

		}
		return count - millPieces.size();

	}

	public synchronized boolean endReached() {
		if (whiteStonesSet < Game.initialPieces || blackStonesSet < Game.initialPieces) {
			return false;
		}

		if (whiteStonesSet - whiteStonesRemoved < 3 || blackStonesSet - blackStonesRemoved < 3) {
			return true;
		}

		return false;
	}

	public synchronized List<Piece> getAdjacentEmtpy(Piece piece) {

		int[] indexes = adjacencyMap[piece.x + piece.y * SIZE];
		List<Piece> adjPieces = new ArrayList<Piece>();

		for (int i : indexes) {
			if (pieces[i] == 0)
				adjPieces.add(new Piece(PieceColor.EMPTY, i));
		}

		return adjPieces;

	}

	public synchronized int getAdjacentEmtpyCount(Piece piece) {

		int count = 0;

		int[] indexes = adjacencyMap[piece.x + piece.y * SIZE];
		for (int i : indexes) {
			if (pieces[i] == 0)
				count++;
		}

		return count;

	}

	public int[] getArrayIndex(int[] arr, int value) {

		int[] indices = IntStream.range(0, arr.length).filter(i -> arr[i] == value).toArray();

		return indices;

	}

	public synchronized int getBlockedCount(PieceColor color) {
		int count = 0;
		List<Piece> piecesByColor = getPiecesByColor(color);
		for (Piece piece : piecesByColor) {
			if (getAdjacentEmtpyCount(piece) == 0)
				count++;
		}
		return count;
	}

	public synchronized List<Piece> getEmpty() {

		/*
		 * List<Piece> emptyPieces = new ArrayList<Piece>(); for(Piece piece :
		 * pieces){ if(piece.color == PieceColor.EMPTY &&
		 * MoveValidator.validPositions[piece.x + piece.y * SIZE])
		 * emptyPieces.add(piece); } return emptyPieces;
		 */
		int[] index = getArrayIndex(pieces, 0);
		List<Piece> emptyPieces = new ArrayList<Piece>();
		for (int i : index) {
			emptyPieces.add(new Piece(PieceColor.EMPTY, i));
		}

		return emptyPieces;

	}

	public synchronized Move getLastMove() {
		return history.getLastMove();
	}

	public synchronized Piece getPiece(int col, int row) {
		return new Piece(getPieceColor(col, row), col, row);
	}

	public PieceColor getPieceColor(int index) {
		switch (pieces[index]) {
		case 0:
			return PieceColor.EMPTY;
		case 1:
			return PieceColor.WHITE;
		case 2:
			return PieceColor.BLACK;
		}
		return null;
	}

	public PieceColor getPieceColor(int x, int y) {
		switch (pieces[x + y * SIZE]) {
		case 0:
			return PieceColor.EMPTY;
		case 1:
			return PieceColor.WHITE;
		case 2:
			return PieceColor.BLACK;
		}
		return null;
	}

	public synchronized List<Piece> getPiecesByColor(PieceColor color) {

		int[] index = getArrayIndex(pieces, colorToValue(color));
		List<Piece> piecesByColor = new ArrayList<Piece>();
		for (int i : index) {
			piecesByColor.add(new Piece(color, i));
		}

		return piecesByColor;

	}

	public synchronized int getPiecesByColorCount(PieceColor color) {
		int[] index = getArrayIndex(pieces, colorToValue(color));
		return index.length;
	}

	public synchronized int getPiecesSetCount(PieceColor color) {
		if (color == PieceColor.BLACK)
			return blackStonesSet;
		else if (color == PieceColor.WHITE)
			return whiteStonesSet;
		else
			return -1;
	}

	public synchronized int getRemainingPieces(PieceColor color) {
		if (color == PieceColor.WHITE) {
			return (whiteStonesSet - whiteStonesRemoved);
		} else {
			return (blackStonesSet - blackStonesRemoved);
		}
	}

	public synchronized void incrementMillCount(PieceColor color) {
		switch (color) {
		case WHITE:
			playerWhiteMillCount++;
			break;
		case BLACK:
			playerBlackMillCount++;
			break;
		case EMPTY:
			System.out.println("ERROR - cannot increment emtpy mill count!");
		default:
			break;
		}
	}

	public synchronized boolean isRemovable(Piece move) {
		if (move.color == PieceColor.EMPTY) {
			System.out.println("is empty, not removable");
			return false;
		}
		List<Piece> allPieces = getPiecesByColor(move.color);
		boolean allMill = true;
		for (Piece piece : allPieces) {
			if (!checkMill(piece)) {
				allMill = false;
				break;
			}
		}
		if (allMill)
			return true;
		return !(checkMill(move));

	}

	public synchronized void makeMove(Move move) {
		// System.out.println(move);
		history.add(move);

		lastMoveMill = false;

		if (move.moveFrom == null && move.moveTo != null) {
			if (move.moveTo.color == PieceColor.BLACK)
				blackStonesSet++;
			else if (move.moveTo.color == PieceColor.WHITE)
				whiteStonesSet++;

		}

		if (move.moveFrom != null) {
			if (checkMill(move.moveFrom)) {
				if (move.moveFrom.color == PieceColor.BLACK)
					playerBlackMillCount--;
				else if (move.moveFrom.color == PieceColor.WHITE)
					playerWhiteMillCount--;
			}

			setPieceColor(move.moveFrom, PieceColor.EMPTY);
			// updateKey(move.moveTo.x, move.moveTo.y, PieceColor.EMPTY);
		}

		if (move.moveTo != null) {
			setPieceColor(move.moveTo, move.moveTo.color);
			// updateKey(move.moveTo.x, move.moveTo.y, move.moveTo.color);

			if (checkMill(move.moveTo)) {
				lastMoveMill = true;
				if (move.moveTo.color == PieceColor.BLACK)
					playerBlackMillCount++;
				else if (move.moveTo.color == PieceColor.WHITE)
					playerWhiteMillCount++;
			}

			moveCounter++;

		}

		if (move.removePiece != null) {

			if (checkMill(move.removePiece)) {
				if (move.removePiece.color == PieceColor.BLACK)
					playerBlackMillCount--;
				else if (move.removePiece.color == PieceColor.WHITE)
					playerWhiteMillCount--;
			}

			if (move.removePiece.color == PieceColor.BLACK)
				blackStonesRemoved++;
			else if (move.removePiece.color == PieceColor.WHITE)
				whiteStonesRemoved++;

			setPieceColor(move.removePiece, PieceColor.EMPTY);
			// updateKey(move.removePiece.x, move.removePiece.y,
			// PieceColor.EMPTY);

		}

	}

	public void print() {
		for (int i = 0; i < SIZE; i++) {

			String number = "\n" + (SIZE - i) + " |";

			String[] row = new String[SIZE];
			for (int j = 0; j < SIZE; j++) {

				if (!validPositions[j + i * SIZE])
					row[j] = "";
				else if (pieces[j + i * SIZE] == 1) {
					row[j] = "| W |";
				} else if (pieces[j + i * SIZE] == 2) {
					row[j] = "| B |";
				} else {
					row[j] = "|   |";
				}
			}
			System.out.format("%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s", "\n  |", row[0].equals("") ? "     " : "+---+",
					row[1].equals("") ? "     " : "+---+", row[2].equals("") ? "     " : "+---+",
					row[3].equals("") ? "     " : "+---+", row[4].equals("") ? "     " : "+---+",
					row[5].equals("") ? "     " : "+---+", row[6].equals("") ? "     " : "+---+");

			System.out.format("%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s", number, row[0], row[1], row[2], row[3], row[4],
					row[5], row[6]);
			System.out.format("%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s", "\n  |", row[0].equals("") ? "     " : "+---+",
					row[1].equals("") ? "     " : "+---+", row[2].equals("") ? "     " : "+---+",
					row[3].equals("") ? "     " : "+---+", row[4].equals("") ? "     " : "+---+",
					row[5].equals("") ? "     " : "+---+", row[6].equals("") ? "     " : "+---+");
		}

		System.out.println("");
		System.out.println("   ---------------------------------------------------");
		System.out.format("%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s", "   ", " a ", " b ", " c ", " d ", " e ", " f ", " g ");
		System.out.print("\n\n");
		System.out.println(moveCounter);
	}

	public synchronized void revertLastMove() {
		revertMove(history.getLastMove());
	}

	public synchronized void revertMove(Move move) {

		history.remove(move);

		if (move.removePiece != null) {
			setPieceColor(move.removePiece, move.removePiece.color);
			if (checkMill(move.removePiece)) {
				incrementMillCount(move.removePiece.color);
			}

			if (move.removePiece.color == PieceColor.BLACK)
				blackStonesRemoved--;
			else if (move.removePiece.color == PieceColor.WHITE)
				whiteStonesRemoved--;
		}

		if (move.moveFrom == null && move.moveTo != null) {
			moveCounter--;

			if (checkMill(move.moveTo)) {
				decrementMillCount(move.moveTo.color);
			}
			setPieceColor(move.moveTo, PieceColor.EMPTY);

			if (move.moveTo.color == PieceColor.BLACK)
				blackStonesSet--;
			else if (move.moveTo.color == PieceColor.WHITE)
				whiteStonesSet--;

		} else {

			if (move.moveTo != null) {
				moveCounter--;
				if (checkMill(move.moveTo)) {
					decrementMillCount(move.moveTo.color);
				}
				setPieceColor(move.moveTo, PieceColor.EMPTY);

			}

			if (move.moveFrom != null) {
				setPieceColor(move.moveFrom, move.moveFrom.color);
				if (checkMill(move.moveFrom)) {
					incrementMillCount(move.moveTo.color);
				}
			}

		}

	}

	public synchronized void setPieceColor(Piece piece, PieceColor color) {

		switch (color) {
		case EMPTY:
			pieces[piece.x + piece.y * SIZE] = 0;
			return;
		case WHITE:
			pieces[piece.x + piece.y * SIZE] = 1;
			return;
		case BLACK:
			pieces[piece.x + piece.y * SIZE] = 2;
			return;
		}
	}

	public synchronized int threePieceConfigurationCount(PieceColor color) {
		Set<Integer> millPieces = new HashSet<Integer>();
		List<Piece> piecesByColor = getEmpty();
		int count = 0;
		for (Piece piece : piecesByColor) {

			Piece newPiece = new Piece(color, piece.x, piece.y);
			if (checkMill(newPiece)) {
				int[][] indexes = millMap[newPiece.x + newPiece.y * SIZE];

				for (int[] i : indexes) {

					if (getPieceColor(i[0]) == newPiece.color && getPieceColor(i[1]) == newPiece.color) {
						millPieces.add(i[0]);
						millPieces.add(i[1]);
						count += 2;
					}

				}

			}
			// }

		}

		return count - millPieces.size();
	}

	public synchronized int twoPieceConfigurationCount(PieceColor color) {
		Set<Integer> millPieces = new HashSet<Integer>();
		List<Piece> piecesByColor = getPiecesByColor(color);
		for (Piece piece : piecesByColor) {
			List<Piece> adjEmpty = getAdjacentEmtpy(piece);
			for (Piece adjPiece : adjEmpty) {
				if (checkMill(new Piece(color, adjPiece.x, adjPiece.y)))
					millPieces.add(adjPiece.x + adjPiece.y * SIZE);
			}

		}
		return millPieces.size();
	}

//	void updateKey(int index, int color) {
//		hashId = hashId ^ zobrist[index][color];
//	}
//
//	void updateKey(int x, int y, PieceColor color) {
//		hashId = hashId ^ zobrist[x + y * SIZE][colorToValue(color)];
//	}
//
//	void updateKey(int index, PieceColor color) {
//		hashId = hashId ^ zobrist[index][colorToValue(color)];
//	}

	public synchronized int zwickMillCount(PieceColor color) {
		int count = 0;
		List<Piece> piecesColor = getPiecesByColor(color);
		for (Piece newPiece : piecesColor) {
			if (checkMill(newPiece)) {
				List<Piece> adjEmptyPieces = getAdjacentEmtpy(newPiece);
				setPieceColor(newPiece, PieceColor.EMPTY);
				for (Piece adjPiece : adjEmptyPieces) {

					if (checkMill(adjPiece, color))
						count++;

				}
				setPieceColor(newPiece, color);

			}
		}
		return count;
	}
}

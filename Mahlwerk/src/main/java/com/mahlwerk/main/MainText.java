package com.mahlwerk.main;

import java.io.IOException;

import com.mahlwerk.base.Board;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece;
import com.mahlwerk.base.Piece.PieceColor;

public class MainText {

	public static void main(String[] args){
		Board board = new Board();
		board.print();
		Piece piece = new Piece(PieceColor.WHITE, 1, 1);
		Move move = new Move(null, piece, null);
		board.makeMove(move);
		board.makeMove(new Move(null, new Piece(PieceColor.WHITE, 3, 1),null));
		board.makeMove(new Move(null, new Piece(PieceColor.WHITE, 5, 1),null));

		board.print();
		System.out.println(board.lastMoveMill);
		System.out.println(board.whiteStonesSet);
		System.out.println(board.playerWhiteMillCount);
		
		try {
			System.in.read();
			System.out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		board.makeMove(new Move(null, new Piece(PieceColor.WHITE, 5, 3),null));
		
		board.print();
		System.out.println(board.lastMoveMill);
		System.out.println(board.whiteStonesSet);
		System.out.println(board.getPiecesByColor(PieceColor.BLACK));
		
		try {
			System.in.read();
			System.out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		board.makeMove(new Move(new Piece(PieceColor.WHITE, 5, 3), new Piece(PieceColor.WHITE, 6, 3),new Piece(PieceColor.WHITE, 3, 1)));
		
		board.print();
		System.out.println(board.lastMoveMill);
		System.out.println(board.whiteStonesSet);
		System.out.println(board.getPiecesByColor(PieceColor.WHITE));
		try {
			System.in.read();
			System.out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

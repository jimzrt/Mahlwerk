package com.mahlwerk.base;

import java.io.Serializable;

public class Piece implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7546232965396753487L;

	public enum PieceColor{
		EMPTY,BLACK,WHITE
	}
	
	public  PieceColor color;
	public  int x;
	public  int y;
	
	public Piece(PieceColor color, int x, int y){
		this.color = color;
		this.x = x;
		this.y = y;
	}
	
	public Piece(PieceColor color, int index){
		this(color,index%Board.SIZE,index/Board.SIZE);
	}
	
	public Piece(Piece piece){
		this(piece.color,piece.x,piece.y);
	}
	
	public Piece(Piece piece, int index){
		this(piece.color,index%Board.SIZE,index/Board.SIZE);
	}
	
	public Piece(Piece piece, PieceColor color){
		this(color,piece.x,piece.y);
	}
	

	public String toString(){
		return "Move - Color: " + this.color + " - Position: (" + x + ", " + y + ")";
	}
	
	public static PieceColor toggleColor(PieceColor color){
		if(color == PieceColor.BLACK){
			return PieceColor.WHITE;
		} else if( color == PieceColor.WHITE){
			return PieceColor.BLACK;
		} else {
			try {
				throw new Exception("can only toggle black or white!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return color;
	}

	
	
}

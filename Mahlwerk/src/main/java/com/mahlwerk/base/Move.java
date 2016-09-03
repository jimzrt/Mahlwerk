package com.mahlwerk.base;

import java.io.Serializable;

public class Move implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1424220669591134819L;
	public Piece moveFrom;
	public Piece moveTo;
	public Piece removePiece;
	

	public Move(Piece moveFrom, Piece moveTo, Piece removePiece){
		this.moveFrom = moveFrom;
		this.moveTo = moveTo;
		this.removePiece = removePiece;
	}
	
	public Move(Move lastMove) {
		this (lastMove.moveFrom, lastMove.moveTo, lastMove.removePiece);
	}

	public String toString(){
		return ("Move from: " + moveFrom + ", Move to: " + moveTo + ", remove Piece: "  + removePiece);
				 
	}
}

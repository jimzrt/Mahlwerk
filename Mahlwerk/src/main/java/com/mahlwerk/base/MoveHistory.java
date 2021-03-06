package com.mahlwerk.base;

import java.util.ArrayList;
import java.util.List;

/**
 * History of Moves
 * @author James Tophoven
 *
 */
public class MoveHistory {
	
	List<Move> moves = new ArrayList<Move>();

	public synchronized void add(Move move) {
		if(move.moveTo == null && move.removePiece != null){
			moves.get(moves.size()-1).removePiece = move.removePiece;
		}else {
			moves.add(move);

		}
		
	}
	
	public synchronized Move getLastMove(){
		if(moves.isEmpty())
			return null;
		return moves.get(moves.size()-1);
	}
	
	public synchronized void removeLastMove(){
		if(!moves.isEmpty())
			moves.remove(moves.size()-1);
	}

	public synchronized void remove(Move move) {
		moves.remove(move);
		
	}

}

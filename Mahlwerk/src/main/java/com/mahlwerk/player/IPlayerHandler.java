package com.mahlwerk.player;

import java.util.Observer;

import com.mahlwerk.base.Game;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece.PieceColor;

	/**
	 * Interface for all Players and "Watchers"
	 * Will be notified by Game if Move is made or Phase/State is changed
	 * @author James Tophoven
	 *
	 */
	public interface IPlayerHandler extends Runnable, Observer {
		
		public void setGame(Game game);
		public void setColor(PieceColor color);
		public PieceColor getColor();
		public void close();
		public void revertLastMove();

	}


package com.mahlwerk.player;

import java.util.Observer;

import com.mahlwerk.base.Game;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece.PieceColor;

	public interface IPlayerHandler extends Runnable, Observer {
		
		public void startPlayer();
		public void setGame(Game game);
		public void continueTimer();
		public void stopTimer();
		public void makeMove(Move move);
		//public void onGameStateChange(Gamestate gamestate);
		//public void onGamePhaseChange(Gamephase gamephase);
		public void setColor(PieceColor color);
		public PieceColor getColor();
		public void close();

	}


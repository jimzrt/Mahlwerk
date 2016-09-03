package com.mahlwerk.player;

import java.lang.Thread.State;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.mahlwerk.ai.AlphaBetaPruning;
import com.mahlwerk.base.Board;
import com.mahlwerk.base.Game;
import com.mahlwerk.base.Game.Gamephase;
import com.mahlwerk.base.Game.Gamestate;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece.PieceColor;

import javafx.concurrent.Task;




public class AiPlayerNegamax implements IPlayerHandler {

	private Game game;
	private Board board;
	private PieceColor color;
	private PieceColor otherColor;
	private Gamestate myTurn;
	private Gamestate otherTurn;
	private Gamephase myMill;
	private Gamephase otherMill;
	private Object syncObject;
	
	
 

	AlphaBetaPruning solver;
	

	private int config = 0;
	public AiPlayerNegamax(int i) {
		config = i;
	}

	@Override
	public void startPlayer() {

	}

	@Override
	public void setGame(Game game) {

		this.game = game;
		this.board = game.board;
	}

	@Override
	public void continueTimer() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopTimer() {
		// TODO Auto-generated method stub

	}


	@Override
	public void setColor(PieceColor color) {

		this.color = color;
		this.otherColor = color == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
		this.myTurn = color == PieceColor.BLACK ? Gamestate.PlayerBlackTurn : Gamestate.PlayerWhiteTurn; 
		this.otherTurn = myTurn == Gamestate.PlayerBlackTurn ? Gamestate.PlayerWhiteTurn : Gamestate.PlayerBlackTurn;
		this.myMill = myTurn == Gamestate.PlayerBlackTurn ? Gamephase.PlayerBlackMill : Gamephase.PlayerWhiteMill;
		this.otherMill = myMill == Gamephase.PlayerBlackMill ? Gamephase.PlayerWhiteMill : Gamephase.PlayerBlackMill;



	}

	@Override
	public PieceColor getColor() {

		return color;
	}

	@Override
	public void run() {
		System.out.println("superior outstanding artificial intelligence started");

		game.addObserver(this);

		solver = new AlphaBetaPruning(game,this.color,15, config);
	//	Object syncObject = new Object();

	//	makeValidMove();
		

	}

	private synchronized void makeValidMove() {



			if (game.gameState == myTurn) {
				

				
				
				if(game.gamePhase == Gamephase.SetStones){
					//setting stones
						Move bestMove = solver.getBestSetNegaMax();
						if(game.gameState == myTurn){
							game.onMoveMake(this, bestMove);

						}

					

				} else if(game.gamePhase == Gamephase.MoveStones || game.gamePhase == Gamephase.Endgame){
					

					if(board.getRemainingPieces(color)<4){
						// less than 4 stones, jump
						
					//	Entry<Piece,Piece> move = solver.getBestMove().entrySet().iterator().next();
					//	List<Piece> move = solver.getSuperMoveNegamax();
						
						Move bestMove = solver.getSuperMoveNegamax();
						if(game.gameState == myTurn){

						game.onMoveMake(this, bestMove);
						}
						
						
					} else {
						//moving stones
					//	Entry<Piece,Piece> move = solver.getBestMove().entrySet().iterator().next();
					//	List<Piece> move = solver.getSuperMoveNegamax();

						Move bestMove = solver.getSuperMoveNegamax();
						if(game.gameState == myTurn){

						game.onMoveMake(this, bestMove);
						}
//						try {
//							System.in.read();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					}
				}
			}
		
				
			
		

		
	//	solver = null;
	//	System.out.println("Going to sleep now...");

	}

	@Override
	public void makeMove(Move move) {
		// TODO Auto-generated method stub
		solver.makeMove(move);
		
	}
	
	public void onGameStateChange(Gamestate gamestate) {
		// TODO Auto-generated method stub
		System.out.println("Ai Nega - gamestate changed!");
			makeValidMove();
		
	}

	public void onGamePhaseChange(Gamephase gamephase) {
		// TODO Auto-generated method stub
		System.out.println("AiNega - gamephase changed!");

	}
	
	ExecutorService pool = Executors.newSingleThreadExecutor();
	Future task;

	private Runnable r = new Runnable() {
        public void run() {
       	 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            makeValidMove();
        }
    };

	@Override
	public void update(Observable o, Object arg) {
		
		if(arg instanceof Move){
			
				solver.terminate = true;
				solver.makeMove((Move) arg);
				solver.terminate = false;
				return;
		}

//		if(arg  instanceof Gamestate){
//			if((Gamestate) arg == Gamestate.GameOver){
//				thread.interrupt();
//			}
//		}
		
//			     
			//     thread = new Thread(r);
		if(!pool.isShutdown())
			     task= pool.submit(r);
			     
			 //    thread.start();

		
		// TODO Auto-generated method stub
	//	if(arg  instanceof Gamephase){
	//		onGamePhaseChange((Gamephase) arg);
	//	} else if(arg instanceof Gamestate){
	//		 onGameStateChange((Gamestate) arg);
	//	}
		
	}
	
	@Override
	public void close() {
		pool.shutdown();
	}




}

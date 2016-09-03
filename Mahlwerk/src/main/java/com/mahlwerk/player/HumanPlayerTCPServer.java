package com.mahlwerk.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mahlwerk.base.Board;
import com.mahlwerk.base.Game;
import com.mahlwerk.base.Game.Gamestate;
import com.mahlwerk.base.Move;
import com.mahlwerk.base.Piece;
import com.mahlwerk.base.Piece.PieceColor;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class HumanPlayerTCPServer implements IPlayerHandler{
	
	ServerSocket hostServer;
	Socket socket;
	
	BufferedReader in;
	PrintWriter out;
	private Game game;
	private PieceColor color;
	private Gamestate myturn;
	private String ip;
	private boolean server;
	private boolean terminated;
	private boolean connected;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Stage stage = new Stage();
				start(stage);
			}
		});

		

		
			if(server){
				try {
	
					Platform.runLater(()->{
						controller.addText(PieceColor.EMPTY, "Hallo Server. Verbinde...");				
		        	});
System.out.println("Verbinde...");
					hostServer = new ServerSocket(1337);
			        socket = hostServer.accept();
			        connected = true;
			        System.out.println("Client verbunden!");
					System.out.println("");
					Platform.runLater(()->{
						controller.addText(PieceColor.EMPTY, "Verbunden!");				
		        	});


			        
//			        
//			        in = new BufferedReader(new 
//			                  InputStreamReader(socket.getInputStream()));
//			               out = new PrintWriter(socket.getOutputStream(), true);

				} catch (IOException e) {
					System.out.println("Konnte nicht verbinden");
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				}
			} else {
				Platform.runLater(()->{
					controller.addText(PieceColor.EMPTY, "Hallo Client. Verbinde...");				
	        	});
			
				while(true && !terminated){
					
					
	                try {
						socket = new Socket(ip, 1337);
					} catch (IOException e) {
						System.out.println("Konnte nicht mit Server " + ip +":1337 verbinden");
						System.out.println("Neuer Versuch.");
						System.out.println("");

						try {
							Thread.sleep(1500);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
	                if(socket != null)
	                	break;
				}
				System.out.println("Client verbunden!");
				Platform.runLater(()->{
					controller.addText(PieceColor.EMPTY, "Verbunden!");				
	        	});
				connected = true;
				

			}
			
			
			
				
				try {
					oos = new ObjectOutputStream(socket.getOutputStream());
			         ois = new ObjectInputStream(socket.getInputStream());

				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("closing sockets");
					close();
				}
				

		//	makeValidMove();
				
				
				
	        

	

				
			
	}
	
	public HumanPlayerTCPServer(String ip,boolean server ){
		this.ip = ip;
		this.server = server;
	}
	
	TCPController controller;
	AnchorPane root;
	
	BlockingQueue<Move> moveQueue = new ArrayBlockingQueue<Move>(10);
	public void initializeGui() {
		
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();

			loader.setLocation(getClass().getResource("/fxml/ChatGui.fxml"));

			try {
				root = (AnchorPane) loader.load();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			controller = loader.getController();

			
			controller.sendButton.setOnAction((e)->{
				if(!controller.textField.getText().isEmpty()){
					controller.addText(color,controller.textField.getText());
					try {
						if(oos != null){
							oos.writeObject(controller.textField.getText());
							oos.flush();
						}
						
					} catch (IOException e1) {
						System.out.println("Schließe outputstream");
						oos = null;
						// TODO Auto-generated catch block
						//e1.printStackTrace();
						
					}
					controller.textField.setText("");
				}
				
			});
			
			
			Runnable task = () -> {
			    
				Object o= null;
				while(!terminated){
					if(ois != null){
				try {
					o = ois.readObject();
				} catch (ClassNotFoundException | IOException e1) {
					// TODO Auto-generated catch block
				//	e1.printStackTrace();
					System.out.println("StreamTask beendet");
					break;
				}
		        if(o instanceof Move) {
		        	moveQueue.add((Move) o);
		        	System.out.println("added move");
		        //	game.onMoveMake(this, (Move)o);
		           // do something with ds
		        }
		        else if(o instanceof String){
		        	System.out.println("added string");

		        	final String string = (String)o;
		        	Platform.runLater(()->{
			        	controller.addText(Piece.toggleColor(color), string); 
		        	});
		           // something gone wrong - this should not happen if your
		           // socket is connected to the sending side above.
		        } else if(o instanceof Boolean){
		        	System.out.println("got bool, close");
		        	Platform.runLater(()->{
						controller.addText(PieceColor.EMPTY, "Verbindung geschlossen");				
		        	});
		        	game.removePlayer(this);
		        	close();
		        	break;
		        }
		        
				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				}
				
			};
			
			Thread thread = new Thread(task);
			thread.setDaemon(true);
			thread.start();

	}
	
	
	public void start(Stage primaryStage) {

		primaryStage.setOnCloseRequest(e -> {

			game.removePlayer(this);
			close();
			System.out.println("closing");
			return;
		});

		initializeGui();
		
		primaryStage.setScene(new Scene(root));

		primaryStage.setResizable(false);
		primaryStage.sizeToScene();
		primaryStage.setTitle("Chattoo");
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(color == PieceColor.BLACK ? "/img/goBlack3.png" : "/img/goWhite3.png")));
		

		
		primaryStage.show();
		 Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		 primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth() -30) ); 
		 primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight() -30) );  


	

	}
	
	public synchronized void makeValidMove(){
		if(terminated)
			return;
		while(!terminated && !connected ){
			System.out.println("Nicht vebunden");
			System.out.println("warte...");
			System.out.println("");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if(game.gameState == myturn || game.gameState == Gamestate.GameOver){
			
			 try {
			if( game.board.getLastMove() != null){
				
		
			
		       
					oos.writeObject(game.board.getLastMove());
					oos.flush();
		        
			
			}
			Move move = moveQueue.take();
				
			
				        	
				        	game.onMoveMake(this, move);
				           // do something with ds
				  
				
		      
				
		
			 } catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					close();
				}      
		}
	}

	ExecutorService pool = Executors.newSingleThreadExecutor();

	private Runnable r = new Runnable() {
        public void run() {
       	 try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            makeValidMove();
        }
    };

	@Override
	public void update(Observable o, Object arg) {

//		if(arg  instanceof Gamestate){
//			if((Gamestate) arg == Gamestate.GameOver){
//				thread.interrupt();
//			}
//		}
//			     
			//     thread = new Thread(r);
		if(!pool.isShutdown())
			     pool.submit(r);
			     
			 //    thread.start();

		
		// TODO Auto-generated method stub
	//	if(arg  instanceof Gamephase){
	//		onGamePhaseChange((Gamephase) arg);
	//	} else if(arg instanceof Gamestate){
	//		 onGameStateChange((Gamestate) arg);
	//	}
		
	}
	


	@Override
	public void startPlayer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGame(Game game) {
		this.game=game;		
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
	public void makeMove(Move move) {
		
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColor(PieceColor color) {
		// TODO Auto-generated method stub
		this.color = color;
		if(color == PieceColor.BLACK)
			myturn = Gamestate.PlayerBlackTurn;
		else
			myturn = Gamestate.PlayerWhiteTurn;
		
	}

	@Override
	public PieceColor getColor() {
		// TODO Auto-generated method stub
		return color;
	}

	@Override
	public void close() {
		
	
			if(oos != null){
				try {
					oos.writeObject(new Boolean(true));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				}
			}
		

		terminated =true;
		pool.shutdown();
		if(oos != null){
			try {
				oos.close();

			} catch (IOException e ) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			oos = null;
		}
		if(ois != null){
			try {
				ois.close();
				
			} catch ( IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			ois = null;
		}
		

		try {
			if(socket != null)
				socket.close();
			if(hostServer!= null)
				hostServer.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		socket = null;
		
	}

}

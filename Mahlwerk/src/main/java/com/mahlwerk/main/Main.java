package com.mahlwerk.main;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import com.mahlwerk.base.Game;
import com.mahlwerk.base.Piece;
import com.mahlwerk.main.MainGuiController;
import com.mahlwerk.player.AiPlayerNegamax;
import com.mahlwerk.player.AiPlayerRandomConsole;
import com.mahlwerk.player.HumanPlayerGui;
import com.mahlwerk.player.HumanPlayerTCPServer;
import com.mahlwerk.player.IPlayerHandler;

public class Main extends Application{
	
	MainGuiController controller;
	AnchorPane root;

	public static void main(String[] args) {
		launch();
		

	}
	
	public void initializeGui() {
	
			FXMLLoader loader = new FXMLLoader();

			loader.setLocation(Main.class.getResource("/fxml/MainGui.fxml"));
			
			try {
				root = (AnchorPane) loader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
			controller = loader.getController();

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		

	    initializeGui();

		controller.startButton.setOnAction((event) -> {
			
		
			
			
			
			Game game = new Game();

			IPlayerHandler playerWhite=null;
			IPlayerHandler playerBlack=null;
			
			
			int indexWhite = controller.playerWhite.getSelectionModel().selectedIndexProperty().get();
			 switch(indexWhite){
					case 0:
						playerWhite = new HumanPlayerGui();
						playerWhite.setColor(Piece.PieceColor.WHITE);
						playerWhite.setGame(game);
						break;
					case 1:
						playerWhite = new AiPlayerNegamax(Integer.parseInt(controller.thinkT.getText()));
						playerWhite.setColor(Piece.PieceColor.WHITE);
						playerWhite.setGame(game);
						break;
					case 2:
						playerWhite = new AiPlayerRandomConsole();
						playerWhite.setColor(Piece.PieceColor.WHITE);
						playerWhite.setGame(game);
						break;
					case 3:
						playerWhite = new HumanPlayerTCPServer(controller.ipT.getText(), true);
						playerWhite.setColor(Piece.PieceColor.WHITE);
						playerWhite.setGame(game);
						break;
					case 4:
						playerWhite = new HumanPlayerTCPServer(controller.ipT.getText(), false);
						playerWhite.setColor(Piece.PieceColor.WHITE);
						playerWhite.setGame(game);
						break;
					}

				
			    
				int indexBlack = controller.playerBlack.getSelectionModel().selectedIndexProperty().get();
			   switch (indexBlack){
					case 0:
						playerBlack = new HumanPlayerGui();
						playerBlack.setColor(Piece.PieceColor.BLACK);
						playerBlack.setGame(game);
						break;
					case 1:
						playerBlack = new AiPlayerNegamax(Integer.parseInt(controller.thinkT2.getText()));
						playerBlack.setColor(Piece.PieceColor.BLACK);
						playerBlack.setGame(game);
						break;
					case 2:
						playerBlack = new AiPlayerRandomConsole();
						playerBlack.setColor(Piece.PieceColor.BLACK);
						playerBlack.setGame(game);
						break;
					case 3:
						playerBlack = new HumanPlayerTCPServer(controller.ipT2.getText(), true);
						playerBlack.setColor(Piece.PieceColor.BLACK);
						playerBlack.setGame(game);
						break;
					case 4:
						playerBlack = new HumanPlayerTCPServer(controller.ipT2.getText(), false);
						playerBlack.setColor(Piece.PieceColor.BLACK);
						playerBlack.setGame(game);
						break;
					}
				
			   
			  if(indexBlack == 3 || indexWhite == 3){
			
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Information");
						alert.setHeaderText("TCPServer");
						alert.setContentText("Um sich mit dem Server zu verbinden, öffne eine neue Instanz des Spiels und wähle den TCPClient aus. Achte darauf, dass eine andere Farbe gewählt wird!");

						alert.showAndWait();
			  } else if(indexBlack == 4 || indexWhite == 4){
				  Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information");
					alert.setHeaderText("TCPClient");
					alert.setContentText("Um sich mit dem Server zu verbinden, öffne eine neue Instanz des Spiels und wähle den TCPServer aus. Achte darauf, dass die richtige IP und eine andere Farbe gewählt wurde!");
					alert.showAndWait();

			  }

				game.setPlayer(playerBlack);
				game.setPlayer(playerWhite);
				
				game.startGame(indexBlack != 0 && indexWhite != 0);
			
			
			
			
			
			
			
			
			
			
			
			
		});
	    	
		primaryStage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0);
		});


		primaryStage.setScene(new Scene(root));
		primaryStage.setResizable(false);
		primaryStage.sizeToScene();
		primaryStage.setTitle("Mahlwerk 0.2");
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/goBlack3.png")));
		primaryStage.show();
		
		
	

		
	}

}

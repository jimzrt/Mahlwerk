package com.mahlwerk.player;

import java.net.URL;
import java.util.ResourceBundle;

import com.mahlwerk.base.Piece.PieceColor;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * Controller for TCP Client/Server - renders Chat Window
 * @author James Tophoven
 *
 */
public class TCPController implements Initializable{
	
	@FXML
	public ListView<BorderPane> chatList;
	
	@FXML
	public TextField textField;
	
	@FXML
	public Button sendButton;
	

	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		chatList.getItems().addListener((ListChangeListener<BorderPane>) (c -> {
	        c.next();
	        final int size = chatList.getItems().size();
	        if (size > 0) {
	        	chatList.scrollTo(size - 1);
	        }
	    }));
		
		
		textField.setOnKeyPressed((event) -> { if(event.getCode() == KeyCode.ENTER) { sendButton.fire(); } });

	}
	
	public void addText(PieceColor color, String text){
		
		
	    Label chatMessage = new Label(text);
	    chatMessage.setMaxWidth(180);
	    chatMessage.setWrapText(true);
	    chatMessage.setMinHeight(60);
	    
	    BorderPane parent = new BorderPane();
	   // StackPane parent = new StackPane();
	    
	    StackPane pane = new StackPane();

	    pane.setMinHeight(60);
	    pane.setMaxWidth(180);
	    
	    
	    final String cssWhite = "-fx-background-color: rgba(175, 175, 175, 1);\n" + "-fx-background-radius: 50;\n"
				+ "-fx-background-insets: 5;\n";
	    final String cssBlack = "-fx-background-color: rgba(54, 54, 54, 1);\n" + "-fx-background-radius: 50;\n"
				+ "-fx-background-insets: 5;\n";
	    
	    final String cssInfo = "-fx-background-color: rgba(157, 187, 201, 1);\n" + "-fx-background-radius: 10;\n"
				+ "-fx-background-insets: 5;\n";

	    pane.getChildren().add(chatMessage);
	    
	    pane.setPadding(new Insets(5, 30, 5, 30));

	    
	    if(color == PieceColor.WHITE){
	    	pane.setAlignment(Pos.CENTER_LEFT);

		    chatMessage.setAlignment(Pos.CENTER_LEFT);
		    pane.setStyle(cssWhite);
		    chatMessage.setStyle("-fx-text-fill: #000;");
		    parent.setLeft(pane);



	    } else if(color == PieceColor.BLACK){
	    	pane.setAlignment(Pos.CENTER_RIGHT);
		    chatMessage.setAlignment(Pos.CENTER_RIGHT);
		    pane.setStyle(cssBlack);
		    parent.setRight(pane);


	    } else {
	    	pane.setAlignment(Pos.CENTER);
		    chatMessage.setAlignment(Pos.CENTER);
		    chatMessage.setStyle("-fx-text-fill: #000;");

		    pane.setStyle(cssInfo);
		    parent.setCenter(pane);

	    }
	    
		chatList.getItems().add(parent);


	}
	


}

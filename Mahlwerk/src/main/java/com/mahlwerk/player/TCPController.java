package com.mahlwerk.player;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.mahlwerk.base.Piece.PieceColor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class TCPController implements Initializable{
	
	@FXML
	public ListView<StackPane> chatList;
	
	@FXML
	public TextField textField;
	
	@FXML
	public Button sendButton;
	

	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		chatList.getItems().addListener((ListChangeListener<StackPane>) (c -> {
	        c.next();
	        final int size = chatList.getItems().size();
	        if (size > 0) {
	        	chatList.scrollTo(size - 1);
	        }
	    }));
		
		
		textField.setOnKeyPressed((event) -> { if(event.getCode() == KeyCode.ENTER) { sendButton.fire(); } });
		//addText(PieceColor.EMPTY, "Willkommen, mein Sohn. Das hier ist der Chat bruda");

	}
	
	public void addText(PieceColor color, String text){
		
		
	//	ImageView iv = new ImageView(getClass().getResource("/img/goWhite3.png").toExternalForm());
	    Label inftx = new Label(text);
	    inftx.setMaxWidth(180);
	    inftx.setWrapText(true);
	    inftx.setMinHeight(60);
	    
	    //inftx.setFill(Color.WHITE);
	    //inftx.setY(-100);
	    
	    StackPane pane = new StackPane();

	    //pane.setPrefHeight(60);
	    pane.setMinHeight(60);
	    pane.setPrefWidth(100);
	    
	  //  final String paneCss = "-fx-background-color: transparent;";
	    
	    final String cssWhite = "-fx-background-color: rgba(175, 175, 175, 1);\n" + "-fx-background-radius: 50;\n"
				+ "-fx-background-insets: 5;\n";
	    final String cssBlack = "-fx-background-color: rgba(54, 54, 54, 1);\n" + "-fx-background-radius: 50;\n"
				+ "-fx-background-insets: 5;\n";
	    
	    final String cssInfo = "-fx-background-color: rgba(157, 187, 201, 1);\n" + "-fx-background-radius: 10;\n"
				+ "-fx-background-insets: 5;\n";
	    
	   // chatList.setStyle(paneCss);
	    
	    //pane.getChildren().add(iv);
	    pane.getChildren().add(inftx);
	    
	    pane.setPadding(new Insets(5, 30, 5, 30));

	    
	    if(color == PieceColor.WHITE){
	    	pane.setAlignment(Pos.CENTER_LEFT);

		    inftx.setAlignment(Pos.CENTER_LEFT);
		    pane.setStyle(cssWhite);
		    inftx.setStyle("-fx-text-fill: #000;");



	    } else if(color == PieceColor.BLACK){
	    	pane.setAlignment(Pos.CENTER_RIGHT);
		    inftx.setAlignment(Pos.CENTER_RIGHT);
		    pane.setStyle(cssBlack);

	    } else {
	    	pane.setAlignment(Pos.CENTER);
		    inftx.setAlignment(Pos.CENTER);
		    inftx.setStyle("-fx-text-fill: #000;");

		    pane.setStyle(cssInfo);
	    }
	    
	    
		chatList.getItems().add(pane);
		//scroll.setVvalue(1.0);           //1.0 means 100% at the bottom


	}
	


}

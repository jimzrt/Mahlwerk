package com.mahlwerk.player;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class HumanPlayerGuiController implements Initializable {

	@FXML
	public GridPane grid;

	@FXML
	public Label topLabel;

	@FXML
	public Label bottomLabel1;
	@FXML
	public Label bottomLabel2;
	@FXML
	public Label bottomLabel3;

	@FXML
	public AnchorPane topBar;

	@FXML
	public AnchorPane bottomBar;

	@FXML
	public ChoiceBox<String> designChoice;

	@FXML
	public Button passButton;

	@FXML
	public Button revertButton;
	
	@FXML
	public AnchorPane root;
	
	@FXML
	public AnchorPane gridParent;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		grid.getStylesheets().clear();

		designChoice.getItems().add("Light Wood");
		designChoice.getItems().add("Wood");
		designChoice.getItems().add("Dark Wood");
		designChoice.getItems().add("Space");
		root.getStylesheets().add(getClass().getResource("/css/board.css").toExternalForm());

		designChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				changeStyle(designChoice.getItems().get(arg2.intValue()));
			}
		});

		designChoice.getSelectionModel().select(0);
	}

	public void changeStyle(String style) {
		gridParent.getStyleClass().clear();

		gridParent.getStyleClass().add(style.toLowerCase().trim().replaceAll("\\s",""));
			
			


	}

}

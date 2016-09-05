package com.mahlwerk.main;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;

public class MainGuiController implements Initializable {

	@FXML
	public Button startButton;

	@FXML
	public Label thinkL;

	@FXML
	public TextField thinkT;

	@FXML
	public Label thinkL2;

	@FXML
	public TextField thinkT2;

	@FXML
	public Label ipL;

	@FXML
	public TextField ipT;

	@FXML
	public Label ipL2;

	@FXML
	public TextField ipT2;

	@FXML
	public Label hostL;

	@FXML
	public Label hostL2;

	@FXML
	public ChoiceBox<String> playerBlack;

	@FXML
	public ChoiceBox<String> playerWhite;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		final Tooltip tooltip = new Tooltip();
		tooltip.setText("Your password must be\n" + "at least 8 characters in length\n");

		startButton.setTooltip(tooltip);

		playerBlack.getItems().add("Mensch - Gui");
		playerBlack.getItems().add("AI - Negamax");
		playerBlack.getItems().add("AI - Zufall");
		playerBlack.getItems().add("TCP - Server");
		playerBlack.getItems().add("TCP - Client");

		playerWhite.getItems().add("Mensch - Gui");
		playerWhite.getItems().add("AI - Negamax");
		playerWhite.getItems().add("AI - Zufall");
		playerWhite.getItems().add("TCP - Server");
		playerWhite.getItems().add("TCP - Client");

		try {
			hostL.setText("Server IP: " + InetAddress.getLocalHost().getHostAddress());
			hostL2.setText("Server IP: " + InetAddress.getLocalHost().getHostAddress());

		} catch (UnknownHostException e1) {
			hostL.setText("Konnte lokale IP-Adresse nicht ermitteln");
			hostL2.setText("Konnte lokale IP-Adresse nicht ermitteln");

		}

		playerBlack.getSelectionModel().select(0);
		playerWhite.getSelectionModel().select(0);

		playerWhite.getSelectionModel().selectedIndexProperty().addListener((e) -> {

			if (((ReadOnlyIntegerProperty) e).get() == 1) {
				thinkL.visibleProperty().set(true);
				thinkT.visibleProperty().set(true);

			} else {
				thinkL.visibleProperty().set(false);
				thinkT.visibleProperty().set(false);
			}

			if (((ReadOnlyIntegerProperty) e).get() == 3) {
				hostL.visibleProperty().set(true);

			} else {
				hostL.visibleProperty().set(false);
			}

			if (((ReadOnlyIntegerProperty) e).get() == 4) {
				ipL.visibleProperty().set(true);
				ipT.visibleProperty().set(true);
			} else {
				ipL.visibleProperty().set(false);
				ipT.visibleProperty().set(false);
			}

			if ((((ReadOnlyIntegerProperty) e).get() == 3 || ((ReadOnlyIntegerProperty) e).get() == 4)
					&& (playerBlack.getSelectionModel().getSelectedIndex() == 3
							|| playerBlack.getSelectionModel().getSelectedIndex() == 4)) {
				startButton.setDisable(true);
			} else {
				startButton.setDisable(false);

			}

		});

		playerBlack.getSelectionModel().selectedIndexProperty().addListener((e) -> {

			if (((ReadOnlyIntegerProperty) e).get() == 1) {
				thinkL2.visibleProperty().set(true);
				thinkT2.visibleProperty().set(true);

			} else {
				thinkL2.visibleProperty().set(false);
				thinkT2.visibleProperty().set(false);
			}

			if (((ReadOnlyIntegerProperty) e).get() == 3) {
				hostL2.visibleProperty().set(true);

			} else {
				hostL2.visibleProperty().set(false);
			}

			if (((ReadOnlyIntegerProperty) e).get() == 4) {
				ipL2.visibleProperty().set(true);
				ipT2.visibleProperty().set(true);
			} else {
				ipL2.visibleProperty().set(false);
				ipT2.visibleProperty().set(false);
			}

			if ((((ReadOnlyIntegerProperty) e).get() == 3 || ((ReadOnlyIntegerProperty) e).get() == 4)
					&& (playerWhite.getSelectionModel().getSelectedIndex() == 3
							|| playerWhite.getSelectionModel().getSelectedIndex() == 4)) {
				startButton.setDisable(true);
			} else {
				startButton.setDisable(false);

			}

		});

	}
	


}

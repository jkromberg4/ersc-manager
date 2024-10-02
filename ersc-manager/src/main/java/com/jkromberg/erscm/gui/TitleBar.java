package com.jkromberg.erscm.gui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Class for custom title bar
 */
public class TitleBar extends HBox {

	/* -- General -- */
	double offsetX;
	double offsetY;

	/* -- GUI Elements -- */
	private Button minButton;
	private Button closeButton;

	/**
	 * Creates a custom title bar with the specified title. Can optionally display
	 * the program icon and/or have a minimize button.
	 * 
	 * @param title       The title to be displayed on the bar
	 * @param displayIcon Whether or not to display the icon
	 * @param canMinimize Whether or not this bar has a minimize button
	 */
	public TitleBar(String title, boolean displayIcon, boolean canMinimize) {
		setFocusTraversable(false);
		getStyleClass().add("window-bar");

		addElements(title, displayIcon, canMinimize);
		addHandlers();
	}

	/**
	 * Creates and arranges all the necessary gui elements.
	 */
	private void addElements(String title, boolean displayIcon, boolean canMinimize) {
		if (displayIcon) {
			HBox ivWrapper = new HBox();
			ivWrapper.getStyleClass().add("image-wrapper");

			ImageView iv = new ImageView(new Image(TitleBar.class.getResourceAsStream(View.ICON)));
			iv.setFitHeight(20 / 1.25);
			iv.setFitWidth(20 / 1.25);

			ivWrapper.getChildren().add(iv);
			getChildren().add(ivWrapper);
		}

		Label titleLabel = new Label(title);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		if (canMinimize) {
			minButton = new Button();
			minButton.getStyleClass().add("window-min-button");
			Region minIcon = new Region();
			minIcon.getStyleClass().add("icon");
			minButton.setGraphic(minIcon);
		}

		closeButton = new Button();
		closeButton.getStyleClass().add("window-close-button");
		Region closeIcon = new Region();
		closeIcon.getStyleClass().add("icon");
		closeButton.setGraphic(closeIcon);

		if (canMinimize) {
			getChildren().addAll(titleLabel, spacer, minButton, closeButton);
		} else {
			getChildren().addAll(titleLabel, spacer, closeButton);
		}
	}

	/**
	 * Adds the event handlers for the title bar.
	 */
	private void addHandlers() {
		// Window Drag
		setOnMousePressed(event -> {
			offsetX = getScene().getWindow().getX() - event.getScreenX();
			offsetY = getScene().getWindow().getY() - event.getScreenY();
		});

		setOnMouseDragged(event -> {
			getScene().getWindow().setX(event.getScreenX() + offsetX);
			getScene().getWindow().setY(event.getScreenY() + offsetY);
		});

		// Window Buttons
		if (minButton != null) {
			minButton.setOnAction(event -> {
				((Stage) (minButton.getScene().getWindow())).setIconified(true);
			});
		}

		closeButton.setOnAction(event -> {
			closeButton.getScene().getWindow().hide();
		});
	}

}

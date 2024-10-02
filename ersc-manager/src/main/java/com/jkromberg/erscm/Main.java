package com.jkromberg.erscm;

import com.jkromberg.erscm.gui.Controller;
import com.jkromberg.erscm.gui.Model;
import com.jkromberg.erscm.gui.View;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		Model model = new Model();
		Controller controller = new Controller(model);
		controller.setHostServices(getHostServices());
		View view = new View(model, controller);

		// Stage properties
		primaryStage.setScene(view.getScene());
		primaryStage.setTitle(View.TITLE);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.setOpacity(0);
		primaryStage.setOnShown(event -> {
			new Thread(() -> {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Platform.runLater(() -> {
					primaryStage.setOpacity(1);
				});
			}).start();
		});

		primaryStage.show();
	}

}

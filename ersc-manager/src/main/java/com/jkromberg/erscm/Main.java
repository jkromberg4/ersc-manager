package com.jkromberg.erscm;

import com.jkromberg.erscm.gui.Controller;
import com.jkromberg.erscm.gui.Model;
import com.jkromberg.erscm.gui.View;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage mainStage) {
		Model model = new Model();
		Controller controller = new Controller(model);
		controller.setHostServices(getHostServices());
		View view = new View(model, controller);

		// Stage properties
		mainStage.setScene(view.getScene());
		mainStage.setTitle(View.TITLE);
		mainStage.show();
	}

}

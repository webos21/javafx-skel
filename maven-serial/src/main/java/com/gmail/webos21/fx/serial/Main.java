package com.gmail.webos21.fx.serial;

import javafx.application.Application;
import javafx.stage.Stage;

import com.gmail.webos21.fx.serial.ui.MainScene;

public class Main extends Application {

	private MainScene main;

	@Override
	public void init() throws Exception {
		super.init();
		main = new MainScene();
		main.init();
	}

	@Override
	public void start(final Stage primaryStage) {
		main.start(primaryStage);
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		main.stop();
	}

	public static void main(final String[] args) {
		launch(args);
	}

}

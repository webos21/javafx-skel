package com.gmail.webos21.fx.browser;

import javafx.application.Application;
import javafx.stage.Stage;

import com.gmail.webos21.fx.browser.ui.FxBrowser;

public class Main extends Application {

	private FxBrowser browser;

	@Override
	public void init() throws Exception {
		super.init();
		browser = new FxBrowser();
	}

	@Override
	public void start(final Stage primaryStage) {
		browser.start(primaryStage);
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		browser.stop();
	}

	public static void main(final String[] args) {
		launch(args);
	}

}

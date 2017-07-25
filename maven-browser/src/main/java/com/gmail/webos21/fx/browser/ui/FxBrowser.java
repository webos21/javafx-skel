package com.gmail.webos21.fx.browser.ui;

import static javafx.concurrent.Worker.State.FAILED;

import java.net.MalformedURLException;
import java.net.URL;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class FxBrowser {

	private WebView webView;
	private WebEngine engine;

	private BorderPane root;
	private Label lblStatus;

	private Button btnGo;
	private TextField txtURL;
	private ProgressBar progressBar;

	public void start(final Stage primaryStage) {

		final double STANDARD_FONT_SIZE = 12.0;
		final double DEFAULT_FONT_SIZE = Font.getDefault().getSize();
		final DoubleProperty scaleProperty = new SimpleDoubleProperty(DEFAULT_FONT_SIZE / STANDARD_FONT_SIZE);

		webView = new WebView();
		webView.zoomProperty().set(scaleProperty.doubleValue());
		engine = webView.getEngine();

		engine.titleProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
				Stage primStage = (Stage) root.getScene().getWindow();
				primStage.setTitle(newValue);
			}
		});

		engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
			@Override
			public void handle(final WebEvent<String> event) {
				lblStatus.setText(event.getData());
			}
		});

		engine.locationProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
				txtURL.setText(newValue);
			}
		});

		engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
				progressBar.setProgress(newValue.doubleValue());
			}
		});

		engine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
			@Override
			public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
				if (engine.getLoadWorker().getState() == FAILED) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Loading error...");
					alert.setContentText((value != null) ? engine.getLocation() + "\n" + value.getMessage() : engine.getLocation()
							+ "\nUnexpected error.");
					alert.showAndWait();
				}
			}
		});

		btnGo = new Button();
		btnGo.setText("Go");
		btnGo.setOnAction((ActionEvent event) -> {
			loadURL(txtURL.getText());
		});
		btnGo.setAlignment(Pos.CENTER_RIGHT);

		txtURL = new TextField();
		txtURL.setOnAction((ActionEvent event) -> {
			loadURL(txtURL.getText());
		});
		txtURL.setMaxWidth(Double.MAX_VALUE);
		txtURL.setAlignment(Pos.CENTER_LEFT);

		HBox topBar = new HBox();
		HBox.setHgrow(txtURL, Priority.ALWAYS);
		HBox.setHgrow(btnGo, Priority.NEVER);
		topBar.getChildren().addAll(txtURL, btnGo);

		lblStatus = new Label();
		lblStatus.setMaxWidth(Double.MAX_VALUE);
		lblStatus.setAlignment(Pos.CENTER_LEFT);

		progressBar = new ProgressBar();
		progressBar.progressProperty().set(0.0);

		HBox statusBar = new HBox();
		HBox.setHgrow(lblStatus, Priority.ALWAYS);
		HBox.setHgrow(progressBar, Priority.NEVER);
		statusBar.getChildren().addAll(lblStatus, progressBar);

		root = new BorderPane();
		root.setTop(topBar);
		root.setCenter(webView);
		root.setBottom(statusBar);

		final Scene scene = new Scene(root, 1150, 800);

		primaryStage.setTitle("JavaFX Browser");
		primaryStage.setScene(scene);
		primaryStage.show();

		engine.load("http://www.naver.com/");
	}

	public void stop() {
	}

	private void loadURL(final String url) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				String tmp = toURL(url);

				if (tmp == null) {
					tmp = toURL("http://" + url);
				}

				engine.load(tmp);
			}
		});
	}

	private static String toURL(String str) {
		try {
			return new URL(str).toExternalForm();
		} catch (MalformedURLException exception) {
			return null;
		}
	}

}

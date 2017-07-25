package com.gmail.webos21.fx.serial.ui;

import java.util.Arrays;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

import com.gmail.webos21.fx.serial.comm.SerialMonitor;
import com.gmail.webos21.fx.serial.comm.SerialReceiver;
import com.gmail.webos21.fx.serial.comm.SerialSender;

public class MainScene {

	private SerialMonitor sm;
	private SerialDataEventProcess sdep;
	private SerialPortEventProcess spep;

	private String[] portList;

	private SerialPort comPort;
	private SerialReceiver comRecv;
	private SerialSender comSend;

	private BorderPane root;

	private Label lblPort;
	private ComboBox<String> selCom;
	private ComboBox<String> selConf;
	private ComboBox<Integer> selBaud;
	private Button btnConnect;
	private Button lblReceived;
	private TextArea txtReceived;
	private Label lblSend;
	private TextField txtSend;
	private Button btnSend;

	public void init() {
		this.sdep = new SerialDataEventProcess();
		this.spep = new SerialPortEventProcess();
		this.sm = new SerialMonitor(spep);
	}

	public void stop() {
		serialDisconnect();
		if (sm != null) {
			sm.stopMonitor();
			sm = null;
		}
	}

	private void serialConnect() {
		int selIdx = selCom.getSelectionModel().getSelectedIndex();
		if (selIdx >= 0) {
			String port = selCom.getSelectionModel().getSelectedItem();
			Integer baud = selBaud.getSelectionModel().getSelectedItem();
			try {
				comPort = (SerialPort) CommPortIdentifier.getPortIdentifier(port).open(this.getClass().getName(), 0);
				comPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				comPort.enableReceiveTimeout(500);

				comRecv = new SerialReceiver(comPort, sdep);
				comSend = new SerialSender(comPort);

				comRecv.startIn();
				comSend.startOut();

				selCom.setDisable(true);
				selConf.setDisable(true);
				selBaud.setDisable(true);
				btnConnect.setText("Disconnect");
			} catch (PortInUseException piue) {
				piue.printStackTrace();
			} catch (NoSuchPortException nspe) {
				nspe.printStackTrace();
			} catch (UnsupportedCommOperationException ucoe) {
				ucoe.printStackTrace();
			}
		} else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Serial Port 미선택");
			alert.setContentText("연결할 Serial Port를 선택하세요.");
			alert.showAndWait();
		}
	}

	private void serialDisconnect() {
		if (comRecv != null) {
			comRecv.stopIn();
			comRecv = null;
		}
		if (comSend != null) {
			comSend.stopOut();
			comSend = null;
		}
		if (comPort != null) {
			comPort.close();
			comPort = null;
		}
		selCom.setDisable(false);
		selConf.setDisable(false);
		selBaud.setDisable(false);
		btnConnect.setText("Connect");
	}

	public void start(final Stage primaryStage) {
		lblPort = new Label();
		lblPort.setText("Port");
		lblPort.setMinWidth(65);

		selCom = new ComboBox<String>();
		selCom.setMaxWidth(Double.MAX_VALUE);

		selConf = new ComboBox<String>();
		selConf.setMinWidth(70);
		selConf.setItems(FXCollections.observableArrayList(new String[] { "8N1" }));
		selConf.setValue("8N1");

		selBaud = new ComboBox<Integer>();
		selBaud.setMinWidth(100);
		selBaud.setItems(FXCollections.observableArrayList(new Integer[] { 9600, 115200 }));
		selBaud.setValue(115200);

		btnConnect = new Button();
		btnConnect.setText("Connect");
		btnConnect.setOnAction((ActionEvent event) -> {
			if ("Connect".equals(btnConnect.getText())) {
				serialConnect();
			} else if ("Disconnect".equals(btnConnect.getText())) {
				serialDisconnect();
			}
		});
		btnConnect.setMinWidth(100);
		btnConnect.setAlignment(Pos.CENTER);

		HBox portBar = new HBox();
		portBar.setPadding(new Insets(10, 10, 5, 10));
		portBar.setSpacing(10);
		HBox.setHgrow(lblPort, Priority.NEVER);
		HBox.setHgrow(selCom, Priority.ALWAYS);
		HBox.setHgrow(selConf, Priority.NEVER);
		HBox.setHgrow(selBaud, Priority.NEVER);
		HBox.setHgrow(btnConnect, Priority.NEVER);
		portBar.getChildren().addAll(lblPort, selCom, selConf, selBaud, btnConnect);

		lblReceived = new Button();
		lblReceived.setWrapText(true);
		lblReceived.setText("Received\nClear");
		lblReceived.setAlignment(Pos.TOP_LEFT);
		lblReceived.setMinWidth(65);
		lblReceived.setOnAction((ActionEvent event) -> {
			txtReceived.setText("");
		});

		txtReceived = new TextArea();
		txtReceived.setFont(Font.font("Monospaced"));
		txtReceived.setEditable(false);

		HBox recvBar = new HBox();
		recvBar.setPadding(new Insets(5, 10, 5, 10));
		recvBar.setSpacing(10);
		HBox.setHgrow(lblReceived, Priority.NEVER);
		HBox.setHgrow(txtReceived, Priority.ALWAYS);
		VBox.setVgrow(txtReceived, Priority.ALWAYS);
		recvBar.getChildren().addAll(lblReceived, txtReceived);

		lblSend = new Label();
		lblSend.setText("Send");
		lblSend.setMinWidth(65);

		txtSend = new TextField();
		txtSend.setOnAction((ActionEvent event) -> {
			if (comSend != null) {
				comSend.sendMessage(txtSend.getText() + "\r");
			}
			txtSend.setText("");
		});
		txtSend.setMaxWidth(Double.MAX_VALUE);

		btnSend = new Button();
		btnSend.setText("Send");
		btnSend.setOnAction((ActionEvent event) -> {
			if (comSend != null) {
				comSend.sendMessage(txtSend.getText() + "\r");
			}
		});
		btnSend.setMinWidth(100);
		btnSend.setAlignment(Pos.CENTER);

		HBox sendBar = new HBox();
		sendBar.setPadding(new Insets(5, 10, 10, 10));
		sendBar.setSpacing(10);
		HBox.setHgrow(lblSend, Priority.NEVER);
		HBox.setHgrow(txtSend, Priority.ALWAYS);
		HBox.setHgrow(btnSend, Priority.NEVER);
		sendBar.getChildren().addAll(lblSend, txtSend, btnSend);

		root = new BorderPane();
		root.setTop(portBar);
		root.setCenter(recvBar);
		root.setBottom(sendBar);

		final Scene scene = new Scene(root, 1150, 800);

		primaryStage.setTitle("Serial Console");
		primaryStage.setScene(scene);
		primaryStage.show();

		sm.startMonitor();
	}

	private class SerialDataEventProcess implements SerialReceiver.SerialDataListener {
		@Override
		public void onSerialData(final String msg) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					MainScene.this.txtReceived.appendText(msg + "\r\n");
				}
			});
		}

		@Override
		public void onSerialDisconnected() {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					serialDisconnect();
				}
			});
		}
	}

	private class SerialPortEventProcess implements SerialMonitor.SerialPortListener {
		@Override
		public void onSerialPortFound(String[] ports) {
			if (portList != null && Arrays.equals(portList, ports)) {
				return;
			}
			portList = ports;
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (!selCom.isDisabled()) {
						selCom.setItems(FXCollections.observableArrayList(portList));
					}
				}
			});
		}
	}

}

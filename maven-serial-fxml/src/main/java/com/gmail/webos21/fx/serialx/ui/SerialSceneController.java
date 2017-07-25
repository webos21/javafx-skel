package com.gmail.webos21.fx.serialx.ui;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

import com.gmail.webos21.fx.serialx.comm.SerialMonitor;
import com.gmail.webos21.fx.serialx.comm.SerialReceiver;
import com.gmail.webos21.fx.serialx.comm.SerialSender;

public class SerialSceneController implements Initializable {

	private SerialMonitor sm;
	private SerialDataEventProcess sdep;
	private SerialPortEventProcess spep;

	private String[] portList;

	private SerialPort comPort;
	private SerialReceiver comRecv;
	private SerialSender comSend;

	@FXML
	private ComboBox<String> selPort;

	@FXML
	private ComboBox<String> selSettings;

	@FXML
	private ComboBox<Integer> selBaud;

	@FXML
	private Button btnConnect;

	@FXML
	private TextArea taReceived;

	@FXML
	private CheckBox chkAutoScroll;

	@FXML
	private TextField txtSend;

	@FXML
	private Button btnSend;

	@FXML
	private ComboBox<String> selEtx;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.sdep = new SerialDataEventProcess();
		this.spep = new SerialPortEventProcess();
		this.sm = new SerialMonitor(spep);

		selSettings.setItems(FXCollections.observableArrayList(new String[] { "8N1" }));
		selBaud.setItems(FXCollections.observableArrayList(new Integer[] { 9600, 115200 }));

		sm.startMonitor();
	}

	@FXML
	private void handleConnectPushed() {
		if ("Connect".equals(btnConnect.getText())) {
			serialConnect();
		} else if ("Disconnect".equals(btnConnect.getText())) {
			serialDisconnect();
		}
	}

	@FXML
	private void handleReceivedScroll() {
		chkAutoScroll.setSelected(false);
	}

	@FXML
	private void handleAutoScrollChecked() {
		if (chkAutoScroll.isSelected()) {
			taReceived.setScrollTop(Double.MAX_VALUE);
		}
	}

	@FXML
	private void handleSendText() {
		if (comSend != null) {
			comSend.sendMessage(txtSend.getText() + getLineEnd());
		}
		txtSend.setText("");
	}

	@FXML
	private void handleSend() {
		if (comSend != null) {
			comSend.sendMessage(txtSend.getText() + getLineEnd());
		}
	}

	public void uninitialize() {
		serialDisconnect();
		if (sm != null) {
			sm.stopMonitor();
			sm = null;
		}
	}

	private String getLineEnd() {
		String v = selEtx.getValue();
		if ("CR+LF".equals(v)) {
			return "\r\n";
		} else if ("CR".equals(v)) {
			return "\r";
		} else if ("LF".equals(v)) {
			return "\n";
		} else {
			return "";
		}
	}

	private void serialConnect() {
		int selPortIdx = selPort.getSelectionModel().getSelectedIndex();
		if (selPortIdx < 0) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Serial Port");
			alert.setContentText("Choose the [Serial Port].");
			alert.showAndWait();
			return;
		}

		int selSettingsIdx = selSettings.getSelectionModel().getSelectedIndex();
		if (selSettingsIdx < 0) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Serial Settings");
			alert.setContentText("Choose the [Serial Settings].");
			alert.showAndWait();
			return;
		}

		int selBaudRate = selBaud.getSelectionModel().getSelectedIndex();
		if (selBaudRate < 0) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Baud Rate");
			alert.setContentText("Choose the [Baud Rate].");
			alert.showAndWait();
			return;
		}

		String port = selPort.getSelectionModel().getSelectedItem();
		Integer baud = selBaud.getSelectionModel().getSelectedItem();
		try {
			comPort = (SerialPort) CommPortIdentifier.getPortIdentifier(port).open(this.getClass().getName(), 0);
			comPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			comPort.enableReceiveTimeout(500);

			comRecv = new SerialReceiver(comPort, sdep);
			comSend = new SerialSender(comPort);

			comRecv.startIn();
			comSend.startOut();

			selPort.setDisable(true);
			selSettings.setDisable(true);
			selBaud.setDisable(true);
			btnConnect.setText("Disconnect");
		} catch (PortInUseException piue) {
			piue.printStackTrace();
		} catch (NoSuchPortException nspe) {
			nspe.printStackTrace();
		} catch (UnsupportedCommOperationException ucoe) {
			ucoe.printStackTrace();
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
		selPort.setDisable(false);
		selSettings.setDisable(false);
		selBaud.setDisable(false);
		btnConnect.setText("Connect");
	}

	private class SerialDataEventProcess implements SerialReceiver.SerialDataListener {
		@Override
		public void onSerialData(final String msg) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					taReceived.appendText(msg + "\r\n");
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
					if (!selPort.isDisabled()) {
						selPort.setItems(FXCollections.observableArrayList(portList));
					}
				}
			});
		}
	}

}

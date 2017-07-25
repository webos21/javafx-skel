package com.gmail.webos21.fx.serialx.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import purejavacomm.SerialPort;

public class SerialReceiver implements Runnable {

	private BufferedReader rd;
	private SerialDataListener listener;

	private boolean ctrl;
	private Thread thrHnd;

	public SerialReceiver(SerialPort com, SerialDataListener sl) {
		try {
			this.rd = new BufferedReader(new InputStreamReader(com.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.listener = sl;
	}

	@Override
	public void run() {
		while (ctrl) {
			String msg = null;
			try {
				msg = rd.readLine();
				if (ctrl && msg != null) {
					if (ctrl && listener != null) {
						listener.onSerialData(msg);
					}
				}
			} catch (IOException e) {
				if ("Underlying input stream returned zero bytes".equals(e.getMessage())) {
					if (msg != null) {
						if (ctrl && listener != null) {
							listener.onSerialData(msg);
						}
					}
				} else {
					// Cannot prevent: jtermios.windows.JTermiosImpl$Fail
					// e.printStackTrace();
					if (ctrl && listener != null) {
						listener.onSerialDisconnected();
					}
					ctrl = false;
				}
			}
		}

		if (rd != null) {
			try {
				rd.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			rd = null;
		}
	}

	public void startIn() {
		this.ctrl = true;
		thrHnd = new Thread(this, this.getClass().getSimpleName());
		thrHnd.start();
	}

	public void stopIn() {
		this.listener = null;
		this.ctrl = false;
		try {
			thrHnd.join(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public interface SerialDataListener {

		public void onSerialData(String msg);

		public void onSerialDisconnected();

	}

}

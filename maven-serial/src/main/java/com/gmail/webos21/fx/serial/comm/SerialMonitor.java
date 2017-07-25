package com.gmail.webos21.fx.serial.comm;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import purejavacomm.CommPortIdentifier;

public class SerialMonitor implements Runnable {

	private static final int MONITOR_INTERVAL = 3000;

	private SerialPortListener listener;

	private boolean ctrl;
	private Thread thrHnd;

	public SerialMonitor(SerialPortListener l) {
		this.listener = l;
	}

	@Override
	public void run() {
		while (ctrl) {
			Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
			List<CommPortIdentifier> list = Collections.list(portEnum);
			if (ctrl && list != null) {
				String[] ret = new String[list.size()];
				for (int i = 0; i < ret.length; i++) {
					ret[i] = list.get(i).getName();
				}
				if (ctrl && listener != null) {
					listener.onSerialPortFound(ret);
				}
			}
			if (ctrl) {
				synchronized (thrHnd) {
					try {
						thrHnd.wait(MONITOR_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public interface SerialPortListener {
		public void onSerialPortFound(String[] ports);
	}

	public void startMonitor() {
		ctrl = true;
		thrHnd = new Thread(this, this.getClass().getSimpleName());
		thrHnd.start();
	}

	public void stopMonitor() {
		listener = null;
		ctrl = false;
		synchronized (thrHnd) {
			thrHnd.notifyAll();
		}
		try {
			thrHnd.join(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

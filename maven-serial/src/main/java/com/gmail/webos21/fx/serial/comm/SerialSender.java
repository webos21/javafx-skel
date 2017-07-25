package com.gmail.webos21.fx.serial.comm;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import purejavacomm.SerialPort;

public class SerialSender implements Runnable {

	private OutputStream os;
	private Queue<byte[]> sq;

	private boolean ctrl;
	private Thread thrHnd;

	public SerialSender(SerialPort com) {
		this.sq = new ArrayBlockingQueue<byte[]>(5);
		try {
			this.os = com.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		byte[] msg = null;
		while (ctrl) {
			if (ctrl && sq != null && (msg = sq.poll()) != null) {
				if (ctrl && os != null) {
					try {
						os.write(msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			synchronized (sq) {
				try {
					sq.wait(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (sq != null) {
			sq.clear();
			sq = null;
		}
		if (os != null) {
			try {
				this.os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			os = null;
		}
	}

	public void startOut() {
		ctrl = true;
		thrHnd = new Thread(this, this.getClass().getSimpleName());
		thrHnd.start();
	}

	public void stopOut() {
		ctrl = false;
		synchronized (sq) {
			sq.notifyAll();
		}
		try {
			thrHnd.join(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String msg) {
		if (sq != null) {
			sq.add(msg.getBytes());
		}
	}

	public void sendMessage(byte[] msg) {
		if (sq != null) {
			sq.add(msg);
		}
	}

}

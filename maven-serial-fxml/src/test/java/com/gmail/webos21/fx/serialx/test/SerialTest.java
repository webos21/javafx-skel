package com.gmail.webos21.fx.serialx.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

public class SerialTest {

	public static void main(String[] args) {
		SerialPort p = null;
		try {
			p = (SerialPort) CommPortIdentifier.getPortIdentifier(args[0]).open(SerialTest.class.getName(), 0);
			p.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			p.enableReceiveTimeout(200);

			OutputStream out = p.getOutputStream();
			InputStream in = p.getInputStream();

			out.write("test\r".getBytes());

			byte[] rb = new byte[128];
			in.read(rb);

			System.out.println(new String(rb));

			p.close();
		} catch (PortInUseException e) {
			e.printStackTrace();
		} catch (NoSuchPortException e) {
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (p != null) {
				p.close();
			}
		}

	}

}

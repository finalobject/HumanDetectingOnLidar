package com.kristou.urgLibJ.Connection;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

public class SerialConnection implements Connection {

    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean connected = false;
    private SerialPort serialPort;

    private String deviceName;
    private int deviceSpeed = 115200;
    private Vector<Byte> receiveBuffer = new Vector<Byte>();

    private String error_message;

    public SerialConnection() {
    }

    @Override
    public String what() {
        // TODO Auto-generated method stub
        return error_message;
    }

    @Override
    public boolean connect(String device, int baudrate) {
        deviceName = device;
        deviceSpeed = baudrate;
        CommPortIdentifier portIdentifier;
        boolean conn = false;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(deviceName);
            if (portIdentifier.isCurrentlyOwned()) {
                error_message = "Port is currently in use";
            } else {
                serialPort = (SerialPort) portIdentifier.open(this.getClass()
                        .getName(), 2000);
                serialPort.setSerialPortParams(deviceSpeed,
                        SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();

                connected = true;
                conn = true;
            }
        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException e) {
            error_message = "Could not open device.";
        }
        return conn;
    }

    @Override
    public boolean connect(String device) {
        return connect(device, deviceSpeed);
    }

    @SuppressWarnings("unchecked")
    public Vector<String> getPortList() {
        Enumeration<CommPortIdentifier> portList;
        Vector<String> portVect = new Vector<String>();
        portList = CommPortIdentifier.getPortIdentifiers();

        CommPortIdentifier portId;
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                portVect.add(portId.getName());
            }
        }

        return portVect;
    }

    @Override
    public boolean disconnect() {
        boolean disconn = true;
        try {
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            disconn = false;
        }
        serialPort.close();
        connected = false;
        return disconn;
    }

    @Override
    public boolean setBaudrate(int baudrate) {
        deviceSpeed = baudrate;
        disconnect();
        return connect(deviceName, baudrate);
    }

    @Override
    public int getBaudrate() {
        return deviceSpeed;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean send(String data, int count) {
        boolean success = false;
        if (isConnected()) {
            try {
                outputStream.write(data.substring(0, count).getBytes());
                success = true;
            } catch (IOException e) {
                disconnect();
            }
        }
        return success;
    }

    @Override
    public String receive(int count, int timeout) {
        long startTime = System.currentTimeMillis();
        String result = "";
        byte[] buffer = new byte[1024];
        int len = -1;
        int readBytes = 0;
        while (readBytes <= count) {
            try {
                if ((inputStream.available()) > 0) {
                    if ((len = inputStream.read(buffer)) > -1) {
                        for (int i = 0; i < len; ++i) {
                            receiveBuffer.add(buffer[i]);
//							System.out.printf("" + (char)buffer[i]);
                        }
                    }
                }
            } catch (IOException e) {
                error_message = "Error receiving";
                return null;
            }

            while (receiveBuffer.size() > 0) {
                result += (char) receiveBuffer.elementAt(0).byteValue();
                receiveBuffer.removeElementAt(0);
                readBytes++;
                if (readBytes == count) {
                    return result;
                }
            }

            long endTime = System.currentTimeMillis();
            int duration = (int) (endTime - startTime);
            if (duration > timeout) {
                break;
            }
        }

        return result;
    }

    @Override
    public void flush() {
        // TODO Auto-generated method stub

    }

    @Override
    public void clear() {
        receiveBuffer.clear();

    }

    @Override
    public ConnectionType type() {
        return ConnectionType.SERIAL_TYPE;
    }

    @Override
    public void ungetc(char c) {
        receiveBuffer.add(0, (byte) c);

    }

    @Override
    public boolean send(String data) {
        boolean success = false;
        if (isConnected()) {
            try {
                outputStream.write(data.getBytes());
                success = true;
            } catch (IOException e) {
                disconnect();
            }
        }
        return success;
    }

}

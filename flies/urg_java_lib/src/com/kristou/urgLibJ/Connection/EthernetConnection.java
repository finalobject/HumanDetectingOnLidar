package com.kristou.urgLibJ.Connection;

import java.io.*;
import java.net.*;
import java.util.Vector;

public class EthernetConnection implements Connection {

    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean connected = false;
    private Socket ethernetPort;

    private String deviceName;
    private int devicePort = 10940;
    private int baudrate;
    private Vector<Byte> receiveBuffer = new Vector<Byte>();

    private String error_message;

    public EthernetConnection() {

    }

    @Override
    public String what() {
        return error_message;
    }

    @Override
    public boolean connect(String device, int port) {
        connected = false;
        deviceName = device;
        devicePort = port;
        try {
            ethernetPort = new Socket(deviceName, devicePort);
            if (ethernetPort != null) {
                inputStream = ethernetPort.getInputStream();
                outputStream = ethernetPort.getOutputStream();
                connected = true;
            }
        } catch (Exception e) {
            error_message = "Could not connect to device.";
        }
        return connected;
    }

    @Override
    public boolean connect(String device) {
        return connect(device, devicePort);
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
        if (ethernetPort.isConnected()) {
            try {
                ethernetPort.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        connected = false;
        return disconn;
    }

    @Override
    public boolean setBaudrate(int baud) {
        baudrate = baud;
        return true;
    }

    @Override
    public int getBaudrate() {
        return baudrate;
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
        return ConnectionType.ETHERNET_TYPE;
    }

    @Override
    public void ungetc(char c) {
        receiveBuffer.add(0, (byte) c);
    }

}

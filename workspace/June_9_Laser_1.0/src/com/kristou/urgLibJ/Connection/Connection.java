package com.kristou.urgLibJ.Connection;

public interface Connection {

    public enum ConnectionType {

        SERIAL_TYPE,
        ETHERNET_TYPE,
        CUSTOM_TYPE
    }

    public String what();

    public boolean connect(String device, int baudrate);

    public boolean connect(String device);

    public boolean disconnect();

    public boolean setBaudrate(int baud);

    public int getBaudrate();

    public boolean isConnected();

    public boolean send(String data, int count);

    public boolean send(String data);

    public String receive(int count, int timeout);

    public void flush();

    public void clear();

    public ConnectionType type();

    public void ungetc(char c);
}

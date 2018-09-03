package com.kristou.urgLibJ.Connection;

public class ConnectionUtil {

    private Connection con;

    public ConnectionUtil(Connection con_) {
        con = con_;
    }

    public boolean isLF(char ch) {
        return ((ch == '\r') || (ch == '\n')) ? true : false;
    }

    public void skip(int total_timeout, int each_timeout) {
        long startTime = System.currentTimeMillis();

        while (true) {
            String result = readline(4092, each_timeout);
            System.out.printf("Skip: " + result);
            System.out.println();
            if (result.isEmpty()) {
                break;
            }
            long endTime = System.currentTimeMillis();
            int duration = (int) (endTime - startTime);
            if (duration > total_timeout) {
                break;
            }
        }
    }

    public String readline(int count, int timeout) {
        String buffer = "";
        int filled = 0;

        while (filled < count) {
            String result = con.receive(1, timeout);
            if (result.isEmpty()) {
                break;
            } else if (isLF(result.charAt(0))) {
                break;
            }
            buffer += result;
            filled++;
        }
        if (filled == count) {
            --filled;
            con.ungetc(buffer.charAt(filled));
        }
        buffer = buffer.substring(0, filled);

        return buffer;
    }
}

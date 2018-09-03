/**
 *
 */
package com.kristou.urgLibJ.RangeSensor.Capture;

import java.util.Vector;
import com.kristou.urgLibJ.Connection.Connection;
import com.kristou.urgLibJ.Connection.ConnectionUtil;
import com.kristou.urgLibJ.RangeSensor.RangeSensorInformation;
import com.kristou.urgLibJ.RangeSensor.RangeSensorInternalInformation;
import com.kristou.urgLibJ.RangeSensor.RangeSensorParameter;

public class CaptureHandler {

    public static boolean Off = false;
    public static boolean On = true;
    public static boolean Force = true;
    Connection myConnection = null;

    private String error_message = "";

    public CaptureHandler(Connection con) {
        myConnection = con;
    }

    public CaptureHandler() {
    }

    public String what() {
        return error_message;
    }

    public void setConnection(Connection con) {
        myConnection = con;
    }

    public Connection getConnection() {
        return myConnection;
    }

    // Match the baudrate and then connect the device.
    public boolean connect(String device, int baudrate) {
        return false;
    }

    public boolean send(String data) {
        return myConnection.send(data);
    }

    public String recv(int size, int timeout) {
        return null;
    }

    public Vector<String> getSupportedCommands() {
        return null;
    }

    public Vector<Capture> getSupportedModes() {
        return null;
    }

    public boolean setRawTimestampMode(boolean on) {
        return false;
    }

    public boolean rawTimestamp(int timestamp) {
        return false;
    }

    public boolean setLaserOutput(boolean on, boolean force) {
        boolean result = false;
        ;
        if (on == On) {
            result = sendBM(force);
        } else {
            result = sendQT();
        }
        return result;
    }

    public boolean sendBM(boolean force) {
        String command = "BM\n";
        send(command);
        Vector<String> lines = receiveAnswer();
        if (lines.size() == 0) {
            error_message = "No response for BM command";
            return false;
        }
        command = command.substring(0, command.length() - 1);
        if (!lines.elementAt(0).equals(command)) {
            error_message = "No echo for BM command";
            return false;
        }

        if (lines.size() < 2) {
            error_message = "No status for BM command";
            return false;
        }

        if (!testChecksun(lines.elementAt(1))) {
            error_message = "Status for BM command failed checksum";
            return false;
        }

        String status = lines.elementAt(1).substring(0,
                lines.elementAt(1).length() - 1);

        if (status.equals("01")) {
            error_message = "The laser was not lighted due to unstable or abnormal condition.";
            return false;
        }
        if (status.equals("00")) {
            return true;
        }
        if (status.equals("02")) {
            if (force) {
                return true;
            }
            error_message = "The sensor is already in measurement state and the laser is already lighted.";
            return false;
        }
        return true;
    }

    public boolean sendQT() {
        String command = "QT\n";
        send(command);
        Vector<String> lines = receiveAnswerForEcho("QT");
        if (lines.size() == 0) {
            error_message = "No response for QT command";
            return false;
        }
        command = command.substring(0, command.length() - 1);
        if (!lines.elementAt(0).equals(command)) {
            error_message = "No echo for QT command";
            return false;
        }

        if (lines.size() < 2) {
            error_message = "No status for QT command";
            return false;
        }

        if (!testChecksun(lines.elementAt(1))) {
            error_message = "Status for QT command failed checksum";
            return false;
        }
        String status = lines.elementAt(1).substring(0,
                lines.elementAt(1).length() - 1);

        if (!status.equals("00")) {
            error_message = "Status for QT command: " + status;
            return false;
        }
        return true;
    }

    public boolean sendSCIP2() {
        String command = "SCIP2.0\n";
        send(command);
        Vector<String> lines = receiveAnswer();
        if (lines.size() == 0) {
            error_message = "No response for SCIP2.0 command";
            return false;
        }
        command = command.substring(0, command.length() - 1);
        if (!lines.elementAt(0).equals(command)) {
            error_message = "No echo for SCIP2.0 command";
            return false;
        }

        if (lines.size() < 2) {
            error_message = "No status for SCIP2.0 command";
            return false;
        }

        if (!testChecksun(lines.elementAt(1))) {
            error_message = "Status for SCIP2.0 command failed checksum";
            return false;
        }
        String status = lines.elementAt(1).substring(0,
                lines.elementAt(1).length() - 1);

        if (!status.equals("00")) {
            error_message = "Status for SCIP2.0 command: " + status;
            return false;
        }
        return true;
    }

    public boolean setLaserOutput(boolean on) {
        return setLaserOutput(on, true);
    }

    public String getCommand(CaptureSettings settings) {

        return null;
    }

    public Vector<String> receiveAnswer() {
        ConnectionUtil cu = new ConnectionUtil(myConnection);
        Vector<String> result = new Vector<String>();
        boolean stillData = true;
        while (stillData) {
            String response = cu.readline(4092, 1500);
//			System.out.printf("received: " + response);
//			System.out.println();
            if (response.length() > 0) {
                result.add(response);
            } else {
                stillData = false;
            }
        }

        return result;
    }

    public Vector<String> receiveAnswerForEcho(String command) {
        ConnectionUtil cu = new ConnectionUtil(myConnection);
        Vector<String> result = new Vector<String>();
        boolean stillData = true;
        boolean echoFound = false;
        while (stillData) {
            String response = cu.readline(4092, 1000);

            if (!echoFound && !response.equals(command)) {
                continue;
            }
            echoFound = true;
            if (response.length() > 0) {
                result.add(response);
            } else {
                stillData = false;
            }
        }

        return result;
    }

    public boolean testChecksun(String st) {
        int actual_sum = (int) st.charAt(st.length() - 1);
        String data = st.substring(0, st.length() - 1);
        int expected_sum = 0x00;
        for (int i = 0; i < data.length(); i++) {
            expected_sum += (int) data.charAt(i);
        }
        expected_sum = (expected_sum & 0x3f) + 0x30;

        return (expected_sum == actual_sum) ? true : false;
    }

    public long decode(String data) {
        long value = 0;
        for (int i = 0; i < data.length(); i++) {
            value <<= 6;
            value &= ~0x3f;
            value |= data.charAt(i) - 0x30;
        }
        return value;
    }

    public RangeSensorParameter getSensorParameter() {
        CaptureHandler ch = new CaptureHandler(myConnection);
        RangeSensorParameter param = new RangeSensorParameter();
        String command = "PP\n";
        ch.send(command);
        Vector<String> lines = ch.receiveAnswer();

        if (lines.size() == 0) {
            error_message = "No response for PP command";
            return null;
        }
        command = command.substring(0, command.length() - 1);
        if (!lines.elementAt(0).equals(command)) {
            error_message = "No echo for PP command";
            return null;
        }

        if (lines.size() < 2) {
            error_message = "No status for PP command";
            return null;
        }

        if (!testChecksun(lines.elementAt(1))) {
            error_message = "Status for PP command failed checksum";
            return null;
        }
        String status = lines.elementAt(1).substring(0,
                lines.elementAt(1).length() - 1);

        if (!status.equals("00")) {
            error_message = "Status for PP command: " + status;
            return null;
        }

        for (int i = 2; i < lines.size(); i++) {
            String line = lines.elementAt(i);
            line = line.replace(";", "");
            if (!ch.testChecksun(line)) {
                error_message = "PP checksum failed at " + i;
                return null;
            }

            line = line.substring(0, line.length() - 1);
            if (line.startsWith("MODL:")) {
                param.model = line.substring(5);
            }
            if (line.startsWith("DMIN:")) {
                param.distance_min = Long.parseLong(line.substring(5));
            }
            if (line.startsWith("DMAX:")) {
                param.distance_max = Long.parseLong(line.substring(5));
            }
            if (line.startsWith("ARES:")) {
                param.area_total = Integer.parseInt(line.substring(5));
            }
            if (line.startsWith("AMIN:")) {
                param.area_min = Integer.parseInt(line.substring(5));
            }
            if (line.startsWith("AMAX:")) {
                param.area_max = Integer.parseInt(line.substring(5));
            }
            if (line.startsWith("AFRT:")) {
                param.area_front = Integer.parseInt(line.substring(5));
            }
            if (line.startsWith("SCAN:")) {
                param.scan_rpm = Integer.parseInt(line.substring(5));
            }
        }

        return param;
    }

    public RangeSensorInformation getSensorInformation() {
        CaptureHandler ch = new CaptureHandler(myConnection);
        RangeSensorInformation info = new RangeSensorInformation();
        String command = "VV\n";
        ch.send(command);
        Vector<String> lines = ch.receiveAnswer();

        if (lines.size() == 0) {
            error_message = "No response for VV command";
            return null;
        }
        command = command.substring(0, command.length() - 1);
        if (!lines.elementAt(0).equals(command)) {
            error_message = "No echo for VV command";
            return null;
        }

        if (lines.size() < 2) {
            error_message = "No status for VV command";
            return null;
        }

        if (!testChecksun(lines.elementAt(1))) {
            error_message = "Status for VV command failed checksum";
            return null;
        }
        String status = lines.elementAt(1).substring(0,
                lines.elementAt(1).length() - 1);

        if (!status.equals("00")) {
            error_message = "Status for VV command: " + status;
            return null;
        }

        for (int i = 2; i < lines.size(); i++) {
            String line = lines.elementAt(i);
            line = line.replaceFirst(";", "");
            if (!ch.testChecksun(line)) {
                error_message = "VV checksum failed at " + i;
                return null;
            }

            line = line.substring(0, line.length() - 1);
            if (line.startsWith("VEND:")) {
                info.vendor = line.substring(5);
            }
            if (line.startsWith("PROD:")) {
                info.product = line.substring(5);
            }
            if (line.startsWith("FIRM:")) {
                info.firmware = line.substring(5);
            }
            if (line.startsWith("PROT:")) {
                info.protocol = line.substring(5);
            }
            if (line.startsWith("SERI:")) {
                info.serial_number = line.substring(5);
            }
        }

        return info;
    }

    public RangeSensorInternalInformation getSensorInternalInformation() {
        CaptureHandler ch = new CaptureHandler(myConnection);
        RangeSensorInternalInformation intinfo = new RangeSensorInternalInformation();
        String command = "II\n";
        ch.send(command);
        Vector<String> lines = ch.receiveAnswer();

        if (lines.size() == 0) {
            error_message = "No response for II command";
            return null;
        }
        command = command.substring(0, command.length() - 1);
        if (!lines.elementAt(0).equals(command)) {
            error_message = "No echo for II command";
            return null;
        }

        if (lines.size() < 2) {
            error_message = "No status for II command";
            return null;
        }

        if (!testChecksun(lines.elementAt(1))) {
            error_message = "Status for II command failed checksum";
            return null;
        }
        String status = lines.elementAt(1).substring(0,
                lines.elementAt(1).length() - 1);

        if (!status.equals("00")) {
            error_message = "Status for II command: " + status;
            return null;
        }

        for (int i = 2; i < lines.size(); i++) {
            String line = lines.elementAt(i);
            line = line.replace(";", "");
            if (!ch.testChecksun(line)) {
                error_message = "II checksum failed at " + i;
                return null;
            }

            line = line.substring(0, line.length() - 1);
            if (line.startsWith("MODL:")) {
                intinfo.model = line.substring(5);
            }
            if (line.startsWith("LASR:")) {
                intinfo.laserStatus = line.substring(5);
            }
            if (line.startsWith("SCSP:")) {
                intinfo.motorDesiredSpeed = line.substring(5);
            }
            if (line.startsWith("MESM:")) {
                intinfo.stateID = line.substring(5);
            }
            if (line.startsWith("SBPS:")) {
                intinfo.communicationType = line.substring(5);
            }
            if (line.startsWith("TIME:")) {
                intinfo.internalTime = line.substring(5);
            }
            if (line.startsWith("STAT:")) {
                intinfo.sensorSituation = line.substring(5);
            }
        }

        return intinfo;
    }
}

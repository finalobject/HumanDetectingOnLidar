/**
 *
 */
package com.kristou.urgLibJ.RangeSensor.Capture;

import java.util.Vector;

import com.kristou.urgLibJ.Connection.Connection;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureData.Step;

/**
 * @author Administrator
 *
 */
public class ND_Capture implements Capture {

    private CaptureSettings settings = null;
    CaptureHandler myCaptureHandler = new CaptureHandler();
    private String error_message = "";
    private int dataSize = 3;
    private boolean isStarted = false;

    public ND_Capture(Connection con) {
        myCaptureHandler.setConnection(con);
    }

    /* (non-Javadoc)
     * @see com.kristou.urgLibJ.RangeSensor.Capture.Capture#createCaptureCommand()
     */
    @Override
    public String createCaptureCommand() {
        if (settings == null) {
            error_message = "No setting defined";
            return null;
        }

        String command = String.format("ND%04d%04d%02d%01d%02d\n",
                settings.capture_first, settings.capture_last,
                settings.skip_lines, settings.frame_interval,
                settings.capture_times > 99 ? 0 : settings.capture_times);
        return command;
    }

    /* (non-Javadoc)
     * @see com.kristou.urgLibJ.RangeSensor.Capture.Capture#capture()
     */
    @Override
    public CaptureData capture() {
        if (!isStarted) {
            error_message = "Start capture before capturing";
            return null;
        }

        Vector<String> resultLines = myCaptureHandler.receiveAnswer();

        CaptureData data = new CaptureData();

        if (resultLines.size() == 0) {
            error_message = "No answer from sensor: " + myCaptureHandler.what();
            return null;
        }

        String command = createCaptureCommand();
        if (command == null) {
            return null;
        }
        command = command.substring(0, command.length() - 1);
        data.command = command;
        if (!data.command.equals(command)) {
            error_message = "Echo back is incorrect. received[" + data.command
                    + "] sent[" + command + "]";
            return null;
        }

        if (resultLines.size() < 2) {
            error_message = "No Capture status";
            return null;
        }
        if (!myCaptureHandler.testChecksun(resultLines.elementAt(1))) {
            error_message = "Capture status checksum failed";
            return null;
        }
        data.status = resultLines.elementAt(1).substring(0,
                resultLines.elementAt(1).length() - 1);
        if (!data.status.equals("99")) {
            error_message = "Capture failed with error number \"" + data.status
                    + "\"";
            return null;
        }

        if (resultLines.size() < 3) {
            error_message = "No Capture timestamp";
            return null;
        }

        if (!myCaptureHandler.testChecksun(resultLines.elementAt(2))) {
            error_message = "Capture timestamp checksum failed";
            return null;
        }
        String timestamp = resultLines.elementAt(2).substring(0,
                resultLines.elementAt(1).length() - 1);
        data.timestamp = myCaptureHandler.decode(timestamp);

        if (resultLines.size() > 3) {
            String remaining = "";
            Step step = null;
            for (int i = 3; i < resultLines.size(); i++) {
                String line = resultLines.elementAt(i);
                if (!myCaptureHandler.testChecksun(line)) {
                    error_message = "Capture data checksum failed at line \""
                            + i + "\"";
                    return null;
                }
                line = remaining + line.substring(0, line.length() - 1);
                while (line.length() >= dataSize) {
                    String dataPeiece = line.substring(0, dataSize);
                    if (dataPeiece.charAt(0) == '&') {
                        if (line.length() > dataSize) {
                            dataPeiece = line.substring(1, dataSize + 1);
                            line = line.substring(dataSize + 1);
                        } else {
                            break;
                        }
                    } else {
                        step = data.new Step();
                        line = line.substring(dataSize);
                        data.steps.add(step);
                    }

//					// ////////////
//					System.out.printf("dataPeiece: " + dataPeiece + " size: "
//							+ dataPeiece.length());
//					System.out.println();
//					// ////////////
                    long dataValue = myCaptureHandler.decode(dataPeiece);
                    step.distances.add(dataValue);

                }
                remaining = line;
            }
            if (remaining.length() > 0) {
                System.out.printf("Data remaining: " + remaining + " size: "
                        + remaining.length());
                System.out.println();
            }
        }

        return data;
    }

    /* (non-Javadoc)
     * @see com.kristou.urgLibJ.RangeSensor.Capture.Capture#setCapturesSize(int)
     */
    @Override
    public void setCapturesSize(int size) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.kristou.urgLibJ.RangeSensor.Capture.Capture#capturesSize()
     */
    @Override
    public int capturesSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.kristou.urgLibJ.RangeSensor.Capture.Capture#remainCaptureTimes()
     */
    @Override
    public int remainCaptureTimes() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.kristou.urgLibJ.RangeSensor.Capture.Capture#setSettings(com.kristou.urgLibJ.RangeSensor.Capture.CaptureSettings)
     */
    @Override
    public void setSettings(CaptureSettings s) {
        settings = s;

    }

    /* (non-Javadoc)
     * @see com.kristou.urgLibJ.RangeSensor.Capture.Capture#what()
     */
    @Override
    public String what() {
        return error_message;
    }

    @Override
    public boolean start() {
        String command = createCaptureCommand();
        isStarted = false;
        if (command == null) {
            return false;
        }
        if (!myCaptureHandler.send(command)) {
            error_message = "Sending command failed: " + command + " -> " + myCaptureHandler.what();
            return false;
        }

        Vector<String> resultLines = myCaptureHandler.receiveAnswer();

        CaptureData data = new CaptureData();

        if (resultLines.size() == 0) {
            error_message = "No answer from sensor: " + myCaptureHandler.what();
            return false;
        }

        command = command.substring(0, command.length() - 1);
        data.command = command;
        if (!data.command.equals(command)) {
            error_message = "Echo back is incorrect. received[" + data.command
                    + "] sent[" + command + "]";
            return false;
        }

        if (resultLines.size() < 2) {
            error_message = "No Capture status";
            return false;
        }
        if (!myCaptureHandler.testChecksun(resultLines.elementAt(1))) {
            error_message = "Capture status checksum failed";
            return false;
        }
        data.status = resultLines.elementAt(1).substring(0,
                resultLines.elementAt(1).length() - 1);
        if (!data.status.equals("00")) {
            error_message = "Capture failed with error number \"" + data.status
                    + "\"";
            return false;
        }
        isStarted = true;
        return true;
    }

    @Override
    public boolean stop() {
        boolean result = myCaptureHandler.setLaserOutput(CaptureHandler.Off);
        isStarted = false;
        return result;
    }

}

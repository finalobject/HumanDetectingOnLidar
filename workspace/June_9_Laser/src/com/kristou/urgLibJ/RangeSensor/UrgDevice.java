package com.kristou.urgLibJ.RangeSensor;

import com.kristou.urgLibJ.Connection.Connection;
import com.kristou.urgLibJ.RangeSensor.Capture.Capture;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureData;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureFactory;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureHandler;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureSettings;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureSettings.CaptureMode;

public class UrgDevice implements RangeSensor {

    private String error_message = "";
    private Connection myConnection = null;
    private CaptureSettings myCaptureSettings = new CaptureSettings();
    private RangeSensorParameter myParameter = null;
    private RangeSensorInformation myInformation = null;
    private RangeSensorInternalInformation myInternalInformation = null;
    private Capture myCapture = null;

    public UrgDevice() {
    }

    public UrgDevice(Connection con) {
        setConnection(con);
    }

    @Override
    public String what() {
        return error_message;
    }

    @Override
    public boolean connect(String device, int baudrate) {
        boolean connected = myConnection.connect(device, baudrate);
        if (connected) {
            if (!initSensor()) {
                error_message = "Could not initialize the connection.";
                disconnect();
                return false;
            }
        } else {
            error_message = myConnection.what();
        }
        return connected;
    }

    private boolean initSensor() {
        CaptureHandler ch = new CaptureHandler(myConnection);
        if (!ch.sendQT()) {
            ch.sendSCIP2();
        }
        myParameter = ch.getSensorParameter();
        if (myParameter != null) {
            setStartStep(myParameter.area_min);
            setEndStep(myParameter.area_max);
            setSkipLines(1);
            setFrameInterval(0);
            setCaptureTimes(0);
            setDataByte(3);
        } else {
            error_message = "Sensor parameter failed -> " + ch.what();
            return false;
        }
        return true;
    }

    private void setDataByte(int b) {
        myCaptureSettings.data_byte = b;
    }

    @Override
    public boolean connect(String device) {
        boolean connected = myConnection.connect(device);
        if (connected) {
            if (!initSensor()) {
                disconnect();
                return false;
            }
        }else{
            error_message = myConnection.what();
        }
        return connected;
    }

    public void setCaptureTimes(int times) {
        myCaptureSettings.capture_times = times;
    }

    @Override
    public void setConnection(Connection con) {
        myConnection = con;
    }

    @Override
    public Connection getConnection() {
        return myConnection;
    }

    @Override
    public void disconnect() {
        if (myConnection.isConnected()) {
            myConnection.disconnect();
        }
    }

    @Override
    public boolean isConnected() {
        return myConnection.isConnected();
    }

    public boolean setCaptureMode(CaptureMode mode) {
        myCaptureSettings.type = mode;
        myCapture = CaptureFactory.makeCapture(myCaptureSettings, myConnection);
        return myCapture != null;
    }

    @Override
    public void setStartStep(int startStep) {
        myCaptureSettings.capture_first = startStep;
    }

    @Override
    public void setEndStep(int endStep) {
        myCaptureSettings.capture_last = endStep;
    }

    @Override
    public void setSkipLines(int lines) {
        myCaptureSettings.skip_lines = lines;
    }

    @Override
    public void setFrameInterval(int interval) {
        myCaptureSettings.frame_interval = interval;
    }

    @Override
    public CaptureData capture() {
        CaptureData data = myCapture.capture();
        if (data == null) {
            error_message = "Error Capture: " + myCapture.what();
        }
        return data;
    }

    @Override
    public boolean setTimestamp(int timestamp, int response_msec,
            int force_delay_msec) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean setTimestamp(int timestamp, int response_msec) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean setTimestamp(int timestamp) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long recentTimestamp() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean setLaserOutput(boolean on) {
        CaptureHandler ch = new CaptureHandler(myConnection);
        return ch.setLaserOutput(on);
    }

    @Override
    public double index2rad(int index) {
        int index_from_front = index - myParameter.area_front;
        return index_from_front * (2.0 * Math.PI) / myParameter.area_total;
    }

    @Override
    public int index2deg(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int rad2index(double radian) {
        int area_total = myParameter.area_total;
        int index
                = (int) (((radian * area_total) / (2.0 * Math.PI)) + 0.5)
                + myParameter.area_front;

        if (index < 0) {
            index = 0;
        } else if (index > myParameter.area_max) {
            index = myParameter.area_max;
        }
        return index;
    }

    @Override
    public int deg2index(int degree) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setParameter(RangeSensorParameter parameter) {
        myParameter = parameter;
    }

    @Override
    public RangeSensorParameter getParameter() {
        if (myParameter == null) {
            CaptureHandler ch = new CaptureHandler(myConnection);
            myParameter = ch.getSensorParameter();
            return myParameter;
        } else {
            return myParameter;
        }

    }

    @Override
    public RangeSensorInformation getInformation() {
        if (myInformation == null) {
            CaptureHandler ch = new CaptureHandler(myConnection);
            myInformation = ch.getSensorInformation();
            if(myInformation == null){
                error_message = ch.what();
            }
            return myInformation;
        } else {
            return myInformation;
        }
    }

    @Override
    public RangeSensorInternalInformation getInternalInformation() {
        if (myInternalInformation == null) {
            CaptureHandler ch = new CaptureHandler(myConnection);
            myInternalInformation = ch.getSensorInternalInformation();
            return myInternalInformation;
        } else {
            return myInternalInformation;
        }
    }

    public boolean startCapture() {
        if (myCapture == null) {
            error_message = "No capture mode is set";
            return false;
        } else {
            boolean result = myCapture.start();
            if (!result) {
                error_message = "Error starting capture -> " + myCapture.what();
            }
            return result;
        }
    }

    public boolean stopCapture() {
        if (myCapture == null) {
            error_message = "No capture mode is set";
            return false;
        } else {
            boolean result = myCapture.stop();
            if (!result) {
                error_message = "Error stopping capture -> " + myCapture.what();
            }
            return result;
        }
    }

}

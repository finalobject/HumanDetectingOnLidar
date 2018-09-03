/**
 *
 */
package com.kristou.urgLibJ.RangeSensor;

import com.kristou.urgLibJ.Connection.Connection;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureData;

/**
 * @author Administrator
 *
 */
public interface RangeSensor {

    public String what();

    public boolean connect(String device, int baudrate);

    public boolean connect(String device);

    public void setConnection(Connection con);

    public Connection getConnection();

    public void disconnect();

    public boolean isConnected();

    public void setStartStep(int startStep);

    public void setEndStep(int endStep);

    public CaptureData capture();

    public boolean setTimestamp(int timestamp, int response_msec, int force_delay_msec);

    public boolean setTimestamp(int timestamp, int response_msec);

    public boolean setTimestamp(int timestamp);

    public long recentTimestamp();

    public boolean setLaserOutput(boolean on);

    public double index2rad(int index);

    public int index2deg(int index);

    public int rad2index(double radian);

    public int deg2index(int degree);

    public void setParameter(RangeSensorParameter parameter);

    public RangeSensorParameter getParameter();

    public RangeSensorInformation getInformation();

    public RangeSensorInternalInformation getInternalInformation();

    void setSkipLines(int lines);

    void setFrameInterval(int interval);
}

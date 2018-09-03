/**
 *
 */
package com.kristou.urgLibJ.RangeSensor.Capture;

/**
 * @author Administrator
 *
 */
public interface Capture {

    public String createCaptureCommand();

    public boolean start();

    public boolean stop();

    public CaptureData capture();

    public void setCapturesSize(int size);

    public int capturesSize();

    public int remainCaptureTimes();

    public void setSettings(CaptureSettings s);

    public String what();
}

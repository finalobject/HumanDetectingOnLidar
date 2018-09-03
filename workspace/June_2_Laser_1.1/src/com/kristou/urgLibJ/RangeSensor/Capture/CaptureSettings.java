/**
 *
 */
package com.kristou.urgLibJ.RangeSensor.Capture;

/**
 * @author Administrator
 *
 */
public class CaptureSettings {

    public enum CaptureMode {

        Unknow_Capture_mode, // !< sequential acquisition
        GD_Capture_mode, // !< sequential acquisition
        GE_Capture_mode, // !< sequential acquisition
        HD_Capture_mode, // !< sequential acquisition
        HE_Capture_mode, // !< sequential acquisition
        MD_Capture_mode, // !< sequential acquisition
        ME_Capture_mode, // !< sequential acquisition
        ND_Capture_mode, // !< sequential acquisition
        NE_Capture_mode, // !< sequential acquisition
    };

    public CaptureMode type; // !< Type of receive data
    public int error_code; // !< Error code
    public long timestamp; // !< [msec]
    public int capture_first; // !< Acquisition beginning index
    public int capture_last; // !< Acquisition end index
    public int skip_lines; // !< Number of lines to skip
    public int frame_interval; // !< Data acquisition interval
    public int remain_times; // !< Remaining number of scans
    public int capture_times;
    public int data_byte; // !< Number of data bytes
}

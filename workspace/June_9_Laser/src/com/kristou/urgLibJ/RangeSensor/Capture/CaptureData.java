package com.kristou.urgLibJ.RangeSensor.Capture;

import java.util.Vector;

public class CaptureData {

    public class Step {

        public Vector<Long> distances = new Vector<Long>();
        public Vector<Long> intensities = new Vector<Long>();
    }
    public String command;
    public String status;
    public long timestamp;
    public Vector<Step> steps = new Vector<Step>();
}

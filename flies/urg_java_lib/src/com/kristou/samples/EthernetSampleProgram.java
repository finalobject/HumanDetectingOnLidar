/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kristou.samples;

import com.kristou.urgLibJ.Connection.EthernetConnection;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureData;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureSettings;
import com.kristou.urgLibJ.RangeSensor.RangeSensorInformation;
import com.kristou.urgLibJ.RangeSensor.UrgDevice;

/**
 *
 * @author kristou
 */
public class EthernetSampleProgram {

    public static void main(String[] args) {
        //Create an UrgDevice with the ethernet connection
        UrgDevice device = new UrgDevice(new EthernetConnection());
        int times = 20;

        // Connect to the sensor
        if (device.connect("192.168.0.10")) {
            System.out.println("connected");

            //Get the sensor information
            RangeSensorInformation info = device.getInformation();
            if(info != null){
            System.out.println("Sensor model:" + info.product);
            System.out.println("Sensor serial number:" + info.serial_number);
            }else{
                System.out.println("Sensor error:" + device.what());
            }

            //Set the continuous capture type, Please refer to the SCIP protocol for further details
            device.setCaptureMode(CaptureSettings.CaptureMode.MD_Capture_mode);

            //We set the capture type to a continuous mode so we have to start the capture
            device.startCapture();

            for (int i = 0; i < times; i++) {
                //Data reception happens when calling capture
                CaptureData data = device.capture();
                if(data != null) {
                    System.out.println("Scan " + (i + 1) + ", stesps " + data.steps.size());
                } else {
                    System.out.println("Sensor error:" + device.what());
                }
            }

            //Stop the cature
            device.stopCapture();

            //Disconnect from the sensor
            device.disconnect();

        } else {
            System.out.println("not connected: " + device.what());
        }
    }
}

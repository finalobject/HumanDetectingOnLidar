import java.util.Scanner;

import com.kristou.urgLibJ.Connection.EthernetConnection;
import com.kristou.urgLibJ.RangeSensor.RangeSensorInformation;
import com.kristou.urgLibJ.RangeSensor.UrgDevice;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureData;
import com.kristou.urgLibJ.RangeSensor.Capture.CaptureSettings;

public class RadarReader extends Thread{
	public CaptureData capData;
	public UrgDevice device;
	public RadarReader(){
		device = new UrgDevice(new EthernetConnection());
		device.connect("192.168.0.10");
		RangeSensorInformation info = device.getInformation();
        if(info != null){
        System.out.println("Sensor model:" + info.product);
        System.out.println("Sensor serial number:" + info.serial_number);
        }else{
            System.out.println("Sensor error:" + device.what());
        }
        device.setCaptureMode(CaptureSettings.CaptureMode.MD_Capture_mode);
        //We set the capture type to a continuous mode so we have to start the capture
        device.startCapture();
	}
	public void run(){
		boolean flag=true;
		while(flag){
			capData= device.capture();
            //System.out.println(capData.steps.get(0).distances.get(0)+";");
           // System.out.println(); 	
        	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public CaptureData getDataFromRadar(){
		while(capData==null){
			capData= device.capture();
            //System.out.print(data.steps.get(0).distances.get(0)+";");
           // System.out.println(); 	
        	try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return capData;
	}
	public static void main(String args[]) throws InterruptedException{
		Scanner in = new Scanner(System.in);
		RadarReader a = new RadarReader();
		a.setName("reader");
		a.start();
		boolean tu=true;
		while(tu){
			in.nextLine();
			System.out.println(a.getDataFromRadar().steps.get(0).distances.get(0));
		}
		
	}
}

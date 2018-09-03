import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import com.kristou.urgLibJ.RangeSensor.Capture.CaptureData;

public class SocketForRadar {
	public static void main(String[] args) {
		String ServerIpAddress = "192.168.1.105";// 对方的IP
		// String ServerIpAddress = "localhost";
		int PORT = 6666;
		DataOutputStream dos = null;
		BufferedReader br = null;
		DataInputStream dis = null;
		OutputStream outputstream = null;
		InputStream inputstream = null;
		byte[] serverSay = new byte[100000];
		int len;
		String tmp = null;
		InputStreamReader read = new InputStreamReader(System.in);
		int[] depths = null;
		Scanner in = new Scanner(System.in);
		RadarReader a = new RadarReader();
		a.setName("reader");
		a.start();
		int[] same = new int[1081];
		for (int i = 0; i < 1081; i++) {
			same[i] = i;
		}
		try {
			Socket client = new Socket(InetAddress.getByName(ServerIpAddress), PORT);
			System.out.println("Connect!");
			while (true) {
				inputstream = client.getInputStream();
				len = inputstream.read(serverSay);// 接受服务器消息
				// System.out.println("Server:"+new String(serverSay, 0, len));

				if (serverSay[0] == 79 && serverSay[1] == 75) {
					outputstream = client.getOutputStream();
					depths = setDepthsFromRadar(a.getDataFromRadar());
					// outputstream.write(int2String(same).getBytes("gbk"));
					// System.out.println(int2String(depths));
					outputstream.write(int2String(depths).getBytes("gbk"));
					System.out.println("Command receive!");
				} else {
					System.out.println("NOT");
				}
				// outputstream=client.getOutputStream();
				// tmp = new BufferedReader(read).readLine();
				// outputstream.write(tmp.getBytes("gbk"));

			}
		} catch (Exception e) {
			System.out.println("fail to connect");
		}

	}

	public static String int2String(int[] a) {
		String str = "#";
		int length = a.length;
		for (int i = 0; i < length; i++) {

			str = str + a[i] + ";";

		}
		str = str + "#";
		return str;
	}

	public static int[] setDepthsFromRadar(CaptureData data) {

		// System.out.println(data.command);
		// System.out.println(data.status);
		// System.out.println(data.timestamp);
		int size = data.steps.size();
		int[] a = new int[size];
		for (int i = 0; i < size; i++) {

			a[i] = data.steps.get(i).distances.get(0).intValue();
		}
		return a;
	}
}

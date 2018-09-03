import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;

public class Read {
	int[] data;
	String PATH;
	LineNumberReader reader;

	public Read(String PATH) throws FileNotFoundException {
		this.PATH = PATH;
		reader = new LineNumberReader(new FileReader(new File(PATH)));
	}

	public ArrayList<Cell> getDataByText(){
		ArrayList<Cell> list = new ArrayList<Cell>();
		String[] tmp;
		try {
			while(!(reader.readLine().equals("[scan]"))){}
			tmp=reader.readLine().split(";");
		} catch (IOException e) {
			return null;
		};
		for(int i=0;i<tmp.length;i++){
			list.add(new Cell(Integer.parseInt(tmp[i]),0));
		}
		return list;
	}
}

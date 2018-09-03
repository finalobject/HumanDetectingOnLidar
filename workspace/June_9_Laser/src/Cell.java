
public class Cell {
	double depth;
	int flag;

	public Cell(double depth, int flag) {
		this.depth = depth;
		this.flag = flag;
	}

	@Override
	public Cell clone() {
		return new Cell(this.depth, this.flag);
	}

	
}

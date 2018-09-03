import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Data {
	private boolean isFlaged = false;
	ArrayList<Cell> depths;
	int maxGroup=0;

	//minThreshold=maxDepth/divisor
	int divisor;
	int accuracy;
	int powerOfMaxGroup;
	int powerOfVariance;

	
	public Data() {
		super();
		this.depths = new ArrayList<Cell>();
		this.divisor=10;
		this.accuracy=100;
		this.powerOfMaxGroup=0;
		this.powerOfVariance=100;
	}

	public Data(int divisor,int accuracy,int powerOfMaxGroup,int powerOfVariance) {
		this.divisor=divisor;
		this.accuracy=accuracy;
		this.powerOfMaxGroup=powerOfMaxGroup;
		this.powerOfVariance=powerOfVariance;
	}
	public boolean isFlaged() {
		return isFlaged;
	}
	public ArrayList<Cell> copy(ArrayList<Cell> source){
		ArrayList<Cell> another = new ArrayList<Cell>();
		int size=source.size();
		for(int i=0;i<size;i++){
			another.add(source.get(i).clone());
		}
		return another;
	}
	public void setFlaged(boolean isFlaged) {
		this.isFlaged = isFlaged;
	}

	public ArrayList<Cell> getDepths() {
		return depths;
	}

	public void setDepths(ArrayList<Cell> depths) {
		this.depths = depths;
	}
	public double getMaxDepth(){
		double maxDepth=depths.get(0).depth;
		for (int i =0;i<depths.size();i++){
			if(depths.get(i).depth>maxDepth)
				{maxDepth=depths.get(i).depth;}
		}
		return maxDepth;
	}
	public void generate(int size, int group, int range) {
		depths.removeAll(depths);
		isFlaged = false;
		Random rand = new Random();
		int groupBoundary[] = new int[group - 1];
		int tmp = 0, flag = 1;
		double tmpDouble = 0;
		for (int i = 0; i < group - 1; i++) {
			flag = 1;
			tmp = rand.nextInt(size - 1) + 1;
			for (int j = 0; j < i; j++) {
				if (tmp == groupBoundary[j]) {
					flag = 0;
				}
			}
			if (flag == 0) {
				i--;
			} else {
				groupBoundary[i] = tmp;
			}

		}
		Arrays.sort(groupBoundary);
		flag = 0;
		tmp = rand.nextInt(range);
		for (int i = 0; i < size; i++) {
			if (flag < group - 1 && i == groupBoundary[flag]) {			
				flag++;
				tmp = rand.nextInt(range);

			}

			tmpDouble = (double) tmp + rand.nextDouble();
			depths.add(new Cell(tmpDouble, 0));
		}
	}

	public int getDivisor() {
		return divisor;
	}

	public void setDivisor(int divisor) {
		this.divisor = divisor;
	}

	public int getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	public int getPowerOfMaxGroup() {
		return powerOfMaxGroup;
	}

	public void setPowerOfMaxGroup(int powerOfMaxGroup) {
		this.powerOfMaxGroup = powerOfMaxGroup;
	}

	public int getPowerOfVariance() {
		return powerOfVariance;
	}

	public void setPowerOfVariance(int powerOfVariance) {
		this.powerOfVariance = powerOfVariance;
	}

	public void display() {
		Cell cell;
		for (int i = 0; i < depths.size(); i++) {
			cell = depths.get(i);
			System.out.printf("No,%d,depth=%.2f,flag=%d\n", i + 1, cell.depth, cell.flag);
		}
	}

	public void cluster() {
		isFlaged = true;
		ArrayList<Cell> minList = null;
		double minKey=0;
		double tmpKey=0;
		int tmpMaxGroup;
		ArrayList<Cell> tmpList;
		
		double maxDepth=getMaxDepth();
		double threshold=maxDepth/divisor;
		for (int i=0;i<accuracy;i++){
			tmpMaxGroup=0;
			tmpKey=0;
			tmpList=this.copy(depths);
			
			for(int j=0;j<tmpList.size();j++){
				if(j==0){
					tmpList.get(j).flag=1;
				}else if(Math.abs(tmpList.get(j).depth-tmpList.get(j-1).depth)>=threshold){
						tmpList.get(j).flag=tmpList.get(j-1).flag+1;
				}else{
					tmpList.get(j).flag=tmpList.get(j-1).flag;
				}				
			}
			tmpMaxGroup=tmpList.get(tmpList.size()-1).flag;
			if(Math.abs(tmpList.get(0).depth-tmpList.get(tmpList.size()-1).depth)<threshold){
				tmpMaxGroup--;
				int index=tmpList.size()-1;
				int reallyTmp=0;
				while(true){
					if(index==tmpList.size()-1){
						reallyTmp=tmpList.get(index).flag;
						tmpList.get(index).flag=1;
					}else if(index==0){
						break;
					}
					else if(tmpList.get(index).flag==reallyTmp){
						tmpList.get(index).flag=1;
					}else {
						break;
					}
					index--;
				}
			}
			tmpKey=getVariance(tmpList)*powerOfVariance+tmpMaxGroup*powerOfMaxGroup;			
			if(i==0){
				minKey=tmpKey;
				minList=this.copy(tmpList);
				maxGroup=tmpMaxGroup;
			}else if(tmpKey<minKey)
			{
				minKey=tmpKey;
				minList=this.copy(tmpList);
				maxGroup=tmpMaxGroup;
			}
			threshold+=maxDepth/accuracy;

		}
		this.depths=minList;

	}

	public void unCluster() {
		isFlaged = false;
		maxGroup=0;
		for (int i=0;i<depths.size();i++){
			depths.get(i).flag=0;
		}
	}
	public double getVariance(ArrayList<Cell> testDepths) {
		double variance=0;
		class result{
			public double sum=0;
			public double average=0;
			public int size=0;
		}
		if (testDepths.get(0).flag==0){return 0;}
        ArrayList<result> answer = new ArrayList<result>();
        answer.add(new result());
        int recentIndex=0;
        for(int i=0;i<testDepths.size();i++){
        	if(recentIndex!=0&&testDepths.get(i).flag==1){
        		answer.get(0).size++;
        		answer.get(0).sum+=testDepths.get(i).depth;
        	}else
        	{
        		answer.get(recentIndex).size++;
        		answer.get(recentIndex).sum+=testDepths.get(i).depth;
        		if (i<testDepths.size()-1&&(testDepths.get(i+1).flag!=1)&&(testDepths.get(i).flag!=testDepths.get(i+1).flag)){
        			recentIndex++;
        			answer.add(new result());
        		}
        	}
        }
        for(int i =0;i<answer.size();i++){
        	answer.get(i).average=answer.get(i).sum/answer.get(i).size;
        }
        for(int i=0;i<testDepths.size();i++){
        	variance+=(testDepths.get(i).depth-answer.get(testDepths.get(i).flag-1).average)*
        			(testDepths.get(i).depth-answer.get(testDepths.get(i).flag-1).average);
        }
        return variance;
	}

}

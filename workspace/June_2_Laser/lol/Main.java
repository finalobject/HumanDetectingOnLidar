import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.JPanel;
public class Main {
	JFrame helloworld;
	Carol carol;
	Vera vera;
	String PATH = "E://JAVA//workspace//PocketMonster_Mar_19_2016//image//";

	JSlider ABAC;
	JSlider ABDivisor;
	JSlider ABPWM;
	JSlider ABPOV;
	JSlider ABSc;
	JSlider ABAg;

	public static void main(String[] args) {
		Main main = new Main();
		main.init();
	}

	void init() {
		Vera vera = new Vera();

		vera.generate(700, 5, 20);
		helloworld = new JFrame();
		carol = new Carol(vera);
		helloworld.add(carol);
		JButton btn = new JButton("聚类");
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				vera.bull();
				vera.display();
				helloworld.repaint();
			}

		});
		btn.setBounds(700, 0, 150, 40);

		JButton btn2 = new JButton("取消聚类");
		btn2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				vera.unCluster();
				helloworld.repaint();
			}

		});
		btn2.setBounds(700, 60, 150, 40);

		JButton btn3 = new JButton("随机生成");
		btn3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				vera.generate(700, 10, 15);
				helloworld.repaint();
			}

		});
		btn3.setBounds(700, 120, 150, 40);
		carol.setLayout(null);
		carol.add(btn);
		carol.add(btn2);
		carol.add(btn3);

		// 滑动条
		ABAC = new JSlider(1, 1000, 100);
		ABAC.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				vera.aC = slider.getValue();
				if (vera.isFlaged()) {
					vera.unCluster();
					vera.bull();
				}
				helloworld.repaint();
			}
		});
		ABAC.setBounds(700, 190,150, 40);
		carol.add(ABAC);

		ABDivisor = new JSlider(1, 80, 10);
		ABDivisor.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				vera.divisor = slider.getValue();
				helloworld.repaint();
				if (vera.isFlaged()) {
					vera.unCluster();
					vera.bull();
				}
				helloworld.repaint();
			}
		});
		ABDivisor.setBounds(700, 290,150, 40);
		carol.add(ABDivisor);

		ABPWM = new JSlider(0, 100, 0);
		ABPWM.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				vera.pWM = slider.getValue();
				vera.pOV = 100 - vera.pWM;
				ABPOV.setValue(100 - vera.pWM);
				helloworld.repaint();
				if (vera.isFlaged()) {
					vera.unCluster();
					vera.bull();
				}
				helloworld.repaint();
			}
		});
		ABPWM.setBounds(700, 370, 150, 40);
		carol.add(ABPWM);

		ABPOV = new JSlider(0, 100, 100);
		ABPOV.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				vera.pOV = slider.getValue();
				vera.pWM = 100 - vera.pOV;
				ABPWM.setValue(100 - vera.pOV);
				helloworld.repaint();
				if (vera.isFlaged()) {
					vera.unCluster();
					vera.bull();
				}
				helloworld.repaint();
			}
		});
		ABPOV.setBounds(700, 450, 150, 40);
		carol.add(ABPOV);

		ABSc = new JSlider(1, 300, 120);
		ABSc.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				carol.sc = slider.getValue();
				if (vera.isFlaged()) {
					vera.unCluster();
					vera.bull();
				}
				helloworld.repaint();
			}
		});
		ABSc.setBounds(700, 530,150, 40);
		carol.add(ABSc);
		
		ABAg = new JSlider(0, 360, 360);
		ABAg.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				carol.totalAg = slider.getValue();
				if (vera.isFlaged()) {
					vera.unCluster();
					vera.bull();
				}
				helloworld.repaint();
			}
		});
		ABAg.setBounds(700, 610, 150, 40);
		carol.add(ABAg);
		

		helloworld.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		helloworld.setResizable(false);
		helloworld.setTitle("Cluster");

		helloworld.pack();

		helloworld.setVisible(true);
	}

}
class Cell {
	double depth;
	// 组别，默认为0
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
class Vera {
	private boolean isFlaged = false;
	ArrayList<Cell> num;
	int maxGroup=0;
	int divisor=10;
	int aC=100;
	int pWM=0;
	int pOV=100;

	
	public Vera() {
		super();
		this.num = new ArrayList<Cell>();
	}

	public Vera(ArrayList<Cell> num) {
		this.num = num;
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

	public ArrayList<Cell> getNum() {
		return num;
	}

	public void setNum(ArrayList<Cell> num) {
		this.num = num;
	}
	public double getMaxDepth(){
		double md=num.get(0).depth;
		for (int i =0;i<num.size();i++){
			if(num.get(i).depth>md)
				{md=num.get(i).depth;}
		}
		return md;
	}
	public void generate(int size, int group, int range) {
		num.removeAll(num);
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
				// System.out.println("boundary:"+groupBoundary[flag]);
				flag++;
				tmp = rand.nextInt(range);

			}

			tmpDouble = (double) tmp + rand.nextDouble();
			// System.out.println("depth="+tmpDouble);
			num.add(new Cell(tmpDouble, 0));
		}
	}

	public void display() {
		Cell cell;
		for (int i = 0; i < num.size(); i++) {
			cell = num.get(i);
			System.out.printf("No,%d,depth=%.2f,flag=%d\n", i + 1, cell.depth, cell.flag);
		}
	}

	public void bull() {
		isFlaged = true;
		ArrayList<Cell> mL = null;
		double mk=0;
		double tk=0;
		int tmg;
		ArrayList<Cell> tL;
		
		double md=getMaxDepth();
		double ts=md/divisor;
		for (int i=0;i<aC;i++){
			tmg=0;
			tk=0;
			tL=this.copy(num);
			
			for(int j=0;j<tL.size();j++){
				if(j==0){
					tL.get(j).flag=1;
				}else if(Math.abs(tL.get(j).depth-tL.get(j-1).depth)>=ts){
						tL.get(j).flag=tL.get(j-1).flag+1;
				}else{
					tL.get(j).flag=tL.get(j-1).flag;
				}				
			}
			tmg=tL.get(tL.size()-1).flag;
			if(Math.abs(tL.get(0).depth-tL.get(tL.size()-1).depth)<ts){
				tmg--;
				int index=tL.size()-1;
				int reallyTmp=0;
				while(true){
					if(index==tL.size()-1){
						reallyTmp=tL.get(index).flag;
						tL.get(index).flag=1;
					}else if(index==0){
						break;
					}
					else if(tL.get(index).flag==reallyTmp){
						tL.get(index).flag=1;
					}else {
						break;
					}
					index--;
				}
			}
			tk=getVariance(tL)*pOV+tmg*pWM;			
			if(i==0){
				mk=tk;
				mL=this.copy(tL);
				maxGroup=tmg;
			}else if(tk<mk)
			{
				mk=tk;
				mL=this.copy(tL);
				maxGroup=tmg;
			}
			ts+=md/aC;

		}
		this.num=mL;

	}

	public void unCluster() {
		isFlaged = false;
		maxGroup=0;
		for (int i=0;i<num.size();i++){
			num.get(i).flag=0;
		}
	}
	public double getVariance(ArrayList<Cell> testNum) {
		double variance=0;
		class result{
			public double sum=0;
			public double average=0;
			public int size=0;
		}
		if (testNum.get(0).flag==0){return 0;}
        ArrayList<result> answer = new ArrayList<result>();

        answer.add(new result());
        int recentIndex=0;
        for(int i=0;i<testNum.size();i++){
        	if(recentIndex!=0&&testNum.get(i).flag==1){
        		answer.get(0).size++;
        		answer.get(0).sum+=testNum.get(i).depth;
        	}else
        	{
        		answer.get(recentIndex).size++;
        		answer.get(recentIndex).sum+=testNum.get(i).depth;
        		if (i<testNum.size()-1&&(testNum.get(i+1).flag!=1)&&(testNum.get(i).flag!=testNum.get(i+1).flag)){
        			recentIndex++;
        			answer.add(new result());
        		}
        	}
        }
        for(int i =0;i<answer.size();i++){
        	answer.get(i).average=answer.get(i).sum/answer.get(i).size;
        }
        for(int i=0;i<testNum.size();i++){
        	variance+=(testNum.get(i).depth-answer.get(testNum.get(i).flag-1).average)*
        			(testNum.get(i).depth-answer.get(testNum.get(i).flag-1).average);
        }
        return variance;
	}
	
}
class Carol extends JPanel{
	Vera vera;
	public int totalAg=360;
	public int sc=120;
	public Carol(){
		super();
	}
	public Carol(Vera vera){
		this.vera=vera;
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		double ag = (totalAg/360.0*2*Math.PI)/(vera.getNum().size());
		double x,y,x1,y1;
		Graphics2D g2 = (Graphics2D)g;
		double range=vera.getMaxDepth()*sc/100;
		//g2.setFont(new Font("Consolas", Font.PLAIN, 15));
		g2.drawString("精度", 700+2, 180);
		g2.drawString("最小阈值", 700+2, 260);
		g2.drawString("组权重", 700+2, 340);
		g2.drawString("方差权重", 700+2, 420);
		g2.drawString("大小", 700+2, 500);
		g2.drawString("角度", 700+2, 580);
		g2.drawString("Powered By", 775, 670);
		g2.setFont(new Font("Consolas", Font.BOLD, 20));

		g2.drawString("YullionStudio", 700+2, 690);
		g2.drawLine(0, 700/2, 700, 700/2);
		g2.drawLine(700/2,0,700/2,700);
		g2.drawLine(0,0,700,700);
		g2.drawLine(700,0,0,700);
		g2.drawLine(700, 0, 700, 700);
		g2.drawLine(700-1, 0, 700-1, 700);
		for(int i=0;i<9;i++){
			if(i<7){
				g2.drawOval(700/2-(i+1)*50, 700/2-(i+1)*50, (i+1)*100, (i+1)*100);
			}else
			{
				g2.drawArc(700/2-(i+1)*50, 700/2-(i+1)*50, (i+1)*100, (i+1)*100, 
						(int)(Math.acos(  (700/2) / ((double)(i+1)*50)  )/Math.PI*180)+1,
                        360-2*((int)(Math.acos(  (700/2) / ((double)(i+1)*50)  )/Math.PI*180)+1));
			}
		}
		g2.setColor(Color.BLUE);
		for(int i=0;i<vera.getNum().size();i++){
			x=(vera.getNum().get(i).depth)*Math.cos(ag*i)/range*(700/2)+700/2;
			y=(vera.getNum().get(i).depth)*Math.sin(ag*i)/range*(700/2)+700/2;

			g2.fillOval((int)(x), (int)(y),3, 3);
			
		}
		g2.setColor(Color.BLACK);
		
		
		 if(vera.isFlaged()==true){
			Cell before=null,now;
			for(int i=0;i<vera.getNum().size();i++){
				now=vera.getNum().get(i);
				
				if(i!=0&&before!=null&&now.flag==before.flag){
					x=(now.depth)*Math.cos(ag*i)/range*(700/2)+700/2;
					y=(now.depth)*Math.sin(ag*i)/range*(700/2)+700/2;
					x1=(before.depth)*Math.cos(ag*(i-1))/range*(700/2)+700/2;
					y1=(before.depth)*Math.sin(ag*(i-1))/range*(700/2)+700/2;
					g2.setColor(Color.RED);
					g2.drawLine((int)x, (int)y, (int)x1, (int)y1);
					g2.setColor(Color.BLACK);
				}
				before=now;
			}
			if(vera.num.get(0).flag==vera.num.get(vera.num.size()-1).flag){
				x=(vera.num.get(0).depth)*Math.cos(ag*0)/range*(700/2)+700/2;
				y=(vera.num.get(0).depth)*Math.sin(ag*0)/range*(700/2)+700/2;
				x1=(vera.num.get(vera.num.size()-1).depth)*Math.cos(ag*(-1))/range*(700/2)+700/2;
				y1=(vera.num.get(vera.num.size()-1).depth)*Math.sin(ag*(-1))/range*(700/2)+700/2;
				g2.setColor(Color.RED);
				g2.drawLine((int)x, (int)y, (int)x1, (int)y1);
				g2.setColor(Color.BLACK);
			}
		}
	}
	public Dimension getPreferredSize() {
		return new Dimension(700+150,700);
	}
}
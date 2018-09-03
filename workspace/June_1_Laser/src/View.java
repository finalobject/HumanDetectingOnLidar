import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;


import javax.swing.JPanel;

public class View extends JPanel{
	Data data;
	public int maxRange=10000;
	public final int DEFAULT_SIZE=700;
	public final int WIDTH_BUTTON=150;
	public int totalAngle;
	public int scale;
	public View(){
		super();
	}
	public View(Data data,int totalAngle,int scale){
		this.data=data;
		this.totalAngle=totalAngle;
		this.scale=scale;
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		double angle = (totalAngle/360.0*2*Math.PI)/(data.getDepths().size());
		double x,y,x1,y1;
		Graphics2D g2 = (Graphics2D)g;
		double range=maxRange*scale/100;
		g2.setFont(new Font("Consolas", Font.PLAIN, 15));
		g2.drawString("accuracy", DEFAULT_SIZE+2, 180);
		g2.drawString("divisor", DEFAULT_SIZE+2, 260);
		g2.drawString("powerOfMaxGroup", DEFAULT_SIZE+2, 340);
		g2.drawString("powerOfVariance", DEFAULT_SIZE+2, 420);
		g2.drawString("scale", DEFAULT_SIZE+2, 500);
		g2.drawString("angle", DEFAULT_SIZE+2, 580);
		g2.setFont(new Font("Consolas", Font.BOLD, 20));
		g2.drawString("YullionStudio", DEFAULT_SIZE+2, 680);
		g2.drawLine(0, DEFAULT_SIZE/2, DEFAULT_SIZE, DEFAULT_SIZE/2);
		g2.drawLine(DEFAULT_SIZE/2,0,DEFAULT_SIZE/2,DEFAULT_SIZE);
		g2.drawLine(0,0,DEFAULT_SIZE,DEFAULT_SIZE);
		g2.drawLine(DEFAULT_SIZE,0,0,DEFAULT_SIZE);
		g2.drawLine(DEFAULT_SIZE, 0, DEFAULT_SIZE, DEFAULT_SIZE);
		g2.drawLine(DEFAULT_SIZE-1, 0, DEFAULT_SIZE-1, DEFAULT_SIZE);
		//g2.drawLine(DEFAULT_SIZE-2, 0, DEFAULT_SIZE-2, DEFAULT_SIZE);
		//g2.drawLine(DEFAULT_SIZE-3, 0, DEFAULT_SIZE-3, DEFAULT_SIZE);
		for(int i=0;i<9;i++){
			if(i<7){
				g2.drawOval(DEFAULT_SIZE/2-(i+1)*50, DEFAULT_SIZE/2-(i+1)*50, (i+1)*100, (i+1)*100);
			}else
			{
				g2.drawArc(DEFAULT_SIZE/2-(i+1)*50, DEFAULT_SIZE/2-(i+1)*50, (i+1)*100, (i+1)*100, 
						(int)(Math.acos(  (DEFAULT_SIZE/2) / ((double)(i+1)*50)  )/Math.PI*180)+1,
                         360-2*((int)(Math.acos(  (DEFAULT_SIZE/2) / ((double)(i+1)*50)  )/Math.PI*180)+1));
				//System.out.println(((double)(i+1)*50)+"/"+(DEFAULT_SIZE/2));
			}
		}
		g2.setColor(Color.BLUE);
		for(int i=0;i<data.getDepths().size();i++){
			x=(data.getDepths().get(i).depth)*Math.cos(angle*i)/range*(DEFAULT_SIZE/2)+DEFAULT_SIZE/2;
			y=(data.getDepths().get(i).depth)*Math.sin(angle*i)/range*(DEFAULT_SIZE/2)+DEFAULT_SIZE/2;
			//System.out.println("x="+(int)(x-DEFAULT_SIZE/2)+" y="+(int)(y-DEFAULT_SIZE/2));

			g2.fillOval((int)(x), (int)(y),3, 3);
			
		}
		g2.setColor(Color.BLACK);
		
		 if(data.isFlaged()==true){
			Cell before=null,now;
			for(int i=0;i<data.getDepths().size();i++){
				now=data.getDepths().get(i);
				
				if(i!=0&&before!=null&&now.flag==before.flag){
					x=(now.depth)*Math.cos(angle*i)/range*(DEFAULT_SIZE/2)+DEFAULT_SIZE/2;
					y=(now.depth)*Math.sin(angle*i)/range*(DEFAULT_SIZE/2)+DEFAULT_SIZE/2;
					x1=(before.depth)*Math.cos(angle*(i-1))/range*(DEFAULT_SIZE/2)+DEFAULT_SIZE/2;
					y1=(before.depth)*Math.sin(angle*(i-1))/range*(DEFAULT_SIZE/2)+DEFAULT_SIZE/2;
					g2.setColor(Color.RED);
					g2.drawLine((int)x, (int)y, (int)x1, (int)y1);
					g2.setColor(Color.BLACK);
				}
				before=now;
			}
			if(data.depths.get(0).flag==data.depths.get(data.depths.size()-1).flag){
				x=(data.depths.get(0).depth)*Math.cos(angle*0)/range*(DEFAULT_SIZE/2)+DEFAULT_SIZE/2;
				y=(data.depths.get(0).depth)*Math.sin(angle*0)/range*(DEFAULT_SIZE/2)+DEFAULT_SIZE/2;
				x1=(data.depths.get(data.depths.size()-1).depth)*Math.cos(angle*(-1))/range*(DEFAULT_SIZE/2)+DEFAULT_SIZE/2;
				y1=(data.depths.get(data.depths.size()-1).depth)*Math.sin(angle*(-1))/range*(DEFAULT_SIZE/2)+DEFAULT_SIZE/2;
				g2.setColor(Color.RED);
				g2.drawLine((int)x, (int)y, (int)x1, (int)y1);
				g2.setColor(Color.BLACK);
			}
		}
	}
	public Dimension getPreferredSize() {
		return new Dimension(DEFAULT_SIZE+WIDTH_BUTTON,DEFAULT_SIZE);
	}
}

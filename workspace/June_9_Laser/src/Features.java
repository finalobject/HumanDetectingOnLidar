import java.util.ArrayList;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class Features {
	public Data data;
	public double angle;
	public double[] LOS;// Length Of Segment
	public double[] ROM2M;// Ratio Of Major To Minor axis of the ellipse
	public double[] MCCOS;// Mean Curvature Characteristic Of Segment
	public double[] ROD2N;// Ratio Of the Distance between laser source to the
							// centre of segmentation To Numbers of points

	public Features(Data data) {
		if (data.isFlaged() == false) {
			System.err.println("The data has not been cluster!");
		}
		this.data = data;
		angle = (270.0 / 360.0 * 2 * Math.PI) / (data.getDepths().size());
		LOS = new double[data.depths.get(data.depths.size() - 1).flag ];
		ROM2M = new double[data.depths.get(data.depths.size() - 1).flag ];
		MCCOS = new double[data.depths.get(data.depths.size() - 1).flag ];
		ROD2N = new double[data.depths.get(data.depths.size() - 1).flag ];
	}

	public Features(String dataString) {
		String[] tmp = dataString.split(";");
		ArrayList<Cell> list = new ArrayList<Cell>();
		for (int i = 0; i < tmp.length; i++) {
			list.add(new Cell(Integer.parseInt(tmp[i]), 0));
		}
		Data tmpData = new Data();

		tmpData.setDepths(list);
		tmpData.accuracy = 243;
		tmpData.powerOfMaxGroup = 83;
		tmpData.powerOfVariance = 17;
		tmpData.divisor = 177;
		tmpData.setFlaged(true);
		tmpData.cluster();
		this.data = tmpData;
		angle = (270.0 / 360.0 * 2 * Math.PI) / (data.getDepths().size());
		LOS = new double[data.depths.get(data.depths.size() - 1).flag ];
		ROM2M = new double[data.depths.get(data.depths.size() - 1).flag ];
		MCCOS = new double[data.depths.get(data.depths.size() - 1).flag ];
		ROD2N = new double[data.depths.get(data.depths.size() - 1).flag ];
	}

	public Features() {
		super();
	}

	public double[][] getXY(int[] depthsData) {
		double[][] xy = new double[depthsData.length][2];
		for (int i = 0; i < depthsData.length; i++) {
			xy[i][0] = (depthsData[i]) * Math.cos(-angle * i - Math.PI / 4 * 3 + Math.PI);
			xy[i][1] = (depthsData[i]) * Math.sin(-angle * i - Math.PI / 4 * 3 + Math.PI);
		}
		return xy;
	}

	public int[] getDepthsDataInArray(int flag) {
		int loc1 = -1, loc2 = -1;
		for (int i = 0; i < data.depths.size(); i++) {
			if (i == 1071) {
				loc1 = loc1 + 1 - 1;
			}
			if (data.depths.get(i).flag == flag && loc1 == -1) {
				loc1 = i;
			} else if (data.depths.get(i).flag == flag + 1) {
				loc2 = i - 1;
				break;
			} else if (i == data.depths.size() - 1) {
				loc2 = data.depths.size() - 1;
			}
		}
		int[] depthsData = new int[loc2 - loc1 + 1];
		for (int i = 0; i < depthsData.length; i++) {
			depthsData[i] = (int) data.depths.get(loc1 + i).depth;
		}
		return depthsData;
	}

	public double getLOS(double[][] xyData) {
		double localLOS = 0;
		double x1 = xyData[0][0];
		double y1 = xyData[0][1];
		double x2 = xyData[xyData.length-1][0];
		double y2 = xyData[xyData.length-1][1];
		localLOS = Math.sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
		return localLOS;
	}

	// continuing
	public double getROM2M(double[][] xyData) {
		double localROM2M = 0;
		return localROM2M;
	}

	public double getMCCOS(double[][] xyData) {
		int times=xyData.length-2;
		double x1=0,y1=0,x2=0,y2=0,x3=0,y3=0;
		double A=0,d1=0,d2=0,d3=0;
		double localMCCOS = 0;
		int conTime=0;
		if(times>0){
			for(int i=0;i<times;i++){
				x1=xyData[i][0];
				y1=xyData[i][1];
				x2=xyData[i+1][0];
				y2=xyData[i+1][1];
				x3=xyData[i+2][0];
				y3=xyData[i+2][1];
				A=Math.abs((x1*y2+y1*x3+x2*y3)-(x1*y3+y2*x3+y1*x2))/2;
				d1=Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
				d2=Math.sqrt((x2-x3)*(x2-x3)+(y2-y3)*(y2-y3));
				d3=Math.sqrt((x3-x1)*(x3-x1)+(y3-y1)*(y3-y1));
				if(d1==0||d2==0||d3==0){
					conTime++;
					System.out.println("continue!");
					continue;
				}
				//System.out.println(d1+","+d2+","+d3+","+A);
				localMCCOS+=4*A/(d1*d2*d3);
			}
		}
		if(times<=conTime)return 0;
		localMCCOS=localMCCOS/(times-conTime);
		return localMCCOS;
	}

	// continuing
	public double getROD2N(double[][] xyData) {
		double localROD2N = 0;
		return localROD2N;
	}

	public void getAllFeatures() {
		// getDepthsDataInArray,获取指定flag下的深度数据，返回的是数组
		// 返回的数组传入getXY，获得制定flag下的xy数据，返回二维数组，xy[][2]
		// 然后传入获取特征值函数，结果直接写入域中
	}

	public static void main(String[] args) {
		// frame3
		String a = "8717;8719;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;11068;11031;11020;10942;10830;10771;10745;10716;10700;10700;10711;10740;10769;10812;10835;10869;10893;10907;10918;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;12847;12815;12815;12771;12721;12693;12693;12693;12663;12462;12462;12475;12475;12413;12378;12288;12247;12192;12104;12040;12027;12027;12175;12206;12229;60000;60000;60000;60000;60000;60000;17702;17667;17631;17631;17663;17749;17841;17950;18042;18143;18220;18324;18447;18568;18632;18632;18547;18483;18417;18352;18312;18312;60000;60000;18395;18381;18358;18358;18269;18219;18156;18099;18056;17988;17934;17934;17898;17346;17339;17293;17250;17215;17137;17119;17061;17031;16990;16950;16894;16855;16804;16784;16735;16735;60000;60000;16931;16909;16881;16868;16814;16775;16761;16723;16699;16647;16595;16577;16538;9405;9386;9386;9362;9359;9349;9349;9330;9296;9290;9275;9268;9243;9228;9218;9191;9175;9165;9147;9146;9117;9110;9105;9089;9061;9055;9041;9033;9022;9017;8999;8999;9029;9194;9333;9472;9610;9768;9957;10118;10280;10454;10649;10839;11054;11267;11484;11673;11949;12228;12445;12703;13004;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;8638;8630;8630;8632;8632;8632;8622;8626;8625;8626;8630;8630;8636;8641;8641;8642;8644;8644;8642;8642;8651;8651;8651;8650;8652;8665;10012;10012;9973;9945;9925;9925;9936;9954;9960;9995;10117;17707;17721;17730;17736;17741;17780;17783;17783;9020;9020;9042;9049;9060;9067;9067;9049;8922;8836;8836;8853;8869;8872;8892;8892;8907;8919;8930;8934;8949;8964;8969;8984;8991;9002;9007;9039;9052;9067;9072;9080;9095;9119;9124;9131;9139;9169;9184;9192;9214;9218;9229;9248;9268;9290;9298;9329;9332;9359;9370;9385;9401;9431;9444;9458;9474;9503;9513;9530;9557;9574;9605;9609;9631;9658;9674;9699;9709;9745;9774;9785;9810;9835;9863;9883;9901;9937;9949;9972;10008;10032;10050;10090;10119;10124;10177;10193;10220;10251;10279;10321;10350;10373;10401;10423;10460;10506;10531;10564;10591;10644;10648;10691;10720;10764;10797;10832;10882;10914;10951;10980;11022;11044;11099;11128;11171;11210;11261;11285;11358;11382;11429;11458;11498;11555;11599;11656;11683;11727;11794;11858;11893;11945;11973;12054;12106;12157;12196;12279;12328;12373;12413;12498;12535;12609;12653;12718;12770;12835;12909;12976;12976;9437;9410;9359;9358;9308;9267;9236;9215;9178;9141;9109;9084;9049;9010;8987;8961;8925;8895;8870;8853;8806;8781;8746;8720;8689;8676;8643;8622;8592;8572;8545;8512;8507;8461;8448;8414;8412;8389;8352;8329;8315;8282;8272;8240;8229;8210;8176;8146;8144;8119;8096;8091;8052;8048;8028;8002;7984;7970;7953;7937;7924;7896;7892;7861;7842;7835;7814;7813;7802;7774;7765;7749;7726;7713;7699;7682;7681;7657;7655;7633;7617;7612;7605;7581;7571;7560;7553;7545;7519;7506;7491;7469;7433;7433;7416;7409;7392;7392;7494;7600;7644;9196;9196;9196;10677;10677;10655;10635;10617;10617;10616;10612;10612;60000;60000;12719;12703;12703;12693;12680;12655;12640;12640;12599;12584;12578;12537;12535;12510;12467;12467;12464;12411;12407;12385;12369;12346;12346;12319;12277;12277;12270;12268;12260;12255;12216;12216;12218;12218;12190;12176;12188;12176;12159;12140;12140;12155;60000;60000;60000;12172;12172;16547;16561;16557;16557;16557;16570;16570;16555;16555;16565;16565;16569;16566;16571;16566;16573;16574;16585;16585;16592;16590;16592;16611;16611;16611;16615;16624;16624;7282;7155;7086;7086;7101;7103;7113;7117;7141;7166;7172;7178;7183;7197;7197;7204;7206;7209;7218;7225;7233;7244;7247;7254;7262;7263;7263;7233;7214;7214;7307;7309;7323;7324;7324;7292;7215;7033;6844;6712;6614;6512;6443;6343;6258;6170;6077;5993;5908;5821;5766;5680;5606;5538;5470;5413;5333;5274;5220;5162;5103;5054;5002;4934;4886;4832;4785;4739;4685;4637;4598;4553;4516;4477;4426;4388;4348;4306;4269;4230;4187;4156;4117;4095;4053;4022;3980;3946;3914;3886;3861;3823;3800;3771;3748;3712;3681;3660;3628;3595;3571;3558;3524;3495;3468;3456;3428;3408;3381;3357;3330;3318;3289;3273;3254;3236;3211;3196;3174;3157;3149;3149;3156;3157;3173;3196;3198;3215;3229;3235;3246;3251;3257;3266;3299;3311;3320;3327;3345;3357;3375;3375;3386;3393;3414;3447;3447;3447;3466;3472;3483;3517;3529;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;2560;2560;2557;2552;2552;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;60000;1610;1591;1591;1581;1572;1569;1563;1563;1559;1568;1568;1568;1569;1572;1578;1580;1606;1675;1685;1690;1686;1686;1686;1679;1677;1672;1664;1577;1549;1549;1549;1548;1547;1544;1543;1540;1539;1538;1532;1530;1529;1525;1525;1527;1527;1524;1522;1522;1522;1523;1523;1519;1519;1517;1517;1515;1515;1515;1511;1510;1509;1506;1505;1496;1489;1471;1460;1053;964;897;844;823;807;787;781;775;765;765;763;761;761;762;763;763;763;763;763;765;766;765;766;766;768;768;772;765;766;766;771;773;776;776;771;771;771;771;771;776;777;776;762;760;760;770;771;771;765;765;766;767;767;758;758;754;754;748;748;748;748;742;742;742;741;740;735;735;735;711;694;694;694;692;692;692;684;684;685;688;688;688;688;689;690;691;691;691;695;695;695;696;701;731;743;763;2263;2263;2259;2255;2259;2266;2266;2269;2279;2279";

		Features fea1 = new Features(a);
		for(int i=0;i<33;i++){
			fea1.LOS[i]=fea1.getLOS(fea1.getXY(fea1.getDepthsDataInArray(i+1)));
			fea1.MCCOS[i]=fea1.getMCCOS(fea1.getXY(fea1.getDepthsDataInArray(i+1)));
		}
		System.out.println("-------");
		for(int i=12;i<19;i++){
			if(i==0){
				System.out.print(fea1.LOS[i]);
			}else{
				System.out.print(","+fea1.LOS[i]);
			}
		}
		System.out.println();
		for(int i=12;i<19;i++){
			if(i==0){
				System.out.print(fea1.MCCOS[i]);
			}else{
				System.out.print(","+fea1.MCCOS[i]);
			}
		}
		System.out.println();
		System.out.println("-------");/*
		svm_node[][] datas = new svm_node[33][2];
		double[] lables = new double[33];
		for(int i=0;i<33;i++){
			datas[i][0]=new svm_node();
			datas[i][1]=new svm_node();
			datas[i][0].index=0;
			datas[i][0].value=fea1.LOS[i];
			datas[i][1].index=-1;
			datas[i][1].value=fea1.MCCOS[i];
			
			lables[i]=-1.0;
		}
		lables[2]=1.0;
		lables[16]=1.0;
		lables[15]=1.0;
		lables[20]=1.0;
		lables[23]=1.0;
		lables[30]=1.0;
		lables[31]=1.0;
		
		
		svm_problem problem = new svm_problem();
		problem.l = 33; 
		problem.x = datas; 
		problem.y = lables; 
		svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.LINEAR;
		param.cache_size = 100;
		param.eps = 0.00001;
		param.C = 1;
		System.out.println(svm.svm_check_parameter(problem, param));
		svm_model model = svm.svm_train(problem, param); 
		
		//定义测试数据点c
		svm_node pc0 = new svm_node();
		pc0.index = 0;
		svm_node pc1 = new svm_node();
		pc1.index = -1;
		svm_node[] pc = {pc0, pc1};
		for(int i=0;i<33;i++){
			pc0.value = fea1.LOS[i];
			pc1.value = fea1.MCCOS[i];
			System.out.println(i+1+":"+svm.svm_predict(model, pc));
		}*/
	}
}

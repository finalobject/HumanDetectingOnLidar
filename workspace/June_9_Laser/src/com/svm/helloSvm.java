package com.svm;

import java.awt.Graphics;
import java.nio.channels.Pipe;
import java.util.StringTokenizer;
import java.util.Vector;



import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class helloSvm {
	
	public static void main(String[] args) {
		String cmd="-t 2 -c 100";
		double[] fea1 = {4382.975426174424,10449.038654869822,942.7782562674089,451.0902719614609,546.7742028227258,8073.75285023907,4087.479916424864,200.0};
		double[] fea2 ={0.004375960372540781,1.6666666674272983E-5,0.002940907253214825,0.0066231692003413355,0.0020384848081544584,0.004584420599555675,0.007077371632069125,0.1};
		double[][] features={fea1,fea2};
		double[] lables={0.0,0.0,0.0,1.0,1.0,0.0,0.0,0.0};
		Vector<point> point_list =new Vector<>(fea1.length);
		for(int i=0;i<fea1.length;i++){
			point_list.addElement(new point(fea1[i],fea2[i],0));
		}
		point_list.get(3).value=1.0;
		point_list.get(4).value=1.0;
		
		svm_model model = getModel(cmd, point_list);
		svm_node[] pc = {new svm_node(),new svm_node()};
		pc[0].index = 1;
		pc[1].index = 2;
		for(int i=0;i<fea1.length;i++){
			pc[0].value=fea1[i];
			pc[1].value=fea2[i];
			System.out.println(i+1+":"+svm.svm_predict(model,pc));
		}
	}
	static svm_model getModel(String args,Vector<point> point_list) {
		// guard
		svm_model model = new svm_model();

		svm_parameter param = new svm_parameter();

		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0;
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 40;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];

		// parse options
		StringTokenizer st = new StringTokenizer(args);
		String[] argv = new String[st.countTokens()];
		for (int i = 0; i < argv.length; i++)
			argv[i] = st.nextToken();

		for (int i = 0; i < argv.length; i++) {
			if (argv[i].charAt(0) != '-')
				break;
			if (++i >= argv.length) {
				System.err.print("unknown option\n");
				break;
			}
			switch (argv[i - 1].charAt(1)) {
			case 's':
				param.svm_type = Integer.parseInt(argv[i]);
				break;
			case 't':
				param.kernel_type = Integer.parseInt(argv[i]);
				break;
			case 'd':
				param.degree = Integer.parseInt(argv[i]);
				break;
			case 'g':
				param.gamma = Integer.parseInt(argv[i]);
				break;
			case 'r':
				param.coef0 = Integer.parseInt(argv[i]);
				break;
			case 'n':
				param.nu = Integer.parseInt(argv[i]);
				break;
			case 'm':
				param.cache_size = Integer.parseInt(argv[i]);
				break;
			case 'c':
				param.C = Integer.parseInt(argv[i]);
				break;
			case 'e':
				param.eps = Integer.parseInt(argv[i]);
				break;
			case 'p':
				param.p = Integer.parseInt(argv[i]);
				break;
			case 'h':
				param.shrinking = Integer.parseInt(argv[i]);
				break;
			case 'b':
				param.probability = Integer.parseInt(argv[i]);
				break;
			case 'w':
				++param.nr_weight; {
				int[] old = param.weight_label;
				param.weight_label = new int[param.nr_weight];
				System.arraycopy(old, 0, param.weight_label, 0, param.nr_weight - 1);
			}

			{
				double[] old = param.weight;
				param.weight = new double[param.nr_weight];
				System.arraycopy(old, 0, param.weight, 0, param.nr_weight - 1);
			}

				param.weight_label[param.nr_weight - 1] = Integer.parseInt(argv[i - 1].substring(2));
				param.weight[param.nr_weight - 1] = Integer.parseInt(argv[i]);
				break;
			default:
				System.err.print("unknown option\n");
			}
		}

		// build problem
		svm_problem prob = new svm_problem();
		prob.l = point_list.size();
		prob.y = new double[prob.l];

		if (param.kernel_type == svm_parameter.PRECOMPUTED) {
		} else if (param.svm_type == svm_parameter.EPSILON_SVR || param.svm_type == svm_parameter.NU_SVR) {
			if (param.gamma == 0)
				param.gamma = 1;
			prob.x = new svm_node[prob.l][1];
			for (int i = 0; i < prob.l; i++) {
				point p = point_list.elementAt(i);
				prob.x[i][0] = new svm_node();
				prob.x[i][0].index = 1;
				prob.x[i][0].value = p.x;
				prob.y[i] = p.y;
			}

			// build model & classify
			model = svm.svm_train(prob, param);
			


			
		} else {
			if (param.gamma == 0)
				param.gamma = 0.5;
			prob.x = new svm_node[prob.l][2];
			for (int i = 0; i < prob.l; i++) {
				point p = point_list.elementAt(i);
				prob.x[i][0] = new svm_node();
				prob.x[i][0].index = 1;
				prob.x[i][0].value = p.x;
				prob.x[i][1] = new svm_node();
				prob.x[i][1].index = 2;
				prob.x[i][1].value = p.y;
				prob.y[i] = p.value;
			}

			// build model & classify
			model = svm.svm_train(prob, param);



		}
		return model;
	}

}

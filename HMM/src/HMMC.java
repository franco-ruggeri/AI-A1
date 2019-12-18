import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class HMMC {
	
	public static void main(String[] args) throws FileNotFoundException {
		// generating model
		double[][] genA = {
				{0.7, 0.05, 0.25},
				{0.1, 0.8, 0.1},
				{0.2, 0.3, 0.5}
		};
		double[][] genB = {
				{0.7, 0.2, 0.1, 0},
				{0.1, 0.4, 0.3, 0.2},
				{0, 0.1, 0.2, 0.7}
		};
		double[] genPi = {1, 0, 0};
		HMM genHmm = new HMM(genPi, genA, genB, 3, 4);
		
		// initialize model
		// Q7
//		double[][] A = {
//				{0.54, 0.26, 0.20},
//				{0.19, 0.53, 0.28},
//				{0.22, 0.18, 0.6}
//		};
//		double[][] B = {
//				{0.5, 0.2, 0.11, 0.19},
//				{0.22, 0.28, 0.23, 0.27},
//				{0.19, 0.21, 0.15, 0.45}
//		};
//		double[] pi = {0.3, 0.2, 0.5};
//		HMM hmm = new HMM(pi, A, B, 3, 4);
		
		// Q8
//		HMM hmm = new HMM(3, 4);
		
		// Q9
		HMM hmm = new HMM(6, 4);
		
		// Q10.1
//		double[][] A = {
//				{1.0/3.0, 1.0/3.0, 1.0/3.0},
//				{1.0/3.0, 1.0/3.0, 1.0/3.0},
//				{1.0/3.0, 1.0/3.0, 1.0/3.0}
//		};
//		double[][] B = {
//				{1.0/4.0, 1.0/4.0, 1.0/4.0, 1.0/4.0},
//				{1.0/4.0, 1.0/4.0, 1.0/4.0, 1.0/4.0},
//				{1.0/4.0, 1.0/4.0, 1.0/4.0, 1.0/4.0}
//		};
//		double[] pi = {1.0/3.0, 1.0/3.0, 1.0/3.0};
//		HMM hmm = new HMM(pi, A, B, 3, 4);
		
		// Q10.2
//		double[][] A = {
//				{1, 0, 0},
//				{0, 1, 0},
//				{0, 0, 1}
//		};
//		double[][] B = {
//				{0.5, 0.2, 0.11, 0.19},
//				{0.22, 0.28, 0.23, 0.27},
//				{0.19, 0.21, 0.15, 0.45}
//		};
//		double[] pi = {0, 0, 1};
//		HMM hmm = new HMM(pi, A, B, 3, 4);
		
		// Q10.3
//		double[][] A = {
//				{0.69, 0.06, 0.25},
//				{0.2, 0.75, 0.05},
//				{0.15, 0.35, 0.5}
//		};
//		double[][] B = {
//				{0.65, 0.25, 0.05, 0.05},
//				{0.15, 0.35, 0.3, 0.2},
//				{0, 0.1, 0.25, 0.65}
//		};
//		double[] pi = {0.95, 0.05, 0};
//		HMM hmm = new HMM(pi, A, B, 3, 4);
//		System.err.println(hmm);
		
		// observations
		int[] O;
		try (Scanner s = new Scanner(new File("hmm_c_N1000.in"))) {
//		try (Scanner s = new Scanner(new File("hmm_c_N10000.in"))) {
			int T = s.nextInt();
			O = new int[T];
			for (int t=0; t<T; t++)
				O[t] = s.nextInt();
		}
		
		// learn hmm
		hmm.learn(O);
		System.out.println(hmm);
		System.out.println("Distance: " + 1/ (double) O.length * (hmm.evaluateLog(O) - genHmm.evaluateLog(O)));
	}

}

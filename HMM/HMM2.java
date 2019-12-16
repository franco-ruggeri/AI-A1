import java.util.Scanner;

public class HMM2 {

	public static void main(String[] args) {
		int N, K, observationSequence[];
		double A[][], B[][], pi[];
		HMM hmm;
		Scanner s = new Scanner(System.in);
		
		// read A
		N = s.nextInt(); s.nextInt();	// skip second one (== N)
		A = new double[N][N];
		for (int i=0; i<A.length; i++)
			for (int j=0; j<A[i].length; j++)
				A[i][j] = s.nextDouble();
		
		// read B
		s.nextInt(); K = s.nextInt(); 	// skip first one (== N)
		B = new double[N][K];
		for (int i=0; i<B.length; i++)
			for (int j=0; j<B[i].length; j++)
				B[i][j] = s.nextDouble();
		
		// read pi
		s.nextInt(); s.nextInt();		// skip first (== 1) and second (== N)
		pi = new double[N];
		for (int i=0; i<pi.length; i++)
			pi[i] = s.nextDouble();
		
		// create HMM
		hmm = new HMM(pi, A, B, N, K);
		
		// read observation sequence
		observationSequence = new int[s.nextInt()];
		for (int t=0; t<observationSequence.length; t++)
			observationSequence[t] = s.nextInt();
		
		// decode
		int[] stateSequence = hmm.decode(observationSequence);
		for (int state : stateSequence)
			System.out.print(state + " ");
		
		s.close();
	}

}

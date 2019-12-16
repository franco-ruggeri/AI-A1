import java.util.Scanner;

/**
 * Hidden Markov Model
 * 
 * No checks done for simplicity, but they should be added. The results are
 * unpredictable in case of wrong inputs.
 * 
 * @author fruggeri
 */
public class HMM {
	private double pi[], A[][], B[][];
	private int N, K;
	
	/**
	 * Constructs an HMM reading through a scanner.
	 */
	public HMM(Scanner s) {
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
	}
	
	/**
	 * Constructs an HMM with the given model parameters.
	 * 
	 * @param pi initial state distribution
	 * @param A transition matrix
	 * @param B output matrix
	 */
	public HMM(double[] pi, double[][] A, double[][] B) {
		this.pi = pi;
		this.A = A;
		this.B = B;
		this.N = A.length;
		this.K = B[0].length;
	}
	
	/**
	 * Computes the next observation distribution given the distribution of the
	 * current state.
	 * 
	 * @param currentStateDistribution
	 * @return next observation distribution
	 */
	public double[] nextObservationDistribution(double[] currentStateDistribution) {
		double[] result;
		result = Matrix.vectorByMatrix(currentStateDistribution, A);
		result = Matrix.vectorByMatrix(result, B);
		return result;
	}
	
	/**
	 * Computes the observation distribution O2 (first time step), so using the
	 * initial state distribution pi.
	 * 
	 * @return next observation distribution
	 */
	public double[] nextObservationDistribution() {
		return nextObservationDistribution(pi);
	}
	
	public double evaluate(int[] observationSequence) {
		double result, alpha[][], c[];
		int T = observationSequence.length;
		
		// forward algorithm
		alpha = new double[T][N];
		c = new double[T];
		forward(observationSequence, alpha, c);
		
		// compute result
		result = c[0];
		for (int t=1; t<T; t++)
			result *= c[t];
		result = 1 / result;
		return result;
	}
	
	private void forward(int[] O, double[][] alpha, double[] c) {
		int T = O.length;
		
		// compute alpha_0
		c[0] = 0;
		for (int i=0; i<N; i++) {
			alpha[0][i] = pi[i] * B[i][O[0]];
			c[0] += alpha[0][i];
		}
		
		// scale alpha_0
		c[0] = 1/c[0];
		for (int i=0; i<N; i++)
			alpha[0][i] *= c[0];
		
		// 0 < t < T
		for (int t=1; t<T; t++) {
			// compute alpha_t
			c[t] = 0;
			for (int i=0; i<N; i++) {
				alpha[t][i] = 0;
				for (int j=0; j<N; j++)
					alpha[t][i] += alpha[t-1][j] * A[j][i];
				alpha[t][i] *= B[i][O[t]];
				c[t] += alpha[t][i];
			}
			
			// scale alpha_t
			c[t] = 1/c[t];
			for (int i=0; i<N; i++)
				alpha[t][i] *= c[t];
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(Matrix.matrixToString(A));
		sb.append("\n");
		sb.append(Matrix.matrixToString(B));
		sb.append("\n");
		sb.append(Matrix.vectorToString(pi));
		return sb.toString();
	}
	
}

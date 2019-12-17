
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
	 * Constructs a HMM with the given model parameters. No checks are done for
	 * simplicity, but they should be added. The results are unpredictable in case
	 * of wrong inputs.
	 * 
	 * @param pi initial state distribution
	 * @param A  state transition matrix
	 * @param B  output matrix
	 * @param N  number of states
	 * @param K  number of output symbols
	 */
	public HMM(double[] pi, double[][] A, double[][] B, int N, int K) {
		this.N = N;
		this.K = K;
		this.pi = pi;
		this.A = A;
		this.B = B;
	}
	
	/**
	 * Constructs a HMM with good initialization to learn well.
	 * 
	 * @param N number of states
	 * @param K number of output symbols
	 */
	public HMM(int N, int K) {
		this.N = N;
		this.K = K;
		A = Matrix.randomRowStochastic(N, N);
		B = Matrix.randomRowStochastic(N, K);
		pi = new double[N];
		double[][] tmp = Matrix.randomRowStochastic(1, N);
		for (int i=0; i<N; i++)
			this.pi[i] = tmp[0][i];
	}

	/**
	 * Computes the probability distribution for O2 (t=2, second time step), so
	 * using the initial state distribution pi.
	 * 
	 * @return next observation distribution
	 */
	public double[] nextObservationDistribution() {
		double[] result;
		result = Matrix.vectorByMatrix(pi, A);
		result = Matrix.vectorByMatrix(result, B);
		return result;
	}
	
	/**
	 * Computes the probability distribution for the next observation OT+1, given an
	 * observation sequence O1:T (i.e. P(OT+1|O1:T)).
	 * 
	 * @return next observation distribution
	 */
	public double[] nextObservationDistribution(int[] observationSequence) {
		double[] result = new double[K];
		int T = observationSequence.length;

		// forward algorithm
		double[][] alpha = new double[T][N];
		double[] c = new double[T];
		forward(observationSequence, alpha, c);
		
		// compute result
		for (int k=0; k<K; k++) {
			result[k] = 0;
			for (int i=0; i<N; i++) {
				double tmp = 0;
				for (int j=0; j<N; j++)
					tmp += A[j][i] * alpha[T-1][j];
				tmp *= B[i][k];
				result[k] += tmp;
			}
		}
		return result;
	}
	
	/**
	 * Computes the log-probability of the observation sequence.
	 * 
	 * @param observationSequence observation sequence
	 * @return log-probability of observation sequence
	 */
	public double evaluate(int[] observationSequence) {
		double result, alpha[][], c[];
		int T = observationSequence.length;
		
		// forward algorithm
		alpha = new double[T][N];
		c = new double[T];
		forward(observationSequence, alpha, c);
		
		// compute result
		result = 0;
		for (int t=0; t<T; t++)
			result += Math.log(c[t]);
		result = -result;
		return Double.isFinite(result) ? result : Double.NEGATIVE_INFINITY;
	}
	
	/**
	 * Finds the most likely state sequence given the observations.
	 * 
	 * @param observationSequence observation sequence
	 * @return state sequence
	 */
	public int[] decode(int[] observationSequence) {
		int T = observationSequence.length;
		int[] stateSequence = new int[T];
		viterbi(observationSequence, stateSequence);
		return stateSequence;
	}
	
	/**
	 * Estimates the model parameters from the observation sequence.
	 * 
	 * @param observationSequence observation sequence
	 * @param maxIters maximum number of iterations
	 */
	public void learn(int[] observationSequence, int maxIters) {
		baumWelch(observationSequence, maxIters);
	}
	
	/**
	 * Forward algorithm (a.k.a. alpha-pass).
	 * 
	 * @param O observation sequence
	 * @param alpha preallocated matrix filled by the method
	 * @param c preallocated array filled by the method
	 */
	private void forward(int[] O, double[][] alpha, double[] c) {
		int T = O.length;
		
		// compute alpha_0
		c[0] = 0;
		for (int i=0; i<N; i++) {
			alpha[0][i] = pi[i] * B[i][O[0]];
			c[0] += alpha[0][i];
		}
		
		// scale alpha_0
		if (c[0] != 0)
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
			if (c[t] != 0)
				c[t] = 1/c[t];
			for (int i=0; i<N; i++)
				alpha[t][i] *= c[t];
		}
	}
	
	/**
	 * Viterbi algorithm.
	 * 
	 * @param O observation sequence
	 * @param stateSequence preallocated array filled by the method
	 * @return log probability of the most likely state sequence
	 */
	private double viterbi(int[] O, int[] stateSequence) {
		int T = O.length;
		double[][] delta = new double[T][N];
		int[][] deltaIdx = new int[T][N];
		double logProb = Double.NEGATIVE_INFINITY;
		
		// compute delta_0
		for (int i = 0; i < N; i++)
			delta[0][i] = Math.log(pi[i] * B[i][O[0]]);

		// compute delta_t, 0<t<T 
		for (int t = 1; t < T; t++) {
			for (int i = 0; i < N; i++) {
				// initialize using state 0
				delta[t][i] = delta[t - 1][0] + Math.log(A[0][i]) + Math.log(B[i][O[t]]);
				deltaIdx[t][i] = 0;

				// search max
				for (int j = 1; j < N; j++) {
					double tmp = delta[t - 1][j] + Math.log(A[j][i]) + Math.log(B[i][O[t]]);
					if (tmp > delta[t][i]) {
						delta[t][i] = tmp;
						deltaIdx[t][i] = j;
					}
				}
			}
		}

		// search last state
		stateSequence[T-1] = 0; // initialize to state 0
		for (int i = 1; i < N; i++) // search max delta
			if (delta[T-1][i] > delta[T-1][stateSequence[T-1]])
				stateSequence[T - 1] = i;

		// backtrack
		for (int t = T - 2; t >= 0; t--)
			stateSequence[t] = deltaIdx[t+1][stateSequence[t+1]];
		
		// compute log probability
		for (int i=0; i<N; i++)
			if (delta[T-1][i] > logProb)
				logProb = delta[T-1][i];
		return logProb;
	}
	
	/**
	 * Backward algorithm (a.k.a. beta-pass).
	 * 
	 * @param O observation sequence
	 * @param beta preallocated matrix filled by the method
	 * @param c scaling factors found with forward algorithm
	 */
	private void backward(int[] O, double[][] beta, double[] c) {
		int T = O.length;
		
		// compute beta_T-1 scaled by c_T-1
		for (int i=0; i<N; i++)
			beta[T-1][i] = c[T-1];
		
		// compute beta_t scaled by c_t, 0<=t<T-1
		for (int t=T-2; t>=0; t--) {
			for (int i=0; i<N; i++) {
				beta[t][i] = 0;
				for (int j=0; j<N; j++)
					beta[t][i] += A[i][j] * B[j][O[t+1]] * beta[t+1][j];
				beta[t][i] *= c[t];
			}
		}
	}
	
	/**
	 * Baum-Welch algorithm.
	 * 
	 * @param O observation sequence
	 * @param maxIters maximum number of iterations
	 */
	private void baumWelch(int[] O, int maxIters) {
		int T = O.length, iters = 0;
		double[] c = new double[T];
		double[][] alpha = new double[T][N];
		double[][] beta = new double[T][N];
		double[][] gamma = new double[T][N];
		double[][][] digamma = new double[T][N][N];
		double numer, denom, logProb = Double.NEGATIVE_INFINITY, oldLogProb;

		do {
			oldLogProb = logProb;

			// alpha-pass
			forward(O, alpha, c);

			// beta-pass
			backward(O, beta, c);

			// compute di-gamma and gamma
			for (int t = 0; t < T - 1; t++) {
				for (int i = 0; i < N; i++) {
					gamma[t][i] = 0;
					for (int j = 0; j < N; j++) {
						digamma[t][i][j] = alpha[t][i] * A[i][j] * B[j][O[t + 1]] * beta[t + 1][j];
						gamma[t][i] += digamma[t][i][j];
					}
				}
			}
			// special case for gammaT-1(i)
			for (int i = 0; i < N; i++)
				gamma[T - 1][i] = alpha[T - 1][i];

			// re-estimate pi
			for (int i = 0; i < N; i++)
				pi[i] = gamma[0][i];
			
			// re-estimate A
			for (int i = 0; i < N; i++) {
				denom = 0.0;
				for (int t = 0; t < T - 1; t++)
					denom += gamma[t][i];
				for (int j = 0; j < N; j++) {
					numer = 0.0;
					for (int t = 0; t < T - 1; t++)
						numer += digamma[t][i][j];
					A[i][j] = numer / denom;
				}
			}
			
			// re-estimate B
			for (int i = 0; i < N; i++) {
				denom = 0.0;
				for (int t = 0; t < T; t++)
					denom += gamma[t][i];
				for (int j = 0; j < K; j++) {
					numer = 0.0;
					for (int t = 0; t < T; t++)
						if (O[t] == j)
							numer += gamma[t][i];
					B[i][j] = numer / denom;
				}
			}

			// repeat?
			logProb = 0.0;
			for (int i = 0; i < T; i++)
				logProb += Math.log(c[i]);
			logProb = -logProb;
			iters++;
		} while (iters < maxIters && logProb > oldLogProb);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(Matrix.matrixToString(A));
		sb.append("\n");
		sb.append(Matrix.matrixToString(B));
		return sb.toString();
	}
	
}

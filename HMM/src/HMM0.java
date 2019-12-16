import java.util.Scanner;

public class HMM0 {

	public static void main(String[] args) {
		try (Scanner s = new Scanner(System.in)) {
			HMM hmm = new HMM(s);
//			System.out.println(hmm);
			System.out.println(Matrix.vectorToString(hmm.nextObservationDistribution()));
		}
	}

}

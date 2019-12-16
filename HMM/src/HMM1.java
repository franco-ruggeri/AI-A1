import java.util.Scanner;

public class HMM1 {

	public static void main(String[] args) {
		try (Scanner s = new Scanner(System.in)) {
			HMM hmm = new HMM(s);
			int[] observationSequence = new int[s.nextInt()];
			for (int t=0; t<observationSequence.length; t++)
				observationSequence[t] = s.nextInt();
//			System.out.println(hmm);
			System.out.println(hmm.evaluate(observationSequence));
		}
	}

}


/**
 * Abstract class providing utility functions for matrix algebra.
 * 
 * No checks done for simplicity, but they should be added. The results are
 * unpredictable in case of wrong inputs.
 * 
 * @author fruggeri
 */
public abstract class Matrix {
	
	public static double[] vectorByMatrix(double[] v, double[][] m) {
		double[] result = new double[m[0].length];
		for (int i=0; i<result.length; i++) {
			result[i] = 0;
			for (int j=0; j<v.length; j++)
				result[i] += v[j] * m[j][i]; 
		}
		return result;
	}
	
	public static String vectorToString(double[] v) {
		StringBuffer sb = new StringBuffer();
		sb.append("1 ").append(v.length).append(" ");
		for (int i=0; i<v.length; i++)
			sb.append(String.format("%.2f", v[i])).append(" ");
		return sb.toString();
	}
	
	public static String matrixToString(double[][] m) {
		StringBuffer sb = new StringBuffer();
		sb.append(m.length).append(" ").append(m[0].length).append(" ");
		for (int i=0; i<m.length; i++)
			for (int j=0; j<m[i].length; j++)
				sb.append(String.format("%.2f", m[i][j])).append(" ");
		return sb.toString();
	}
}

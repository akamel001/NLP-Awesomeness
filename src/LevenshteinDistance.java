
public class LevenshteinDistance {

	/**
	 * Compares two strings and finds the Levenshtein Distance. This implementation was borrowed 
	 * from the author listed below. A link has been used to reference the authors implementation 
	 * @param s First string
	 * @param t Second string
	 * @author Michael Gilleland (http://www.merriampark.com/ld.htm)
	 * @return Levenshtein distance between first and second string
	 */
	public int distance (String s, String t) {
		int d[][], n, m, i, j, s_i, t_j, cost; 

		// Step 1
		n = s.length ();
		m = t.length ();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n+1][m+1];

		// Step 2
		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}

		// Step 3
		for (i = 1; i <= n; i++) {

			s_i = s.charAt (i - 1);

			// Step 4
			for (j = 1; j <= m; j++) {

				t_j = t.charAt (j - 1);

				// Step 5
				if (s_i == t_j) {
					cost = 0;
				}
				else {
					cost = 1;
				}

				// Step 6
				d[i][j] = Minimum (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);

			}

		}

		// Step 7
		return d[n][m];

	}

	/**
	 * Helper function that returns minimum of three integers
	 * @param a
	 * @param b
	 * @param c
	 * @return min(a,b,c)
	 */
	private static int Minimum (int a, int b, int c) {
		int mi;

		mi = a;
		if (b < mi) {
			mi = b;
		}
		if (c < mi) {
			mi = c;
		}
		return mi;

	}
}

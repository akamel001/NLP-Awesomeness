import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import Jama.Matrix;

public class Homework4 {
	private final static String TERM_DOC_FILENAME = "term-100doc-matrix.txt";
	private final static int NUM_TERMS = 4916;
	private final static int NUM_DOCUMENTS = 100;
	
	public static HashMap<String, Integer> termIDs = new HashMap<String, Integer>();
	public static HashMap<String, Integer> shingleIDs = new HashMap<String, Integer>();
	public static HashMap<Integer, HashSet<Integer>> shingles = new HashMap<Integer, HashSet<Integer>>();
	public static HashMap<Integer, HashSet<Integer>> sketches = new HashMap<Integer, HashSet<Integer>>();
	public static Matrix termDocumentMatrixNorm;
	public static int nextShingleID = 0;
	
	public static ArrayList<Integer> aValues = new ArrayList<Integer>();
	public static ArrayList<Integer> bValues = new ArrayList<Integer>();
	
	static int prime = 24001;
	private static Matrix termDocumentMatrix;
	
	public static void main(String args[]) {
		minHash();
		experiments();
	}
	
	public static void experiments() {
		//Experiment A
		System.out.println("Experiment A");
		System.out.println("Creating the Term Document Matrix");
		createTermDocumentMatrix();
		System.out.println("Performing Bisections");
		
		//Perform the bisection runs
		ArrayList<ArrayList<Matrix>> results = new ArrayList<ArrayList<Matrix>>();
		for(int i = 0; i < 10; i++) {
			ArrayList<Matrix> m1 = kMeans(2, i);
			ArrayList<Matrix> m2 = kMeans(2, i+1);
			System.out.println("Consistency:" + computeConsistency(m1,m2));
		}
		
		//Determine the Elbow
		System.out.println("Determining the Elbow");
		for(int k = 1; k <= 15; k++) {
			Double minRSS = Double.POSITIVE_INFINITY;
			for(int seed = 0; seed < 10; seed++) {
				double rss = RSS(kMeans(k, seed));
				minRSS = Math.min(minRSS, rss);
			}
			System.out.println("k = " + k + "; rss = " + minRSS);
		}
		
		System.out.println("Experiment B");
		ArrayList<Matrix> mu4 = kMeans(4, 0);
		ArrayList<String> labels = labelClustersDocName(mu4);
		System.out.println("k = 4; The Cluster Labels Are:" + labels);
		
		ArrayList<Matrix> mu8 = kMeans(8, 0);
		labels = labelClustersDocName(mu8);
		System.out.println("k = 8; The Cluster Labels Are:" + labels);
		
		ArrayList<Matrix> mu13 = kMeans(13, 0);
		labels = labelClustersDocName(mu13);
		System.out.println("k = 13; The Cluster Labels Are:" + labels);
		
		System.out.println("Experiment C");
		System.out.println("k = 4:\n" + getClustering(mu4));
		System.out.println("k = 8:\n" + getClustering(mu8));
		System.out.println("k = 13:\n" + getClustering(mu13));
	}
	
	public static double computeConsistency(ArrayList<Matrix> mu1, ArrayList<Matrix> mu2) {
		int count = 0;
		HashSet<Integer> clusters11 = getClustering(mu1).get(0);
		HashSet<Integer> clusters12 = getClustering(mu1).get(1);
		HashSet<Integer> clusters21 = getClustering(mu2).get(0);
		HashSet<Integer> clusters22 = getClustering(mu2).get(1);
		for(int p1 : clusters11) {
			for(int p2 : clusters12) {
				if(clusters21.contains(p1) && clusters22.contains(p2) || clusters21.contains(p2) && clusters22.contains(p1))
					count++;
			}
		}
		return count/(double)(clusters11.size() * clusters12.size());
	}
	
	/**
	 * Displays the clustering for a set of means
	 * @param mu
	 */
	public static ArrayList<HashSet<Integer>> getClustering(ArrayList<Matrix> mu) {
		ArrayList<HashSet<Integer>> clusters = new ArrayList<HashSet<Integer>>();
		for(Matrix centroid : mu)
			clusters.add(new HashSet<Integer>());
		for(int i = 0; i < NUM_DOCUMENTS; i++) {
			double minDist = Double.POSITIVE_INFINITY;
			int index = -1;
			for(int j = 0; j < mu.size(); j++) {
				double distance = distance(mu.get(j), termDocumentMatrixNorm.getMatrix(i, i, 0, NUM_TERMS));
				if(distance < minDist) {
					minDist = distance;
					index = j;
				}
			}
			clusters.get(index).add(i);
		}
		return clusters;
	}
	
	/**
	 * Performs the minHash exercises from part 1
	 */
	public static void minHash() {
		for(int i = 0; i < NUM_DOCUMENTS; i++) {
			shingles.put(i, new HashSet<Integer>());
		}
		findShingles();
		System.out.println("Number of shingles:" + shingleIDs.size());
		if(shingleIDs.size() >= prime) {
			System.out.println("Number of shingles > p!");
			System.exit(1);
		}
		Random rand = new Random();
		for(int i = 0; i < 25; i++) {
			aValues.add(rand.nextInt(prime-1) + 1);
			bValues.add(rand.nextInt(prime));
		}
		computeSketches();
		System.out.println("Documents with Jaccard Estimate > 0.5");
		for(int i = 0; i < NUM_DOCUMENTS; i++) {
			for(int j = i+1; j < NUM_DOCUMENTS; j++) {
				if(i != j) {
					double jaccardEstimate = JaccardEstimate(i,j);
					if(jaccardEstimate > .5)
						System.out.println("J(" + i + "," + j + "):" + jaccardEstimate + "; Actual:" +  JaccardCoefficient(i,j));
				}
			}
		}
		System.out.println("Nearest Neighbors:");
		for(int i = 0; i < 10; i++) {
			LinkedList<Integer> topPairsDocID = new LinkedList<Integer>();
			LinkedList<Double> topPairsValues = new LinkedList<Double>();
			for(int j = 0; j < NUM_DOCUMENTS; j++) {
				if(i != j) {
					double coefficient = JaccardCoefficient(i,j);
					if(topPairsValues.isEmpty()) {
						topPairsValues.add(coefficient);
						topPairsDocID.add(j);
					} else if(coefficient > topPairsValues.getLast()) {
						for(int k = 0; k < topPairsValues.size(); k++) {
							if(topPairsValues.get(k) < coefficient) {
								topPairsValues.add(k, coefficient);
								topPairsDocID.add(k, j);
								if(topPairsValues.size() > 3) {
									topPairsValues.removeLast();
									topPairsDocID.removeLast();
								}
								break;
							}
						}
					}
				}
			}
			System.out.println("Document " + i + ":");
			for(int k = 0; k < topPairsValues.size(); k++)
				System.out.println("Neighbor " + topPairsDocID.get(k) + ":" + topPairsValues.get(k));
		}
	}
	
	/**
	 * Post processes a set of clusters to label them based on the document closest to the centroid
	 * @param mu list of clusters
	 * @return
	 */
	public static ArrayList<String> labelClustersDocName(ArrayList<Matrix> mu) {
		ArrayList<String> labels = new ArrayList<String>();
		for(Matrix centroid : mu) {
			int docID = -1;
			double minDistance = Double.POSITIVE_INFINITY;
			for(int i = 0; i < 40; i++) { //Use the first 40 documents, as per the instructions
				double distance = Math.min(minDistance, distance(centroid, termDocumentMatrixNorm.getMatrix(i, i, 0, NUM_TERMS)));
				if(distance < minDistance) {
					minDistance = distance;
					docID = i;
				}
			}
			labels.add("file" + String.format("%02d", docID));
		}
		return labels;
	}
	
	/*public static double mutualInformation(int termID, int clusterID, ArrayList<HashMap<Integer,Matrix>> clusters) {
		int docFreq = 0;
		for(int i = 0; i < NUM_DOCUMENTS; i++) {
			if(termDocumentMatrix.get(i, termID) > 0)
				docFreq++;
		}
		double pTerm = docFreq/NUM_DOCUMENTS;
		double pNotTerm = (NUM_DOCUMENTS - docFreq)/NUM_DOCUMENTS;
		double pCluster
	}*/
	
	/**
	 * Finds the RSS value of a set of centroids
	 * @param centroids
	 * @return
	 */
	public static double RSS(ArrayList<Matrix> centroids) {
		double sum = 0;
		for(int i = 0; i < NUM_DOCUMENTS; i++) {
			double min = Double.MAX_VALUE;
			int index = -1;
			Matrix document = termDocumentMatrixNorm.getMatrix(i,i, 0, NUM_TERMS);
			for(int j = 0; j < centroids.size(); j++) {
				double distance = distance(centroids.get(j), document);
				if(distance < min) {
					min = distance;
					index = j;
				}
			}
			sum += Math.pow(min, 2);
		}
		return sum;
	}
	
	/**
	 * Finds the actual Jaccard Coefficient between two documents
	 * @param i 
	 * @param j
	 * @return
	 */
	private static double JaccardCoefficient(int i, int j) {
		HashSet<Integer> union = ((HashSet<Integer>)shingles.get(i).clone());
		union.addAll(shingles.get(j));
		int k = 0;
		for(int shingleValueI : shingles.get(i))
			for(int shingleValueJ : shingles.get(j))
				if(shingleValueI == shingleValueJ) k++;
		return k/(double)union.size();
	}

	/**
	 * Estimates the Jaccard Coefficient between two documents
	 * @param i
	 * @param j
	 * @return
	 */
	private static double JaccardEstimate(int i, int j) {
		int k = 0;
		for(int sketchValueI : sketches.get(i))
			for(int sketchValueJ : sketches.get(j))
				if(sketchValueI == sketchValueJ) k++;
		return k/25.0;
	}

	/**
	 * Creates the Term Document Matrix by reading the provided file
	 */
	public static void createTermDocumentMatrix() {
		termDocumentMatrix = new Matrix(NUM_DOCUMENTS, NUM_TERMS+1);
		termDocumentMatrixNorm = new Matrix(NUM_DOCUMENTS, NUM_TERMS + 1);
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(TERM_DOC_FILENAME));
			while (scanner.hasNextLine()) {
				processLine(scanner.nextLine());
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found:" + TERM_DOC_FILENAME);
		} finally {
			scanner.close();
		}
		
		double magnitude = 0;
		for(int i = 0; i < NUM_DOCUMENTS; i++) {
			for(int j = 0; j < NUM_TERMS + 1; j++) {
				magnitude += Math.pow(termDocumentMatrix.get(i, j), 2);
			}
			magnitude = Math.sqrt(magnitude);
			for(int j = 0; j < NUM_TERMS + 1; j++) {
				termDocumentMatrixNorm.set(i,  j, termDocumentMatrix.get(i,j) /  magnitude);
			}
		}
	}
	
	/**
	 * Processes a single line out of the term matrix file
	 * @param line
	 */
	public static void processLine(String line) {
		String[] lineInfo = line.split(" ");
		termIDs.put(lineInfo[1], Integer.valueOf(lineInfo[0]));
		for(int position = 2; position < lineInfo.length - 2; position++) {
			String[] termFreq = lineInfo[position].split(":");
			termDocumentMatrix.set(Integer.valueOf(termFreq[0]), Integer.valueOf(lineInfo[0]), Integer.valueOf(termFreq[1]));
		}
	}
	
	/**
	 * Reads through the test set to find the shingles
	 */
	public static void findShingles() {
		Scanner scanner = null;
		for(int docID = 0; docID < 100; docID++) {
			String filename = "test/file" + String.format("%02d", docID) + ".txt";
			try {
				scanner = new Scanner(new FileInputStream(filename));
				String plaintext = "";
				while (scanner.hasNextLine()) {
					plaintext += scanner.nextLine() + "\n";
				}
				ArrayList<String> words = new ArrayList<String>(Arrays.asList(plaintext.split(" ")));
				while(words.remove(""));
				for(int j = 0; j < words.size() - 2; j++) {
					String shingle = words.get(j) + " " + words.get(j+1) + " " + words.get(j+2);
					int shingleID;
					if(!shingleIDs.containsKey(shingle)) shingleID = ++nextShingleID;
					else shingleID = shingleIDs.get(shingle);
					shingleIDs.put(shingle, shingleID);
					shingles.get(docID).add(shingleID);
				}
			} catch (FileNotFoundException e) {
				System.out.println("File not found:" + filename);
			} finally {
				scanner.close();
			}
		}
	}
	
	/**
	 * After the shingles have been found, computes the sketches
	 */
	public static void computeSketches() {
		for(int docID = 0; docID < NUM_DOCUMENTS; docID++) {
			HashSet<Integer> sketch = new HashSet<Integer>();
			for(int run = 1; run <= 25; run++) {
				int smallestFLabel = Integer.MAX_VALUE;
				for(int label : shingles.get(docID)) {
					smallestFLabel = Math.min(func(label,run), smallestFLabel);
				}
				sketch.add(smallestFLabel);
			}
			sketches.put(docID, sketch);
		}
	}
	
	/**
	 * Applys the map function as part of the Jaccard Estimate
	 * @param label
	 * @param s
	 * @return
	 */
	public static int func(int label, int s) {
		return (aValues.get(s-1) * label + bValues.get(s-1)) % prime;
	}
	
	/**
	 * Performs the kMeans algorithm
	 * @param k
	 * @param initSeed
	 * @return
	 */
	public static ArrayList<Matrix> kMeans(int k, int initSeed) {
		if(k > NUM_DOCUMENTS) throw new RuntimeException("Number of clusters exceeds number of documents");
		Random rand = new Random(initSeed);
		ArrayList<Integer> seeds = new ArrayList<Integer>();
		while(seeds.size() < k) {
			int randomSeed;
			do {
				randomSeed = rand.nextInt(NUM_DOCUMENTS);
			} while(seeds.contains(randomSeed));
			seeds.add(randomSeed);
		}
		ArrayList<Matrix> mu = new ArrayList<Matrix>();
		for(int seed : seeds) {
			mu.add(termDocumentMatrixNorm.getMatrix(seed, seed, 0, NUM_TERMS));
		}
		
		boolean finished = true;
		do {
			ArrayList<HashMap<Integer, Matrix>> omega = new ArrayList<HashMap<Integer, Matrix>>();
			for(int i = 0; i < mu.size(); i++) 
				omega.add(new HashMap<Integer, Matrix>());
			for(int i = 0; i < NUM_DOCUMENTS; i++) {
				double min = Double.POSITIVE_INFINITY;
				int index = -1;
				Matrix document = termDocumentMatrixNorm.getMatrix(i,i, 0, NUM_TERMS);
				for(int j = 0; j < mu.size(); j++) {
					double dist = distance(mu.get(j), document);
					if(dist < min) {
						min = dist;
						index = j;
					}
				}
				omega.get(index).put(i, document);
			}
			finished = true;
			ArrayList<Matrix> newMu = new ArrayList<Matrix>();
			for(int i = 0; i < mu.size(); i++) {
				Matrix newCentroid = centroid(omega.get(i));
				newMu.add(newCentroid);
				if(distance(newCentroid, mu.get(i)) > .000001) 
					finished = false;
			}
			mu = newMu;
			
		} while(!finished);
		return mu;
	}
	/**
	 * Finds the centroid of a set of documents
	 * @param hashMap
	 * @return
	 */
	public static Matrix centroid(HashMap<Integer, Matrix> hashMap) {
		Matrix sum = new Matrix(1, NUM_TERMS + 1);
		for(Matrix point : hashMap.values()) {
			sum = sum.plus(point);
		}
		Matrix centroid = sum.times(1.0/hashMap.size());
		double magnitude = 0;
		for(int i = 0; i < NUM_TERMS + 1; i++) {
			magnitude += Math.pow(centroid.get(0, i), 2);
		}
		return centroid.times(1.0 / Math.sqrt(magnitude));
	}
	
	/**
	 * Finds the euclidean distance between two documents
	 * @param a
	 * @param b
	 * @return
	 */
	public static double distance(Matrix a, Matrix b) {
		if(a.getColumnDimension() != b.getColumnDimension() || a.getRowDimension() != b.getRowDimension()) throw new RuntimeException("Vectors are not the same length");
		double sum = 0;
		for(int i = 0; i < a.getColumnDimension(); i++) {
			sum += Math.pow(a.get(0, i) - b.get(0, i), 2);
		}
		return Math.sqrt(sum);
	}
}

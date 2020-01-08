package it.unimib.disco.essere.deduplicator.rad.moea;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.moeaframework.analysis.plot.Plot;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import it.unimib.disco.essere.deduplicator.preprocessing.Instance;
import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.PreprocessingFacade;

public class MethodSelector {

	private AbstractProblem problem;
	// private AbstractEvolutionaryAlgorithm resolutionMethod;
	private String resolutionMethodName;
	private InstancesHandler codeHandler;
	private int numberOfIteration;

	private List<List<Double>> fittests;

	public MethodSelector(AbstractProblem problem, String resolutionMethod, int numberOfIteration,
			InstancesHandler codeHandler) {
		this.problem = problem;
		this.resolutionMethodName = resolutionMethod;
		this.numberOfIteration = numberOfIteration;
		this.codeHandler = codeHandler;

		fittests = new ArrayList<List<Double>>();

	}

	public List<List<ASTNode>> selectInstances() {
		Properties properties = new Properties();

		Algorithm algorithm = new CustomStandardAlgorithms().getAlgorithm(resolutionMethodName, properties, problem);

		List<Double> iteration = new ArrayList<Double>();

		for (int i = 0; i < numberOfIteration; i++) {
			algorithm.step();
			computeAvgFitnessValueForObj(algorithm, fittests);
			iteration.add((double) i);
		}

		//plotFitnessValues(fittests, iteration);

		return getMostFrequentStmts(algorithm);
	}

	private void computeAvgFitnessValueForObj(Algorithm algorithm, List<List<Double>> fittests) {
		for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
			try {
				computeJthObjAvg(algorithm, fittests, j);
			} catch (IndexOutOfBoundsException e) {
				fittests.add(new ArrayList<Double>());
				computeJthObjAvg(algorithm, fittests, j);
			}
		}
	}

	private void computeJthObjAvg(Algorithm algorithm, List<List<Double>> fittests, int j) {
		double sum = 0;
		for (Solution s : algorithm.getResult()) {
			sum += s.getObjective(j);
		}
		fittests.get(j).add(sum / algorithm.getResult().size());
	}

	/**
	 * @return the solution as a Pair: <binary representation, set of duplicated
	 *         statement related to the solution> ex. <"0101...0011",
	 *         ["if(var.equal(var))...", "try{var.apply()}..."]>
	 * 
	 *         The solution selected is the one: - which contain the statement that
	 *         is duplicated the most - contain the minimum number of 1s
	 */
	private List<List<ASTNode>> getMostFrequentStmts(Algorithm algorithm) {
		List<String> mostFreqStmt = new ArrayList<String>();
		String bestSolution = "";
		if (problem instanceof CustomAbstractProblem) {
			Map<String, String> solStmts = ((CustomAbstractProblem) problem).getValues(algorithm.getResult());

			// Find the solutions that contain the statement
			// that is duplicated the most
			Set<String> duplicated = new HashSet<String>(solStmts.values());
			int maxFreq = 0;
			String mostFreqStmtStr = "";
			for (String s : duplicated) {
				int freq = Collections.frequency(solStmts.values(), s);
				if (freq > maxFreq) {
					mostFreqStmtStr = s;
					maxFreq = freq;
				}
			}

			mostFreqStmt.addAll(Arrays.asList(mostFreqStmtStr.split("\n")));

			// Find the solution that contain the minimum number of 1s
			int minNumOfOnes = Integer.MAX_VALUE;
			for (Map.Entry<String, String> entry : solStmts.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();

				if (value.equals(mostFreqStmtStr) && StringUtils.countMatches(key, '1') < minNumOfOnes) {
					bestSolution = key;
				}
			}
		}

		List<List<ASTNode>> dupStmts = retrieveASTNodes(mostFreqStmt, bestSolution);

		return dupStmts;
	}

	private List<List<ASTNode>> retrieveASTNodes(List<String> mostFreqStmt, String bestSolution) {
		List<List<ASTNode>> dupStmts = new ArrayList<List<ASTNode>>();
		for (int i = 0; i < mostFreqStmt.size(); i++) {
			dupStmts.add(new ArrayList<ASTNode>());
			for (int j = 0; j < bestSolution.length(); j++) {
				if (bestSolution.charAt(j) == '1') {
					Instance method = this.codeHandler.getMethod(j);
					List<ASTNode> stmtInMethod = method.getStatement(mostFreqStmt.get(i));
					dupStmts.get(i).addAll(stmtInMethod);
				}
			}
		}
		return dupStmts;
	}

	private void plotFitnessValues(List<List<Double>> fittests, List<Double> iteration) {
		Plot p = new Plot();

		Double[] iters_obj = iteration.toArray(new Double[iteration.size()]);
		double[] iters_prim = ArrayUtils.toPrimitive(iters_obj);

		for (int j = 0; j < fittests.size(); j++) {
			Double[] fittests_obj = fittests.get(j).toArray(new Double[iteration.size()]);
			double[] fittests_prim = ArrayUtils.toPrimitive(fittests_obj);
			String label = "Fitness function";
			if (problem instanceof MultiObjective)
				label = ((MultiObjective) problem).getObjectivesNames().get(j);
			p.line(label, iters_prim, fittests_prim);
		}
		p.show();
	}

	public List<List<Double>> getListOfFittests() {
		return fittests;
	}

	public static void main(String[] args) throws Exception {
		PreprocessingFacade pf = new PreprocessingFacade();

		Instant start = Instant.now();

		InstancesHandler ih = pf.parseSourceCode(
				// "/home/umberto/Documents/OUTLINE/OUTLINE/src/main/java"
				"/home/umberto/Documents/WN_Sources/jasml/jasml-0.10/src/src"
		// "/home/umberto/Documents/WN_Sources/jFin_DateMath/jFin_DateMath-R1.0.1/src/src/main/java"
		);

		Instant preprocessing = Instant.now();

		// SingleObjective so = new SingleObjective(ih);
		// int numberOfIteration = 100;
		// String resolutionMethod = "GA";

		// MultiObjective mo = new MultiObjective(ih, 30);
		int numberOfIteration = 30;
		String resolutionMethod = "NSGA-II";

		List<Double> weightDupCode = new ArrayList<Double>();
		weightDupCode.add(0, 0.33);
		weightDupCode.add(1, 0.33);
		weightDupCode.add(2, 0.33);

		MethodSelector ms = new MethodSelector(new MultiObjective(ih, 30, weightDupCode), resolutionMethod,
				numberOfIteration, ih);

		ms.selectInstances();

		// test MyNSGAII____________________________________________________________
		// for algorithm setting see: org.moeaframework.algorithm.StandardAlgorithms

//		int populationSize = 100;
//
//		Initialization initialization = new CustomInitialization(mo,
//				populationSize);
//
//		NondominatedSortingPopulation population = 
//				new NondominatedSortingPopulation();
//
//		TournamentSelection selection = null;
//
//		selection = new TournamentSelection(2, new ChainedComparator(
//				new ParetoDominanceComparator(),
//				new CrowdingComparator()));
//
//
//		Variation variation = OperatorFactory.getInstance().getVariation(null, 
//				new Properties(), mo);
//
//		NSGAII alg = new NSGAII(mo, 
//				population, 
//				null, 
//				selection, 
//				variation,
//				initialization);
//
//		MethodSelector ms = new MethodSelector(mo, alg, ih, numberOfIteration);
		// ___________________________________________________________________________

		// MethodSelector ms = new MethodSelector(mo, resolutionMethod,
		// numberOfIteration, ih);
//		List<List<ASTNode>> p = ms.selectInstances();
//
//		System.out.println("Size  ->  " + p.size());
//		System.out.println("[0:]  ->  " + p.get(0));
//
//		Instant finish = Instant.now();
//
//		long time1 = Duration.between(start, preprocessing).getSeconds();  //in millis
//		long time2 = Duration.between(preprocessing, finish).getSeconds();  //in millis
//
//		System.out.println("Loading time: " + time1);
//		System.out.println("Find solution time: " + time2);
	}

}

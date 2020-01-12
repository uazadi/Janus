package it.unimib.disco.essere.janus.rad.moea;

import org.moeaframework.core.Initialization;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

public class CustomInitialization implements Initialization {
	
	/**
	 * The problem.
	 */
	protected final Problem problem;

	/**
	 * The initial population size.
	 */
	protected final int populationSize;

	/**
	 * Constructs a random initialization operator.
	 * 
	 * @param problem the problem
	 * @param populationSize the initial population size
	 */
	public CustomInitialization(Problem problem, int populationSize) {
		super();
		this.problem = problem;
		this.populationSize = populationSize;
	}

	@Override
	public Solution[] initialize() {
		Solution[] initialPopulation = new Solution[populationSize];

		for (int i = 0; i < populationSize; i++) {
			Solution solution = problem.newSolution();
			initialPopulation[i] = solution;
		}

		return initialPopulation;
	}

}

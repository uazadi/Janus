package it.unimib.disco.essere.deduplicator.rad.moea;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.rad.Member;
import it.unimib.disco.essere.deduplicator.rad.evaluation.AbstractEvaluator;
import it.unimib.disco.essere.deduplicator.rad.evaluation.Valuable;

public abstract class CustomAbstractProblem extends AbstractProblem {

	static int DEFAULT_INITIAL_NUM_OF_ONES = 10;

	protected InstancesHandler codeHandler;
	protected int initialNumOfOnes;

	public CustomAbstractProblem(
			InstancesHandler codeHandler, 
			int numberOfObjectives,
			int initialNumOfOnes) {

		super(codeHandler.getNumOfInstance(), numberOfObjectives);

		this.codeHandler = codeHandler;
		this.initialNumOfOnes = initialNumOfOnes;
	}

	public CustomAbstractProblem(
			InstancesHandler codeHandler, 
			int numberOfObjectives) {

		this(codeHandler, numberOfObjectives, CustomAbstractProblem.DEFAULT_INITIAL_NUM_OF_ONES);
	}


	/**
	 * Constructs a new solution and defines the bounds of the decision
	 * variables.
	 */
	@Override
	public Solution newSolution() {
		Solution solution = new Solution(
				getNumberOfVariables(), 
				getNumberOfObjectives());

		Random r = new Random();
		Set<Integer> positions = new HashSet<Integer>();
		while(initialNumOfOnes < getNumberOfVariables() // to avoid infinite loop
				&& positions.size() < initialNumOfOnes){
			positions.add(r.nextInt(getNumberOfVariables()-1));
		}

		for (int i = 0; i < getNumberOfVariables(); i++) {
			Variable value = EncodingUtils.newBoolean();
			if(positions.contains(i))
				EncodingUtils.setBoolean(value, true);
			else
				EncodingUtils.setBoolean(value, false);
			solution.setVariable(i, value);
		}

		return solution;
	}

	protected Member solutionToMember(Solution solution) {
		String bits = "";
		for(int i=0; i < getNumberOfVariables(); i++) {
			bits += solution.getVariable(i).toString();
		}
		Member m  = new Member(bits, this.codeHandler);
		return m;
	}

	protected void iterateOverPopulation(
			Population p, 
			Map<String, String> values, 
			AbstractEvaluator fitnessFunction) {
		
		if(fitnessFunction instanceof Valuable) {
			for(int i=0; i < p.size(); i++) {
				Member m = solutionToMember(p.get(i));
				String code = ((Valuable) fitnessFunction).getValue(m);
				if (!code.equals(""))
					values.put(m.toString(), code);
			}
		}
	}

	public abstract Map<String, String> getValues(Population p);

}
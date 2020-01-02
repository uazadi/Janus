package it.unimib.disco.essere.deduplicator.rad.moea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.rad.Member;
import it.unimib.disco.essere.deduplicator.rad.evaluation.AbstractEvaluator;
import it.unimib.disco.essere.deduplicator.rad.evaluation.AggregatedFitnessFuction;
import it.unimib.disco.essere.deduplicator.rad.evaluation.duplicatecode.StatementsExactMatchEvaluator;
import it.unimib.disco.essere.deduplicator.rad.evaluation.refactoringrisks.RefactoringRisksEvaluator;

public class SingleObjective extends CustomAbstractProblem {

	static double STANDARD_WEIGHT = 0.5;

	private AggregatedFitnessFuction fitnessFunction;

	public SingleObjective(
			InstancesHandler codeHandler, 
			List<AbstractEvaluator> objectives,
			List<Double> weights,
			int initialNumOfOnes) throws Exception {

		super(codeHandler, 1, initialNumOfOnes);
		this.fitnessFunction = new AggregatedFitnessFuction(objectives, weights, codeHandler);

	}

	//	List<AbstractEvaluator> ffs = new ArrayList<AbstractEvaluator>();
	//	List<Double> ws = new ArrayList<Double>();
	//
	//	StatementsExactMatchEvaluator evalDC = new StatementsExactMatchEvaluator(codeHandler);
	//	//evalDC.setWeights(wDC1, wDC2, wDC3);
	//	ffs.add(evalDC);
	//	ws.add(new Double(0.5));
	//
	//	RefactoringRisksEvaluator evalRR = new RefactoringRisksEvaluator(codeHandler);
	//	//evalRR.setWeights(wRR1, wRR2);
	//	ffs.add(evalRR);
	//	ws.add(new Double(0.5));


	public SingleObjective(
			InstancesHandler codeHandler,
			List<Double> weightsDupCode,
			List<Double> weightRefRisk) throws Exception {

		this(codeHandler, 
				new ArrayList<AbstractEvaluator>(
						Arrays.asList(
								new StatementsExactMatchEvaluator(codeHandler, weightsDupCode),
								new RefactoringRisksEvaluator(codeHandler, weightRefRisk))),
				new ArrayList<Double>(
						Arrays.asList(
								SingleObjective.STANDARD_WEIGHT,
								SingleObjective.STANDARD_WEIGHT)), 
				CustomAbstractProblem.DEFAULT_INITIAL_NUM_OF_ONES);
	}

	@Override
	public void evaluate(Solution solution) {
		String bits = "";
		for(int i=0; i < getNumberOfVariables(); i++) {
			bits += solution.getVariable(i).toString();
		}

		Member m  = new Member(bits, this.codeHandler);
		double[] fitnessValue = {fitnessFunction.computeFitnessFunction(m)};	
		solution.setObjectives(fitnessValue);
	}

	@Override
	public Map<String, String> getValues(Population p) {
		Map<String, String> values = new HashMap<String, String>();
		super.iterateOverPopulation(p, values, fitnessFunction);		
		return values;
	}

}

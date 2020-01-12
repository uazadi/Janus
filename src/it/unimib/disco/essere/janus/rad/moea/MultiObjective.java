package it.unimib.disco.essere.janus.rad.moea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.rad.Member;
import it.unimib.disco.essere.janus.rad.evaluation.AbstractEvaluator;
import it.unimib.disco.essere.janus.rad.evaluation.NumberOfOnesEvaluator;
import it.unimib.disco.essere.janus.rad.evaluation.duplicatecode.StatementsExactMatchEvaluator;
import it.unimib.disco.essere.janus.rad.evaluation.refactoringrisks.CodePositioningEvaluator;
import it.unimib.disco.essere.janus.rad.evaluation.refactoringrisks.VariableSimilarityEvaluator;

public class MultiObjective extends CustomAbstractProblem {
	private InstancesHandler code_handler;             
	private List<AbstractEvaluator> objectives;
	
		public MultiObjective(
			InstancesHandler code_handler, 
			List<AbstractEvaluator> objectives,
			int initialNumOfOnes) {
		super(code_handler, objectives.size(), initialNumOfOnes);
		this.objectives = objectives;
	}
	
	public MultiObjective(
			InstancesHandler code_handler, 
			int initialNumOfOnes,
			List<Double> weightDupCode) {
		this(code_handler, 
			 new ArrayList<AbstractEvaluator>(
					Arrays.asList(
							new StatementsExactMatchEvaluator(code_handler, weightDupCode),
							new VariableSimilarityEvaluator(code_handler),
							new CodePositioningEvaluator(code_handler))),
							//new NumberOfOnesEvaluator(code_handler))),
			 initialNumOfOnes);
	}
	
	public MultiObjective(
			InstancesHandler code_handler, 
			List<AbstractEvaluator> objectives) {
		this(code_handler, objectives, CustomAbstractProblem.DEFAULT_INITIAL_NUM_OF_ONES);
	}
	

	@Override
	public void evaluate(Solution solution) {
		Member m = solutionToMember(solution);
		
		double[] fanc_values = new double[this.objectives.size()];
		for(int i=0; i < this.objectives.size(); i++) {
			fanc_values[i] = this.objectives.get(i).computeFitnessFunction(m);
		}
		
		solution.setObjectives(fanc_values);
	}
	
	public List<String> getObjectivesNames(){
		List<String> names = new ArrayList<>();
		for(AbstractEvaluator e: this.objectives) {
			names.add(e.getClass().toString());
		}
		return names;
	}
	
	public Map<String, String> getValues(Population p){
		Map<String, String> values = new HashMap<>();
		for(AbstractEvaluator ae: this.objectives) {
				iterateOverPopulation(p, values, ae);
		}
		return values;
	}
}

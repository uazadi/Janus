package it.unimib.disco.essere.janus.rad.evaluation.refactoringrisks;

import java.util.List;

import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.rad.Member;

public class RefactoringRisksEvaluator extends AbstractRefRisksEvaluator {
	
	public static double WEIGHT_VARIABLE_SIMILARITY = 0.5;
	public static double WEIGHT_CODE_POSITIONING 	= 0.5;
	//public static double WEIGHT_FUN_IN 				= 0.25;
	
	private double weightVarSimil;
	private double weightCodePosit;
	//private double weightFunIn;
	
	VariableSimilarityEvaluator varSimil;
	CodePositioningEvaluator	codePosit;
	FunInMetricEvaluator		funIn;
	
	

	public RefactoringRisksEvaluator(InstancesHandler code_handler, List<Double> weights) {
		super(code_handler);
		setWeights(weights.get(0), weights.get(1));
		varSimil = new VariableSimilarityEvaluator(code_handler);
		codePosit = new CodePositioningEvaluator(code_handler);
		funIn = new FunInMetricEvaluator(code_handler);
	}
	
	
	/**
	 * [How to choose the weights]<br>
	 * If you think that the more risky factor for the rafactoring is:
	 * <ul>
	 * 		<li>the dissimilarity between the variables 
	 * 			used by each method, 
	 * 			set the higher value for "weightVarSimil";</li>
	 * 		<li>the position where the duplicated code is placed, 
	 * 			set the higher value for "weightCodePosit"</li>
	 * 		<li>the amount of methods that call the methods where
	 * 			the duplicate code is detected, 
	 * 			set the higher value for "weightFunIn"</li>
	 * </ul>
	 * The weights have to be set through the setter method: "setWeights" (otherwise 
	 * the default ones will be used, respectively: [0.5, 0.25, 0.25]).<br>
	 * [N.B. if you don't want to consider one of these factor pass 0 as weight]<br><br>
	 * 
	 * 
	 * @param member the member of the population that has to be evaluated
	 * @return the fitness value of member
	 */
	@Override
	public double computeFitnessFunction(Member member) {
		
//		System.out.println("\t [Variable] " + varSimil.computeFitnessFunction(member));
//		System.out.println("\t [CodePosition] " + codePosit.computeFitnessFunction(member));
//		System.out.println("\t [FunIn] " + funIn.computeFitnessFunction(member));
		
		double fitnessValue = 
				(weightVarSimil * varSimil.computeFitnessFunction(member))
				+ 1 + (weightCodePosit * codePosit.computeFitnessFunction(member));
				//- (weightFunIn * funIn.computeFitnessFunction(member));
		
		return fitnessValue;
	}
	
	/**
	 * @param weightVarSimil 	the weight (i.e. the relevance) that has to be given to 
	 * 							the average similarity between the variables used by the methods
	 * 							(the value is computed using each possible pair of methods)
	 * 							[N.B. this will be maximized, if you want to minimize it set a
	 * 							negative value];
	 * 
	 * @param weightCodePosit 	the weight (i.e. the relevance) that has to be given to
	 * 						 	the average position values of the methods.
	 * 							(the value is computed using each possible pair of methods
	 * 							and there are four possible position values:
	 * 							same_class_value < same_hierarchy_value < same_package_value < otherwise);
	 * 							[N.B. this will be minimized, if you want to maximized it set a
	 * 							negative value];
	 * 
	 * @param weightFunIn 		the weight (i.e. the relevance) that has to be given to
	 * 							the average fun-in metric computed for each method.
	 * 							[N.B. this will be minimized, if you want to maximized it set a
	 * 							negative value];
	 * */
	public void setWeights(double weightVarSimil, double weightCodePosit) {
		this.weightVarSimil = weightVarSimil;
		this.weightCodePosit =  weightCodePosit;
		//this.weightFunIn = weightFunIn;
	}

}

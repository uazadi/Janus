package it.unimib.disco.essere.deduplicator.rad.evaluation;

import java.util.List;

import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.rad.Member;
import it.unimib.disco.essere.deduplicator.rad.evaluation.duplicatecode.AbstractDuplicateCodeEvaluator;
import it.unimib.disco.essere.deduplicator.rad.evaluation.refactoringrisks.AbstractRefRisksEvaluator;

public class AggregatedFitnessFuction extends AbstractEvaluator implements Valuable{
	
	private static double DUPLICATE_CODE_WEIGHT = 0.5;
	private static double REFACTORING_RISKS_WEIGHT = 0.5;
	
	private double dupCodeWeight;
	private double refRisksWeight;
	
//	private AbstractDuplicateCodeEvaluator dupCode;
//	private AbstractRefRisksEvaluator refRisks;
	
	private List<AbstractEvaluator> fitnessFunctions;
	private List<Double> weights;
	

//	public AggregatedFitnessFuction(AbstractDuplicateCodeEvaluator dupCode, AbstractRefRisksEvaluator refRisks) {
//		super();
//		this.dupCode = dupCode;
//		this.refRisks = refRisks;
//		this.setWeight(DUPLICATE_CODE_WEIGHT, REFACTORING_RISKS_WEIGHT);
//	}
	
	/**
	 * @param fitnessFunctions 	the list of fitness functions that have to be 
	 * 							aggregate. For each fitness function make the 
	 * 							related "computeFitnessFunction" methods return a 
	 * 							positive value if the fitness function has to be 
	 * 							maximized, a negative value otherwise.
	 * @param weights			the weights related to each fitness function.
	 * 							the i-th weight will be used to weigh the i-th 
	 * 							fitness function.
	 * @throws Exception		if the length of the "fitnessFunctions" list is
	 * 							different from the length of the "weights" list. 
	 */
	public AggregatedFitnessFuction(
			List<AbstractEvaluator> fitnessFunctions, 
			List<Double> weights,
			InstancesHandler code_handler) throws Exception {
		super(code_handler);
		if(fitnessFunctions.size() != weights.size())
			throw new Exception("The number of fitness "
					+ "function and the number of weights "
					+ "have to be equal. It has been "
					+ "passed " + fitnessFunctions.size() 
					+ " fitness function and " 
					+ weights.size() +" weights");
		
		this.fitnessFunctions = fitnessFunctions;
		this.weights = weights;
	}
	
	@Override
	public double computeFitnessFunction(Member member) {
		double fitnessValue = 0;
//				(dupCodeWeight * dupCode.computeFitnessFunction(member))
//				+ (refRisksWeight * refRisks.computeFitnessFunction(member))
//				- 0.2 * member.countOnes();
		for(int i=0; i < fitnessFunctions.size(); i++) {
			fitnessValue = fitnessValue + 
					(weights.get(i) * 
							fitnessFunctions.get(i).computeFitnessFunction(member));
		}
		
		return fitnessValue;
	}
	
	public void setWeight(double dupCodeWeight, double refRisksWeight) {
		this.dupCodeWeight = dupCodeWeight;
		this.refRisksWeight = refRisksWeight;
	}
	
	public String getValue(Member member) {
		String value = "";
		for(AbstractEvaluator e: fitnessFunctions) {
			if(e instanceof Valuable)
				value = value
				+ "Value of " + e.getClass() + ": \n"
				+ ((Valuable) e).getValue(member) + "\n";
		}
		return value;
	}

}

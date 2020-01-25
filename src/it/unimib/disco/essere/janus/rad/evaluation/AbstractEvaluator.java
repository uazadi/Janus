package it.unimib.disco.essere.janus.rad.evaluation;

import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;

/** The abstract class of a template methods design pattern */
public abstract class AbstractEvaluator {
	
	protected InstancesHandler code_handler;
	
	public AbstractEvaluator(InstancesHandler code_handler) {
		this.code_handler = code_handler;
	}
	 
	/** compute and the set the fitness function value */
	public void evaluate(Population p) throws Exception{
		for(Member member : p.getPopulation()) {
			double value = this.computeFitnessFunction(member);
			member.setFitnessValue(value); 
		}
	}
	
	
	public abstract double computeFitnessFunction(Member member);

}

package it.unimib.disco.essere.janus.rad.evaluation;

import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;

public class NumberOfOnesEvaluator extends AbstractEvaluator{

	public NumberOfOnesEvaluator(InstancesHandler code_handler) {
		super(code_handler);
	}

	@Override
	public double computeFitnessFunction(Member member) {
		return member.countOnes();
	}

}

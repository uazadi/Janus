package it.unimib.disco.essere.deduplicator.rad.evaluation;

import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.rad.Member;

public class NumberOfOnesEvaluator extends AbstractEvaluator{

	public NumberOfOnesEvaluator(InstancesHandler code_handler) {
		super(code_handler);
	}

	@Override
	public double computeFitnessFunction(Member member) {
		return member.countOnes();
	}

}

package it.unimib.disco.essere.deduplicator.rad.evaluation.refactoringrisks;

import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.rad.evaluation.AbstractEvaluator;

public abstract class AbstractRefRisksEvaluator extends AbstractEvaluator {

	public AbstractRefRisksEvaluator(InstancesHandler code_handler) {
		super(code_handler);
	}

	protected double computeAvg(double sum, double size) {
		double den = (size == 2) ? 1 : ((size*size)/ 2) - size;
		return sum/den;
	}

}

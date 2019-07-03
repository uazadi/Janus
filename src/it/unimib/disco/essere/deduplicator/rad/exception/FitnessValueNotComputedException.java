package it.unimib.disco.essere.deduplicator.rad.exception;

public class FitnessValueNotComputedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FitnessValueNotComputedException() {
		super("Fitness value not yet computed, "
				+ "use one of the class contain in \"it.unimib.disco."
				+ "essere.MethodHandler.ga.evalution\" to compute the "
				+ "fitness function");
	}

}


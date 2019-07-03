package it.unimib.disco.essere.deduplicator.rad.exception;

public class NotInRangeNumberOfOnesException extends Exception {

	private static final long serialVersionUID = 1L;

	
	public NotInRangeNumberOfOnesException(int min, int max) {
		super("Bad number of ones requested, " + 
				"in order to cosider a Member valid it has " + 
				"to have at least " + min + " ones " + 
				"and at most " + max + " ones");
	}
}

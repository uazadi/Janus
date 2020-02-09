package it.unimib.disco.essere.janus.rad.evaluation;

public interface Valuable {
	
	/**
	 * The computation of the fitness function is based
	 * on the comparison of String. 
	 * This method return the String that affect (whatever 
	 * that means in each concrete situation) the fitness 
	 * function computation.
	 * */
	public String getValue(Member member);
}

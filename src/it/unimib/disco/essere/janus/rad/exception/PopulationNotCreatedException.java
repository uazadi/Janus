package it.unimib.disco.essere.janus.rad.exception;

public class PopulationNotCreatedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PopulationNotCreatedException() {
		super("Population not yet created, "
				+ "the \"generateInitialPopulation()\" "
				+ "method allows to create a random "
				+ "initial population");
	}
	
}

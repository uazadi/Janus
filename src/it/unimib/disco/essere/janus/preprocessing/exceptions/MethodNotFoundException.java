package it.unimib.disco.essere.janus.preprocessing.exceptions;

public class MethodNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MethodNotFoundException(int methodID) {
		super("Method with ID: " + methodID + " not found!");
	}
}


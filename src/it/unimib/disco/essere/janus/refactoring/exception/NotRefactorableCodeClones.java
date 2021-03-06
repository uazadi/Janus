package it.unimib.disco.essere.janus.refactoring.exception;

public class NotRefactorableCodeClones extends Exception {

	private static final long serialVersionUID = 1L;
	
	public static final String message = 
			"This kind of code clones can not be automatically refactored yet";

	public NotRefactorableCodeClones() {
		super(message);
	}

}

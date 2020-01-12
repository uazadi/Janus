package it.unimib.disco.essere.janus.rad.evaluation.duplicatecode;

import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.rad.Member;
import it.unimib.disco.essere.janus.rad.evaluation.AbstractEvaluator;
import it.unimib.disco.essere.janus.rad.evaluation.Valuable;

public abstract class AbstractDuplicateCodeEvaluator extends AbstractEvaluator implements Valuable{

	private static boolean DEAFULT_IGNORE_VALUE = true;
	
	protected boolean ignoreVariableNames;

	protected boolean ignoreConstantValues;
	
	public AbstractDuplicateCodeEvaluator(InstancesHandler code_handler) {
		super(code_handler);
		this.ignoreVariableNames = DEAFULT_IGNORE_VALUE;
		this.ignoreConstantValues = DEAFULT_IGNORE_VALUE;
	}

	public AbstractDuplicateCodeEvaluator(
			boolean ignoreVariables, 
			boolean ignoreConstants,
			InstancesHandler code_handler) {
		super(code_handler);
		this.ignoreVariableNames = ignoreVariables;
		this.ignoreConstantValues = ignoreConstants;
	}

	public boolean areVariableNamesIgnored() {
		return ignoreVariableNames;
	}

	public boolean areConstantValuesIgnored() {
		return ignoreConstantValues;
	}
	
	public abstract String getValue(Member member);
}

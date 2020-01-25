package it.unimib.disco.essere.janus.rad.evaluation.duplicatecode;

import it.unimib.disco.essere.janus.preprocessing.Instance;
import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.rad.evaluation.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StatementsExactMatchEvaluator extends AbstractDuplicateCodeEvaluator {

	public static double WEIGHT_NUMBER_OF_COPIED_STATEMENTS = 0.5;
	public static double WEIGHT_AVG_DUPLICATE_CODE_LENGTH = 0.25;
	public static double WEIGHT_AVG_NUMBER_OF_COPIES = 0.25;

	private double weightCopyStmt;
	private double weightDCLength; 
	private double weightNumOfCopies;
	
	public StatementsExactMatchEvaluator(
			InstancesHandler code_handler,
			List<Double> weights) {
		super(code_handler);
		setWeights(	weights.get(0), 
				weights.get(1),  
				weights.get(2));
	}

	public StatementsExactMatchEvaluator(
			boolean cosiderVariables, 
			boolean cosiderConstants,
			InstancesHandler code_handler){
		super(cosiderVariables, cosiderConstants, code_handler);
		setWeights(	WEIGHT_NUMBER_OF_COPIED_STATEMENTS, 
				WEIGHT_AVG_DUPLICATE_CODE_LENGTH, 
				WEIGHT_AVG_NUMBER_OF_COPIES);
	}
	
	/**
	 * [How to choose the weights]<br>
	 * If you want to focus the duplicate code detection on:
	 * <ul>
	 * 		<li>the number of duplicate statements, despite of 
	 * 			their structure, set the higher value for 
	 * 			"weightCopyStmt";</li>
	 * 		<li>the length of the duplicate statements found, despite 
	 * 			the number of statement, set the higher value for 
	 * 			"weightDCLength"</li>
	 * 		<li>the amount of times that the statements are duplicated, 
	 * 			despite the structure and the length of the statements, 
	 * 			set the higher value for "weightNumOfCopies"</li>
	 * </ul>
	 * The weights have to be set through the setter method: "setWeights" (otherwise 
	 * the default ones will be used, respectively: [0.5, 0.25, 0.25]).<br>
	 * [N.B. if you don't want to consider one of these factor pass 0 as weight]<br><br>
	 * 
	 * 
	 * @param member the member of the population that has to be evaluated
	 * @return the fitness value of member
	 */
	@Override
	public double computeFitnessFunction(Member member) {
		List<String> statements = getStatements(member);
		Map<String, Integer> copiedStatements = getCopiedStmt(statements, member);
		double fitnessFunction = 
				Math.log(1 + (weightCopyStmt * copiedStatements.size())) 
				+ Math.log(1 + (weightDCLength * getAvgLength(copiedStatements)))
				+ Math.log(1 + (weightNumOfCopies * getAvgNumOfCopies(copiedStatements)));
		
		return fitnessFunction * -1; // MAXIMIZE
	}

	@Override
	public String getValue(Member member) {
		List<String> statements = getStatements(member);
		Map<String, Integer> copiedStatements = getCopiedStmt(statements, member);

		String value = "";
		for(String stmt : copiedStatements.keySet()) {
			value = value 
					+ stmt + "\n&%&";
//					+ " [" + stmt.getParent().getName() 
//					+ ", " + stmt.getParent().getParent().getName() + "] \n";
		}
		return value;
	}
	
	private double getAvgLength(Map<String, Integer> copiedStatements) {
		if(copiedStatements.size() == 0)
			return 0;

		double sum = 0.0;
		for(String stmt: copiedStatements.keySet()) {
			sum = sum + stmt.length();
		}
		return sum/copiedStatements.size();
	}

	private double getAvgNumOfCopies(Map<String, Integer> copiedStatements) {
		if(copiedStatements.size() == 0)
			return 0;

		double sum = 0.0;
		for(Integer timesCloned: copiedStatements.values()) {
			sum = sum + timesCloned;
		}
		return sum/copiedStatements.size();
	}

	private Map<String, Integer> getCopiedStmt(List<String> statements, Member member) {
		Map<String, Integer> copiedStatements = new HashMap<String, Integer>();
		for(int i=0; i < statements.size(); i++) {
			for(int j=i+1; j < statements.size(); j++) {
				if(statements.get(i).equals(statements.get(j)) // exact match
						&& !statements.get(i).contains("return")
						&& statements.get(i).length() > 70
						//&& statements.get(i).getOriginalStatement().split("\n").length > 2 
						) {
					if(copiedStatements.containsKey(statements.get(i)))
						copiedStatements.replace(statements.get(i), 
								copiedStatements.get(statements.get(i)), 
								copiedStatements.get(statements.get(i)) + 1);
					else
						copiedStatements.put(statements.get(i), 2);
					
					statements.remove(j);
				}
			}
		}
		member.setClonedStatement(copiedStatements);
		return copiedStatements;
	}
	
	public List<String> getStatements(Member member) {
		List<String> statements = new ArrayList<String>();
		for(Instance method: member.getSelectedInstances()) {
			statements.addAll(method.getParsedStatement());
			//extractStatementsFromMethod(statements, method);
		}
		return statements;
	}


	public double getWeightCopyStmt() {
		return weightCopyStmt;
	}

	public double getWeightDCLength() {
		return weightDCLength;
	}

	public double getWeightNumOfCopies() {
		return weightNumOfCopies;
	}

	/**
	 * @param weightCopyStmt 	the weight (i.e. the relevance) that has to be given to 
	 * 							the number of copied statements found; 
	 * 
	 * @param weightDCLength 	the weight (i.e. the relevance) that has to be given to
	 * 						 	the copied statements average length 
	 * 
	 * @param weightNumOfCopies the weight (i.e. the relevance) that has to be given to
	 * 							the average number of times that a statement is copied 
	 * */
	public void setWeights(double weightCopyStmt, double weightDCLength, double weightNumOfCopies) {
		this.weightCopyStmt = weightCopyStmt;
		this.weightDCLength = weightDCLength;
		this.weightNumOfCopies = weightNumOfCopies;
	}
}

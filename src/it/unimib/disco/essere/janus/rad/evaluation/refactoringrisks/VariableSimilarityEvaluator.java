package it.unimib.disco.essere.janus.rad.evaluation.refactoringrisks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unimib.disco.essere.janus.preprocessing.Instance;
import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.rad.evaluation.Member;

public class VariableSimilarityEvaluator extends AbstractRefRisksEvaluator{
	
	public VariableSimilarityEvaluator(InstancesHandler code_handler) {
		super(code_handler);
	}

	public static int REWARD_BOTH_EMPTY_PARAM_LIST = 5;

	@Override
	public double computeFitnessFunction(Member member) {
		if(member.getSelectedInstances().isEmpty())
			return 0;
		double sum = sumVariableSimilarities(member);
		double size = member.getSelectedInstances().size();
		return computeAvg(sum, size) * -1; // MAXIMIZE
	}

	private double sumVariableSimilarities(Member member) {
		double sum = 0.0;
		List<Instance> methods = member.getSelectedInstances();
		for(int i=0; i < methods.size(); i++) {
			for(int j = i+1; j < methods.size(); j++) {
				sum = sum + computeParameterSimilarity(methods.get(i), methods.get(j));
				sum = sum + computeTypesSimilarity(methods.get(i), methods.get(j));
			}
		}
		return sum;
	}

	private double computeTypesSimilarity(Instance m1, Instance m2) {
		Set<String> set1 = new HashSet<String>(m1.getVariableTypes());
		Set<String> set2 = new HashSet<String>(m2.getVariableTypes());
		set1.retainAll(set2);
		return set1.size();
	}

	private double computeParameterSimilarity(Instance m1, Instance m2) {
		if(m1.getMethodParameters().isEmpty() 
				&& m2.getMethodParameters().isEmpty())
			return REWARD_BOTH_EMPTY_PARAM_LIST; 
		if(m1.getMethodParameters().isEmpty()
				|| m2.getMethodParameters().isEmpty())
			return 0;
		
		Set<String> types1 = extractParameterInfo(m1, "types");
		Set<String> names1 = extractParameterInfo(m1, "names");
		
		Set<String> types2 = extractParameterInfo(m2, "types");
		Set<String> names2 = extractParameterInfo(m2, "names");
		
		types1.retainAll(types2);
		names1.retainAll(names2);
		
		return types1.size() + names1.size();
	}
	
	private Set<String> extractParameterInfo(Instance m1, String toBeExtracted) {
		int index = 0;
		if("names".equals(toBeExtracted))
			index = 1;
		
		Set<String> container = new HashSet<String>();
		
		for(String par: m1.getMethodParameters()) {
			String type= par.split(" ")[index];
			container.add(type);
		}
		
		return container;
	}

	
}

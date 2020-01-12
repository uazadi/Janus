package it.unimib.disco.essere.janus.rad.evaluation.refactoringrisks;

import java.util.List;

import it.unimib.disco.essere.janus.preprocessing.Instance;
import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.rad.Member;

public class CodePositioningEvaluator extends AbstractRefRisksEvaluator{
	
	private static int SAME_CLASS     = 0;
	private static int SAME_HIERARCHY = 10; // 2 
	private static int SAME_PACKAGE   = 20; // 5
	private static int OTHERWISE      = 50; // 10
	
	private int same_class;
	private int same_hierarchy;
	private int same_package;
	private int otherwise;
	
	public CodePositioningEvaluator(InstancesHandler code_hander) {
		super(code_hander);
		this.same_class = CodePositioningEvaluator.SAME_CLASS;
		this.same_hierarchy = CodePositioningEvaluator.SAME_HIERARCHY;
		this.same_package = CodePositioningEvaluator.SAME_PACKAGE;
		this.otherwise = CodePositioningEvaluator.OTHERWISE;
	}
	
	public CodePositioningEvaluator(
			int same_class, 
			int same_hierarchy, 
			int same_package, 
			int otherwise,
			InstancesHandler code_hander) {
		super(code_hander);
		this.same_class     = same_class;
		this.same_hierarchy = same_hierarchy;
		this.same_package   = same_package;
		this.otherwise      = otherwise;
	}
	

	@Override
	public double computeFitnessFunction(Member member) {
		if(member.getSelectedInstances().isEmpty())
			return 0;
		
		double sum = sumPenalties(member);
		double size = member.getSelectedInstances().size();
		return Math.log(computeAvg(sum, size));
	}

	private double sumPenalties(Member member) {
		double sum = 0.0;
		List<Instance> methods = member.getSelectedInstances();
		for(int i=0; i < methods.size(); i++) {
			for(int j=i+1; j < methods.size(); j++) {
				sum = sum + getPenalty(methods.get(i), methods.get(j));
			}
		}
		return sum;
	}
	
	private int getPenalty(Instance m1, Instance m2) {
		if(sameClass(m1, m2))
			return 	this.same_class;
		if(sameHierarchy(m1,m2))
			return this.same_hierarchy;
		if(samePackage(m1, m2))
			return this.same_package;
		return this.otherwise;
	}

	private boolean samePackage(Instance m1, Instance m2) {
		String className1 = m1.getClassName();
		String package1 = className1.substring(0, className1.lastIndexOf("."));
		
		String className2 = m2.getClassName();
		String package2 = className2.substring(0, className2.lastIndexOf("."));
		
		return package1.equals(package2);
	}

	private boolean sameHierarchy(Instance m1, Instance m2) {
		String className1 = m1.getClassName();
		String className2 = m2.getClassName();
		String superClass = code_handler.findNearestCommonSuperclass(className1, className2);
		
		if(superClass == null || "java.lang.Object".equals(superClass))
			return false;
		return true;
	}

	private boolean sameClass(Instance m1, Instance m2) {
		String className1 = m1.getClassName();
		String className2 = m2.getClassName();
		return className1.equals(className2);
	}

}

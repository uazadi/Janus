package it.unimib.disco.essere.janus.rad.evaluation.refactoringrisks;

import it.unimib.disco.essere.janus.preprocessing.Instance;
import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.rad.Member;

public class FunInMetricEvaluator extends AbstractRefRisksEvaluator {

	public FunInMetricEvaluator(InstancesHandler code_handler) {
		super(code_handler);
	}

	/**
	 * Compute the sum of the FUN-IN metrics of each method 
	 * selected by the member
	 * */
	@Override
	public double computeFitnessFunction(Member member) {
		double sum = 0.0;
		for(Instance m: member.getSelectedInstances()) {
			//boolean visit = !(md.modifiers().toString().contains("private"));
//			if( ! ((MethodDeclaration) m.getNode()).modifiers().toString().contains("private")) {
//				MethodInvocationVisitor visitor = new MethodInvocationVisitor(m.getName(), m.getParent().getName());
//				JavaContainer dir = m.getParent().getParent().getParent();
//				for(JavaComponent jc: dir.getChildren()){
//					jc.getNode().accept(visitor);
//				}
//				sum = sum + visitor.getValues().size();
//			}
		}
		//System.out.println(sum);
		return sum;
	}
}

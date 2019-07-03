package it.unimib.disco.essere.deduplicator.refactoring;

import java.io.File;
import java.time.Instant;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
//import org.eclipse.jdt.internal.corext.refactoring.ParameterInfo;
//import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
//import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
//import org.eclipse.jdt.internal.ui.refactoring.code.ExtractMethodWizard;
//import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.PreprocessingFacade;
import it.unimib.disco.essere.deduplicator.rad.moea.MethodSelector;
import it.unimib.disco.essere.deduplicator.rad.moea.MultiObjective;


public class Test {

	
	public static void main(String[] args) throws Exception {
		PreprocessingFacade pf = new PreprocessingFacade();

		Instant start = Instant.now();

		InstancesHandler ih = pf.parseSourceCode(
				//"/home/umberto/Documents/OUTLINE/OUTLINE/src/main/java"
				//"/home/umberto/Documents/WN_Sources/jasml/jasml-0.10/src/src"
				"/home/umberto/Documents/WN_Sources/jFin_DateMath/jFin_DateMath-R1.0.1/src/src/main/java"
				);

		Instant preprocessing = Instant.now();

		MultiObjective mo = new MultiObjective(ih, 30);
		int numberOfIteration = 30;
		String resolutionMethod = "NSGA-II";

		MethodSelector ms = new MethodSelector(mo, resolutionMethod, numberOfIteration, ih);

		//MethodSelector ms = new MethodSelector(mo, resolutionMethod, numberOfIteration);
		List<List<ASTNode>> p = ms.selectInstances();
		
		
		
		Statement stmt = (Statement) p.get(0).get(0);
		
		MethodDeclaration method = (MethodDeclaration) stmt.getParent().getParent();
		CompilationUnit cu = (CompilationUnit) stmt.getParent().getParent().getParent().getParent();

		ICompilationUnit icu = (ICompilationUnit) cu.getJavaElement();
		
		String path = "/home/umberto/Documents/WN_Sources/jFin_DateMath/jFin_DateMath-R1.0.1/src/src/main/java";
		String mPackage = cu.getPackage().toString().replace(".", "/").replaceAll("(;|\\n)*", "").split(" ")[1];
		String className =  ((TypeDeclaration) cu.types().get(0)).getName().getIdentifier();
		
		System.out.println(path + "/" + mPackage + "/" + className);	
		
//		ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(
//				icu, stmt.getStartPosition(), stmt.getLength());
//		
//		refactoring.setMethodName("extracted");
//		
//		//refactoring.setVisibility(visibility);
//		RefactoringStatus status= refactoring.checkInitialConditions(new NullProgressMonitor());
//
//		List<ParameterInfo> par = method.parameters();
		
		//new RefactoringStarter().activate(new ExtractMethodWizard(refactoring), getShell(), RefactoringMessages.ExtractMethodAction_dialog_title, 3);
		
//		List<ParameterInfo> parameters= refactoring.getParameterInfos();
//		if (newNames != null && newNames.length > 0) {
//			for (int i= 0; i < newNames.length; i++) {
//				if (newNames[i] != null)
//					parameters.get(i).setNewName(newNames[i]);
//			}
//		}
//		if (newOrder != null && newOrder.length > 0) {
//			assertTrue(newOrder.length == parameters.size());
//			List<ParameterInfo> current= new ArrayList<>(parameters);
//			for (int i= 0; i < newOrder.length; i++) {
//				parameters.set(newOrder[i], current.get(i));
//			}
//		}
//		refactoring.setDestination(destination);
//
//		String out= null;
//		if (mode == COMPARE_WITH_OUTPUT)
//			out= getProofedContent(outputFolder, id);

	}
}

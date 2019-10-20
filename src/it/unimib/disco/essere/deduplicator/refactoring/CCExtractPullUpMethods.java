package it.unimib.disco.essere.deduplicator.refactoring;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.structure.PullUpRefactoringProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;

public class CCExtractPullUpMethods extends CCRefactoring {
	
	private String lowestCommonSuperclass;
	private ICompilationUnit superClass;

	public CCExtractPullUpMethods(
			List<ASTNode> cloneSet, 
			List<ICompilationUnit> icus_involved,
			IJavaProject project,
			String lowestCommonSuperclass,
			ICompilationUnit superClass) {
		super(cloneSet, icus_involved, project);
		this.lowestCommonSuperclass = lowestCommonSuperclass;
		this.superClass = superClass;
	}

	@Override
	public void apply() throws UnsuccessfulRefactoringException {
		checkCloneType();
		sortNodes(cloneSet);
		try {
			extractMethods();
			if(extractedMethods.size() >= 2) {
				IMethod kept = selectMethodToKeep(extractedMethods);
				pullUpMethod(lowestCommonSuperclass, superClass, kept);
				// delete the method in the subclass
				kept.delete(true, null);
			}
			saveAllCompilationUnits();
		} catch (CoreException e) {
			throw new UnsuccessfulRefactoringException(e.getMessage());
		}

	}
	
	private void pullUpMethod(String lcsSoFar, ICompilationUnit superClass, IMethod kept) throws JavaModelException {
		//PullUpDescriptor pud = new PullUpDescriptor();

		CodeGenerationSettings settings =
				JavaPreferencesSettings.getCodeGenerationSettings(kept.getCompilationUnit().getJavaProject());

		IMember[] ims = {kept};

		PullUpRefactoringProcessor mimp = new PullUpRefactoringProcessor(ims, settings);

		String[] path = lcsSoFar.split("\\.");
		String className = path[path.length - 1];

		superClass.getType(className);

		mimp.setDestinationType(superClass.getAllTypes()[0]);

		try {
			mimp.checkInitialConditions(new NullProgressMonitor());
			CheckConditionsContext ccc = new CheckConditionsContext();
			ccc.add(new ResourceChangeChecker());
			mimp.checkFinalConditions(new NullProgressMonitor(), ccc);
			Change change = mimp.createChange(new NullProgressMonitor());
			change.perform(new NullProgressMonitor());
		} catch (OperationCanceledException | CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

package it.unimib.disco.essere.deduplicator.refactoring;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;

public class CCExtractMethod extends CCRefactoring {
	
	public CCExtractMethod(
			List<ASTNode> cloneSet,
			List<ICompilationUnit> icus_involved,
			IJavaProject project) {
		super(cloneSet, icus_involved, project);
	}
	
	@Override
	public void apply() throws UnsuccessfulRefactoringException {
		sortNodes(cloneSet);
		try {
			checkCloneType();
			extractMethods();
			selectMethodToKeep(extractedMethods);
			
		} catch(JavaModelException e) {
			// TODO Check access to delete method 
			// raise when trying to get a method that has already been deleted
			// no hard done, the computation can continue, but an investigation
			// on why this happen is worth it 
		} 
		catch (CoreException e) {
			throw new UnsuccessfulRefactoringException(e.getMessage());
		}
		
	}
	
}

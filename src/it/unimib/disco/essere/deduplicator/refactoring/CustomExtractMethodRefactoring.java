package it.unimib.disco.essere.deduplicator.refactoring;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

@SuppressWarnings("restriction")
public class CustomExtractMethodRefactoring extends ExtractMethodRefactoring {

	public CustomExtractMethodRefactoring(ICompilationUnit unit, int selectionStart, int selectionLength) {
		super(unit, selectionStart, selectionLength);
	}
	
	public CustomExtractMethodRefactoring(List<ICompilationUnit> units, int selectionStart, int selectionLength) {
		super(units.get(0), selectionStart, selectionLength);
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException {
		Change change = super.createChange(pm);
		
		MethodDeclaration mm = this.createNewMethod(selectedNodes, this.fCUnit.findRecommendedLineSeparator(),
				substituteDesc);
		
		return change
	}


}

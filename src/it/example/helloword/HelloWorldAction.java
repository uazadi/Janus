package it.example.helloword;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.StatementRewrite;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.internal.ui.refactoring.code.ExtractMethodWizard;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jdt.internal.core.DeleteElementsOperation;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.e4.core.services.log.Logger;

import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.PreprocessingFacade;
import it.unimib.disco.essere.deduplicator.rad.moea.MethodSelector;
import it.unimib.disco.essere.deduplicator.rad.moea.MultiObjective;
import it.unimib.disco.essere.deduplicator.rad.moea.SingleObjective;

@SuppressWarnings("restriction")
public class HelloWorldAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	private Action action = this;
	private IJavaProject selectedProject;

	private String extMethodName;


	@Override
	public void run(IAction arg0) {
		// MessageDialog.openInformation(window.getShell(), "FirstPlugin", "Hello,
		// World!");

		IStructuredSelection selection = 
				(IStructuredSelection) window.getSelectionService().getSelection();
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof IAdaptable) {
			IJavaProject project = null;
			if(firstElement instanceof Project) {
				Project projectTmp = (Project) firstElement;
				project = JavaCore.create(projectTmp);
			}else if (firstElement instanceof JavaProject) {
				JavaProject projectTmp = (JavaProject) firstElement;
				project = projectTmp.getJavaProject();
			}
			accomplishRefactoring(project);
		}
	}

	private void accomplishRefactoring(IJavaProject project) {
		try {
			PreprocessingFacade pf = new PreprocessingFacade();
			InstancesHandler ih = pf.parseSourceCode(project);

			//MultiObjective mo = new MultiObjective(ih, 30);
			SingleObjective so = new SingleObjective(ih);
			int numberOfIteration = 30;
			String resolutionMethod = "NSGA-II";

			MethodSelector ms = new MethodSelector(so, resolutionMethod, numberOfIteration, ih);
			List<List<ASTNode>> p = ms.selectInstances();
			
			System.out.println("[HelloWorldAction - accomplishRefactoring]  " + "Code Clones:");

			// For each set of clone to be refactored
			for(List<ASTNode> cloneSet: p) {

				List<ICompilationUnit> icus_involved = new LinkedList<>();
				sortNodes(cloneSet);
				
				// For each clone within the group selected 
				for(int i=0; i < cloneSet.size(); i++) {
					Statement stmt = (Statement) cloneSet.get(i);
					
					System.out.println("[HelloWorldAction - accomplishRefactoring]  " + stmt.toString());
					
					extractMethod(stmt, icus_involved);
				}
				List<IMethod> extractedMethods = getExtractedMethods(icus_involved);
				selectOneOfExtracted(extractedMethods);
				
				for(ICompilationUnit icu: icus_involved)
					icu.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void selectOneOfExtracted(List<IMethod> extractedMethods) throws JavaModelException {
		if(extractedMethods.size() >= 2) {
			boolean toKeepChosen = false;
			for(int i=0; i < extractedMethods.size(); i++) {
				IMethod extr = extractedMethods.get(i);
				
				//extr.mo
				
				
				// A method has to be kept if:
				// 1) No other method has already been chosen
				// AND
				// 2) either this is the last of the methods extracted 
				//    OR this is the first "static" method extracted
				if(!toKeepChosen &&
						(extr.getSignature().contains(" static ") ||
						 (i + 1) == extractedMethods.size())) {
					toKeepChosen = true;	
				}
				else {
					extr.delete(true, null);
				}
			}
		}
	}

	private List<IMethod> getExtractedMethods(List<ICompilationUnit> workingCopies) throws JavaModelException {
		List<IMethod> extractedMethods = new LinkedList<>();

		for(ICompilationUnit workingCopy: workingCopies) {
			workingCopy.commitWorkingCopy(true, new NullProgressMonitor());
			workingCopy.reconcile(AST.JLS11, true, null, new NullProgressMonitor());

			IMethod[] methods = workingCopy.getTypes()[0].getMethods();

			// Find the extracted methods
			int k = 0;
			for(int j=0; j < methods.length; j++) {
				IMethod im = methods[j];
				if(im.getElementName().equals(this.extMethodName)){
					extractedMethods.add(im);
					k++;
				}
			}
		}
		return extractedMethods;
	}

	private void extractMethod(Statement stmt, List<ICompilationUnit> icus_involved) throws JavaModelException, CoreException {
		ICompilationUnit workingCopy = getICompilationUnit(stmt);
		workingCopy.becomeWorkingCopy(new NullProgressMonitor());

		ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(
				workingCopy, stmt.getStartPosition(), stmt.getLength());

		this.extMethodName = refactoring.getMethodName();
		refactoring.checkAllConditions(new NullProgressMonitor());
		Change change = refactoring.createChange(new NullProgressMonitor());
		change.perform(new NullProgressMonitor());
		icus_involved.add(workingCopy);
		
	}

	private void sortNodes(List<ASTNode> nodes) {
		/** Sort the statements in descending order, in this way
		    if two statement are in the same class, the one one in 
		    the lower position is extracted before then the one 
		    in the higher position. 
		    The other way around will generate a conflict concerning 
		    the position of the code!
		 */ 
		nodes.sort(new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				if(node1.getStartPosition() > node2.getStartPosition())
					return -1;
				else 
					return 1;
			}
		});
	}

	private ICompilationUnit getICompilationUnit(Statement stmt) {
		ASTNode parent = stmt.getParent();
		while(!(parent instanceof CompilationUnit))
			parent = parent.getParent();
		CompilationUnit cu = (CompilationUnit) parent;
		ICompilationUnit icu = (ICompilationUnit) cu.getJavaElement();
		return icu;
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}

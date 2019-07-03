package it.example.helloword;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.StatementRewrite;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.internal.ui.refactoring.code.ExtractMethodWizard;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.PreprocessingFacade;
import it.unimib.disco.essere.deduplicator.rad.moea.MethodSelector;
import it.unimib.disco.essere.deduplicator.rad.moea.MultiObjective;

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
			Project projectTmp = (Project) firstElement;
			IJavaProject project = JavaCore.create(projectTmp);
			try {
				PreprocessingFacade pf = new PreprocessingFacade();
				InstancesHandler ih = pf.parseSourceCode(project);
				
				MultiObjective mo = new MultiObjective(ih, 30);
				int numberOfIteration = 30;
				String resolutionMethod = "NSGA-II";
				
				MethodSelector ms = new MethodSelector(mo, resolutionMethod, numberOfIteration, ih);
				List<List<ASTNode>> p = ms.selectInstances();

				
				List<Change> list = new LinkedList<Change>();
				
				//TODO Controllo che se due cloni sono nella stessa classe 
				//     viene computato l'offset delle righe
				for(List<ASTNode> cloneSet: p) {
					boolean keep = true;
					for(ASTNode clone: cloneSet) {
						
						Statement stmt = (Statement) clone;
						
						System.out.println(((TypeDeclaration) stmt.getParent().getParent().getParent()).getName());
						System.out.println(((MethodDeclaration) stmt.getParent().getParent()).getName());
						System.out.println(stmt);
						
						
						ICompilationUnit icu = getICompilationUnit(stmt);
						
						ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(
								icu, stmt.getStartPosition(), stmt.getLength());
						
						this.extMethodName = refactoring.getMethodName();
						
						refactoring.checkAllConditions(new NullProgressMonitor());
						Change change = refactoring.createChange(new NullProgressMonitor());
						refactoring.checkFinalConditions(new NullProgressMonitor());
						
						
						change.perform(new NullProgressMonitor());
						
						//change.perform(new NullProgressMonitor());
						
						list.add(change);
						
//						ExtractMethodWizard wizard = new ExtractMethodWizard(refactoring);
//
//						RefactoringStarter starter = new RefactoringStarter();
						
//						if(!keep) {
//							refactoring.setMethodName("extracted1");
//							this.extMethodName = refactoring.getMethodName();
//							ASTRewrite fRewriter = ASTRewrite.create(stmt.getAST());
//							ASTNode[] astmt = {stmt};
//						}
//								
					//starter.activate(wizard, window.getShell(), "Title", 2);

						keep = false; // true only during the first execution
						
						
					}
				}
				
				for(Change c: list)
					c.perform(new NullProgressMonitor());

			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
//		WizardDialog wizardDialog = new WizardDialog(window.getShell(), new HelloWizard(""));
//		wizardDialog.open();
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

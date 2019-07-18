package it.example.helloword;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
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

				// For each set of clone to be refactored
				for(List<ASTNode> cloneSet: p) {

					sortNodes(cloneSet);

					// For each clone within the group selected 
					for(int i=0; i < cloneSet.size(); i++) {

						Statement stmt = (Statement) cloneSet.get(i);

						System.out.println(((TypeDeclaration) stmt.getParent().getParent().getParent()).getName());
						System.out.println(((MethodDeclaration) stmt.getParent().getParent()).getName());
						System.out.println(stmt);

						ICompilationUnit workingCopy = getICompilationUnit(stmt);
						workingCopy.becomeWorkingCopy(new NullProgressMonitor());

						ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(
								workingCopy, stmt.getStartPosition(), stmt.getLength());

						this.extMethodName = refactoring.getMethodName();
						refactoring.checkAllConditions(new NullProgressMonitor());
						Change change = refactoring.createChange(new NullProgressMonitor());
						change.perform(new NullProgressMonitor());

						workingCopy.commitWorkingCopy(true, new NullProgressMonitor());
						CompilationUnit c = workingCopy.reconcile(AST.JLS11, 
								true, null, new NullProgressMonitor());
						
						IMethod[] methods = workingCopy.getTypes()[0].getMethods();
						
						IMethod[] extractedMethods = new IMethod[2];
						
						
						// Find the extracted methods
						int k = 0;
						for(int j=0; j < methods.length; j++) {
							IMethod im = methods[j];
							if(im.getElementName().equals(this.extMethodName)){
								extractedMethods[k] = im;
								k++;
							}
						}
						
						IMethod toBeDeleted = extractedMethods[0];
						
						if(extractedMethods[0].getSignature().contains(" static "))
							toBeDeleted = extractedMethods[0];
						else if(extractedMethods[1].getSignature().contains(" static "))
							toBeDeleted = extractedMethods[1];
						
						toBeDeleted.delete(true, null);
						
//						for(int j=0; j < methods.length; j++) {
//							IMethod im = methods[j];
//							
//							System.out.println(im.getElementName());
//							
//							// if it is one of the method extracted AND
//							if(im.getElementName().equals(this.extMethodName)
//									// the method extracted is NOT a static one OR
//									&& (!im.getSignature().contains(" static ")  
//									// the method to keep has already been chosen
//											|| !keep)){ 	
//								im.delete(false, null);
//							}
//							else { // path executed when the method to keep is chose
//								//methods[j] = null;
//								keep = false;											
//							}
//						}
//						
						
//						for(int j=0; j < toBeDeleted.size(); j++) {
//							
//							System.out.println(toBeDeleted.get(j).resolveBinding());
//							
//							toBeDeletedJavaElem[j] = 
//									(IMethod) toBeDeleted.get(j).resolveBinding().getJavaElement();
//						}
						
						//TextFileChange textFileChange = new TextFileChange(icu, (IFile)c.getResource());
						
//						DeleteElementsOperation deleter = new DeleteElementsOperation(
//								toBeDeleted.toArray(new IJavaElement[toBeDeleted.size()]), 
//								true);
//						deleter.run(null);
						
//						for(MethodDeclaration md: toBeDeleted) {
//							ASTRewrite rewriter = ASTRewrite.create(c.getAST());
//							rewriter.remove(md, null);
//							//rewriter.rewriteAST(c, null);
//						}

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		//		WizardDialog wizardDialog = new WizardDialog(window.getShell(), new HelloWizard(""));
		//		wizardDialog.open();
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

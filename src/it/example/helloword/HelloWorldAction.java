package it.example.helloword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import it.unimib.disco.essere.deduplicator.behaviouralcheck.CheckCompilation;
import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.MethodHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.PreprocessingFacade;
import it.unimib.disco.essere.deduplicator.rad.moea.MethodSelector;
import it.unimib.disco.essere.deduplicator.rad.moea.MultiObjective;
import it.unimib.disco.essere.deduplicator.rad.moea.SingleObjective;
import it.unimib.disco.essere.deduplicator.refactoring.CCRefactoring;

@SuppressWarnings("restriction")
public class HelloWorldAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	private IJavaProject selectedProject;

	@Override
	public void run(IAction arg0) {
		// MessageDialog.openInformation(window.getShell(), "FirstPlugin", "Hello,
		// World!");

		IStructuredSelection selection = 
				(IStructuredSelection) window.getSelectionService().getSelection();
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof IAdaptable) {
			if(firstElement instanceof Project) {
				Project projectTmp = (Project) firstElement;
				selectedProject = JavaCore.create(projectTmp);
			}else if (firstElement instanceof JavaProject) {
				JavaProject projectTmp = (JavaProject) firstElement;
				selectedProject = projectTmp.getJavaProject();
			}
			accomplishRefactoring(selectedProject);
			//new CheckCompilation().check(selectedProject);

			try {
				System.out.println("Output location: " + selectedProject.getOutputLocation());
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			executeCommand("rm -r ./test ");
			executeCommand("cp -r Desktop/Tesi/runtime-EclipseApplication/TestHierarchyRefactoring/bin ./ ");
			executeCommand("java test.TestRun");
			
		}
	}

	private void executeCommand(String command) {
		Process p = null;
		try {
			System.out.println("____________Command processed:  " + command);
			
			p = Runtime.getRuntime().exec(command);
			
			//System.out.println("toString: " + p.getOutputStream());
			
			BufferedReader in = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			
			BufferedReader in2 = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));
			String line2 = null;
			while ((line2 = in2.readLine()) != null) {
				System.out.println(line2);
			}
			

			p.waitFor();
			
			System.out.println("Exit value: " + p.exitValue());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void accomplishRefactoring(IJavaProject project) {
		try {
			PreprocessingFacade pf = new PreprocessingFacade();
			MethodHandler.clear();
			InstancesHandler ih = pf.parseSourceCode(project);

			//MultiObjective mo = new MultiObjective(ih, 30);
			SingleObjective so = new SingleObjective(ih);
			int numberOfIteration = 30;
			String resolutionMethod = "NSGA-II";

			MethodSelector ms = new MethodSelector(so, resolutionMethod, numberOfIteration, ih);
			List<List<ASTNode>> p = ms.selectInstances();

			List<CCRefactoring> refactorings = 
					CCRefactoring.selectRefactoringTechniques(ih, p, project);
			for(CCRefactoring ccr: refactorings)
				ccr.apply();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

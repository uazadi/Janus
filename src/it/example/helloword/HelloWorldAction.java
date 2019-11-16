package it.example.helloword;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import it.unimib.disco.essere.deduplicator.behaviouralcheck.CheckCompilation;
import it.unimib.disco.essere.deduplicator.behaviouralcheck.JUnitCheck;
import it.unimib.disco.essere.deduplicator.behaviouralcheck.MainClassCheck;
import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.PreprocessingFacade;
import it.unimib.disco.essere.deduplicator.rad.moea.MethodSelector;
import it.unimib.disco.essere.deduplicator.rad.moea.MultiObjective;
import it.unimib.disco.essere.deduplicator.rad.moea.SingleObjective;
import it.unimib.disco.essere.deduplicator.refactoring.CCRefactoring;
import it.unimib.disco.essere.deduplicator.versioning.GitVersioner;
import it.unimib.disco.essere.deduplicator.versioning.VersionerException;

@SuppressWarnings("restriction")
public class HelloWorldAction extends Action implements IWorkbenchWindowActionDelegate {

	private static final String JUNIT_NEW_TESTCASE_ID= "org.eclipse.jdt.junit.wizards.NewTestCaseCreationWizard";

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
			} else if (firstElement instanceof JavaProject) {
				JavaProject projectTmp = (JavaProject) firstElement;
				selectedProject = projectTmp.getJavaProject();
			}
			
			GitVersioner versioner = null;
			try {
				versioner = new GitVersioner(selectedProject);
			} catch (VersionerException e3) {
				// TODO Auto-generated catch blockaaaaa
				e3.printStackTrace();
			}
			
			try {
				versioner.newBranch("CCrefactoring");
			} catch (VersionerException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			List<ICompilationUnit> compUnitInvolved = accomplishRefactoring(selectedProject);
		 
			
			
			try {
				for(ICompilationUnit icu: compUnitInvolved) {
					icu.commitWorkingCopy(true, new NullProgressMonitor());
					icu.close();
				}
			} catch (JavaModelException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
//			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//			IEditorPart editor = page.getActiveEditor();
//			page.saveEditor(editor, false /* confirm */);
			
			try {
				for(ICompilationUnit icu: compUnitInvolved)
					icu.close();
				
			} catch (JavaModelException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
					
			List<String> pathOfCompUnitInvolved = new ArrayList<String>();
			for(ICompilationUnit icu: compUnitInvolved) {
				pathOfCompUnitInvolved.add(icu.getPath().toString());
			}
			
			try {
				versioner.commit(pathOfCompUnitInvolved);
			} catch (VersionerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			new CheckCompilation().check(selectedProject);

			try {
				Set<String> names = new HashSet<String>();
				names.add("it.unimib.disco.essere.core.InputParser");
				//names.add("test.TestRun");
				MainClassCheck mainCheck = new MainClassCheck(selectedProject, names);
				mainCheck.run();
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}

			JUnitCheck junitCheck = new JUnitCheck(selectedProject);
			Map<IType, Boolean> junitClasses = null;
			try {
				junitClasses = junitCheck.findJunitClasses();
				for(IType junitClass: junitClasses.keySet()) {
					if(!junitClass.getElementName().equals("TestCrossValidation"))
						junitClasses.replace(junitClass, false);
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			junitCheck.setJunitClasses(junitClasses);

			junitCheck.run();
			
//			try {
//				versioner.rollback();
//			} catch (VersionerException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}	

	private List<ICompilationUnit> accomplishRefactoring(IJavaProject project) {
		
		List<ICompilationUnit> compUnitInvolved = new ArrayList<ICompilationUnit>();
		
		try {
			PreprocessingFacade pf = new PreprocessingFacade();
			InstancesHandler ih = pf.parseSourceCode(project);

			//MultiObjective mo = new MultiObjective(ih, 30);
			SingleObjective so = new SingleObjective(ih);
			int numberOfIteration = 30;
			String resolutionMethod = "NSGA-II";

			MethodSelector ms = new MethodSelector(so, resolutionMethod, numberOfIteration, ih);
			List<List<ASTNode>> p = ms.selectInstances();

			List<CCRefactoring> refactorings = 
					CCRefactoring.selectRefactoringTechniques(ih, p, project);
			
			for(CCRefactoring ccr: refactorings) {
				ccr.apply();
				compUnitInvolved.addAll(ccr.getCompilationUnitInvolved());
			}
			
			ih.clear();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return compUnitInvolved;
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {}

	@Override
	public void dispose() {}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}

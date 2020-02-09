package it.unimib.disco.essere.janus.gui;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.json.JSONArray;
import org.json.JSONObject;

import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.preprocessing.PreprocessingFacade;


@SuppressWarnings("restriction")
public class HelloWorldAction extends Action implements IWorkbenchWindowActionDelegate, IViewActionDelegate {

	//private static final String JUNIT_NEW_TESTCASE_ID= "org.eclipse.jdt.junit.wizards.NewTestCaseCreationWizard";

	private IWorkbenchWindow window;
	private IJavaProject selectedProject;

	@Override
	public void run(IAction arg0) {
		//MessageDialog.openInformation(window.getShell(), "FirstPlugin", "Hello, World!");

		Configuration config = new Configuration();

		//		IStructuredSelection selection = 
		//				(IStructuredSelection) window.getSelectionService().getSelection();
		//		Object firstElement = selection.getFirstElement();

		this.window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IStructuredSelection selection = 
				((IStructuredSelection) window.getSelectionService().getSelection("org.eclipse.jdt.ui.PackageExplorer"));

		Object firstElement = selection.getFirstElement();

		if (firstElement instanceof IAdaptable) {
			if(firstElement instanceof Project) {
				Project projectTmp = (Project) firstElement;
				selectedProject = JavaCore.create(projectTmp);
			} else if (firstElement instanceof JavaProject) {
				JavaProject projectTmp = (JavaProject) firstElement;
				selectedProject = projectTmp.getJavaProject();
			}
		}
		config.selectedProject = this.selectedProject;
		

		IFile file = selectedProject.getProject().getFile(".janusignore");
		try {
			JSONObject tomJsonObj = new JSONObject();
			tomJsonObj.put("package", new JSONArray());
			tomJsonObj.put("class", new JSONArray());
			tomJsonObj.put("method", new JSONArray());
			tomJsonObj.put("keyword", new JSONArray());
			
			String str = tomJsonObj.toString(2);
			InputStream is = new ByteArrayInputStream(str.getBytes());
			
			file.create(is, true, null);

		} catch (CoreException e) {
			// File already exist
		}
		Utils.PROJECT = selectedProject.getProject();

		PreprocessingFacade pf = new PreprocessingFacade();
		try {
			InstancesHandler ih = pf.parseSourceCode(selectedProject);
			config.mainClasses = ih.getMainClasses();
			ih.clear();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		config.workbenchPage = page;

		HomePage window = new HomePage(config);
		window.launch();

	}

	//	private void myRun(Object firstElement) {
	//		if (firstElement instanceof IAdaptable) {
	//			if(firstElement instanceof Project) {
	//				Project projectTmp = (Project) firstElement;
	//				selectedProject = JavaCore.create(projectTmp);
	//			} else if (firstElement instanceof JavaProject) {
	//				JavaProject projectTmp = (JavaProject) firstElement;
	//				selectedProject = projectTmp.getJavaProject();
	//			}
	//			
	//			GitVersioner versioner = null;
	//			try {
	//				versioner = new GitVersioner(selectedProject);
	//			} catch (VersionerException e3) {
	//				e3.printStackTrace();
	//			}
	//			
	//			try {
	//				versioner.newBranch("CCrefactoring");
	//			} catch (VersionerException e2) {
	//				// TODO Auto-generated catch block
	//				e2.printStackTrace();
	//			}
	//			
	//			List<ICompilationUnit> compUnitInvolved = accomplishRefactoring(selectedProject);
	//		 
	//			
	//			
	//			try {
	//				for(ICompilationUnit icu: compUnitInvolved) {
	//					icu.commitWorkingCopy(true, new NullProgressMonitor());
	//					icu.close();
	//				}
	//			} catch (JavaModelException e2) {
	//				// TODO Auto-generated catch block
	//				e2.printStackTrace();
	//			}
	//			
	//			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	//			IEditorPart editor = page.getActiveEditor();
	//			page.saveEditor(editor, false /* confirm */);
	//			
	//			try {
	//				for(ICompilationUnit icu: compUnitInvolved)
	//					icu.close();
	//				
	//			} catch (JavaModelException e2) {
	//				// TODO Auto-generated catch block
	//				e2.printStackTrace();
	//			}
	//					
	//			List<String> pathOfCompUnitInvolved = new ArrayList<String>();
	//			for(ICompilationUnit icu: compUnitInvolved) {
	//				pathOfCompUnitInvolved.add(icu.getPath().toString());
	//			}
	//			
	//			try {
	//				versioner.commit(pathOfCompUnitInvolved);
	//			} catch (VersionerException e1) {
	//				// TODO Auto-generated catch block
	//				e1.printStackTrace();
	//			}
	//			
	//			new CheckCompilation().check(selectedProject);
	//
	//			try {
	//				Set<String> names = new HashSet<String>();
	//				names.add("it.unimib.disco.essere.core.InputParser");
	//				//names.add("test.TestRun");
	//				MainClassCheck mainCheck = new MainClassCheck(selectedProject, names);
	//				mainCheck.run();
	//			}catch(Exception e) {
	//				System.out.println(e.getMessage());
	//			}
	//
	//			JUnitCheck junitCheck = new JUnitCheck(selectedProject);
	//			Map<IType, Boolean> junitClasses = null;
	//			
	//			try {
	//				junitClasses = junitCheck.findJunitClasses();
	//				for(IType junitClass: junitClasses.keySet()) {
	//					if(!junitClass.getElementName().equals("TestCrossValidation"))
	//						junitClasses.replace(junitClass, false);
	//				}
	//			} catch (CoreException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
	//			
	//			junitCheck.setJunitClasses(junitClasses);
	//
	//			junitCheck.run();
	//			
	//			try {
	//				versioner.rollback();
	//			} catch (VersionerException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
	//		}
	//	}	
	//
	//	private List<ICompilationUnit> accomplishRefactoring(IJavaProject project) {
	//		
	//		List<ICompilationUnit> compUnitInvolved = new ArrayList<ICompilationUnit>();
	//		
	//		try {
	//			PreprocessingFacade pf = new PreprocessingFacade();
	//			InstancesHandler ih = pf.parseSourceCode(project);
	//
	//			//MultiObjective mo = new MultiObjective(ih, 30);
	//			SingleObjective so = new SingleObjective(ih);
	//			int numberOfIteration = 30;
	//			String resolutionMethod = "NSGA-II";
	//
	//			MethodSelector ms = new MethodSelector(so, resolutionMethod, numberOfIteration, ih);
	//			List<List<ASTNode>> p = ms.selectInstances();
	//
	//			List<CCRefactoring> refactorings = 
	//					CCRefactoring.selectRefactoringTechniques(ih, p, project);
	//			
	//			for(CCRefactoring ccr: refactorings) {
	//				ccr.apply();
	//				compUnitInvolved.addAll(ccr.getCompilationUnitInvolved());
	//			}
	//			
	//			ih.clear();
	//			
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//		
	//		return compUnitInvolved;
	//	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {}

	@Override
	public void dispose() {}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void init(IViewPart view) {
	}

}
package it.example.helloword;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.CharSet;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.core.LaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.junit.launcher.JUnit3TestFinder;
import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder;
import org.eclipse.jdt.internal.junit.launcher.JUnit5TestFinder;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.wizards.NewTypeDropDownAction;
import org.eclipse.jdt.internal.ui.wizards.NewTypeDropDownAction.OpenTypeWizardAction;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.internal.RealSystem;
import org.junit.internal.requests.ClassRequest;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import it.unimib.disco.essere.deduplicator.behaviouralcheck.CheckCompilation;
import it.unimib.disco.essere.deduplicator.behaviouralcheck.JUnitCheck;
import it.unimib.disco.essere.deduplicator.behaviouralcheck.MainClassCheck;
import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.MethodHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.PreprocessingFacade;
import it.unimib.disco.essere.deduplicator.rad.moea.MethodSelector;
import it.unimib.disco.essere.deduplicator.rad.moea.MultiObjective;
import it.unimib.disco.essere.deduplicator.rad.moea.SingleObjective;
import it.unimib.disco.essere.deduplicator.refactoring.CCRefactoring;
import it.unimib.disco.essere.deduplicator.versioning.GitVersioner;

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
			}else if (firstElement instanceof JavaProject) {
				JavaProject projectTmp = (JavaProject) firstElement;
				selectedProject = projectTmp.getJavaProject();
			}
			
			GitVersioner versioner = new GitVersioner(selectedProject);
			Git repo = ((Git) versioner.getRepo());
			
			versioner.newBranch("CCrefactoring");
			
			
			List<ICompilationUnit> compUnitInvolved = accomplishRefactoring(selectedProject);
		 
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IEditorPart editor = page.getActiveEditor();
			page.saveEditor(editor, false /* confirm */);
			

			
			List<String> pathOfCompUnitInvolved = new ArrayList<String>();
			for(ICompilationUnit icu: compUnitInvolved) {
				pathOfCompUnitInvolved.add(icu.getPath().toString());
			}

			versioner.commit(pathOfCompUnitInvolved);
			
			
			new CheckCompilation().check(selectedProject);

			try {
				MainClassCheck mainCheck = new MainClassCheck(selectedProject);
				mainCheck.run();
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}
			
			try {
				selectedProject.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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
		}
	}	

	private List<ICompilationUnit> accomplishRefactoring(IJavaProject project) {
		
		List<ICompilationUnit> compUnitInvolved = new ArrayList<ICompilationUnit>();
		
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
			
			for(CCRefactoring ccr: refactorings) {
				ccr.apply();
				compUnitInvolved.addAll(ccr.getCompilationUnitInvolved());
			}
			
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


	protected class ClientProjectClassLoader {

		private URLClassLoader innerCL;

		/** an automatic generated delegate method */
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			System.out.println("[ClientProjectClassLoader] innerCL find resource: " + innerCL.findResource(name));
			return innerCL.loadClass(name);
		}

		public ClientProjectClassLoader(ICompilationUnit c) {

			String unitPath = c.getResource().getProject().getLocationURI().toString();
			URL binURI = null;
			try {
				binURI = new URL(unitPath + "/target/test-classes/");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("[ClientProjectClassLoader] binURI: " + binURI.getPath());

			innerCL = new URLClassLoader(new URL[] { binURI });

			System.out.println("[ClientProjectClassLoader] binURI: " + innerCL);

		}

	}

}

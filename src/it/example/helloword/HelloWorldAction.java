package it.example.helloword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.junit.launcher.JUnit3TestFinder;
import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder;
import org.eclipse.jdt.internal.junit.launcher.JUnit5TestFinder;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.junit.internal.RealSystem;
import org.junit.internal.requests.ClassRequest;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

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
			//accomplishRefactoring(selectedProject);
			new CheckCompilation().check(selectedProject);

			try {
				System.out.println("Output location: " + selectedProject.getOutputLocation());
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			executeCommand("rm -r ./test ");
			executeCommand("cp -r Desktop/Tesi/runtime-EclipseApplication/TestHierarchyRefactoring/bin ./ ");
			executeCommand("java test.TestRun");

			try {
				Set<IType> retVal3 =  new HashSet<IType>();
				new JUnit3TestFinder().findTestsInContainer(selectedProject, retVal3, null);
				System.out.println("JUnit3TestFinder " + retVal3.size());

				Set<IType> retVal4 =  new HashSet<IType>();
				new JUnit4TestFinder().findTestsInContainer(selectedProject, retVal4, null);
				System.out.println("JUnit4TestFinder " + retVal4.size());

				Set<IType> retVal5 =  new HashSet<IType>();
				new JUnit5TestFinder().findTestsInContainer(selectedProject, retVal5, null);
				System.out.println("JUnit5TestFinder " + retVal5.size());

				IType[] retVal4arr = new IType[retVal4.size()];
				retVal5.toArray(retVal4arr);
				System.out.println("retVal5arr[0].getClass() ==== " + retVal4arr[4].getClass());
				System.out.println("retVal5arr[0].getElementName() ==== " + retVal4arr[4].getFullyQualifiedName());

				String name = retVal4arr[4].getFullyQualifiedName();
				String path = retVal4arr[4].getFullyQualifiedName().replace(".", "/");
				System.out.println("Path: " + path);


				Class<?> act = null;
				try {
					
//					System.out.println("Bundle ID:" + Activator.getDefault());
//					
//					String bundleID =  Activator.getDefault().getBundleId(selectedProject);
//					act = Activator.getDefault().getBundle(bundleID).loadClass(name);

					
					
					ICompilationUnit anUnit = selectedProject.findType(retVal4arr[4].getFullyQualifiedName()).getCompilationUnit();

					ClientProjectClassLoader cpcl = new ClientProjectClassLoader(anUnit, Activator.getDefault().getDescriptor().getPluginClassLoader( ));
					Activator.getDefault().getDebugOptions()
					
					Class myClass = cpcl.loadClass(className);

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//				 try {
				//					    act = Class.forName(retVal4arr[4].getFullyQualifiedName());
				//					 } catch (ClassNotFoundException e) {
				//					        e.printStackTrace();
				//					}


				Result a = 
						//JUnitCore.runClasses(retVal4arr[3].getClass());
						//new JUnitCore().run(retVal4arr[3].getClass());
						//new JUnitCore().run(new Computer(), new ClassRequest(retVal4arr[3].getClass()));
						//new JUnitCore().run(new Computer(), retVal4arr[3].getClass());
						new JUnitCore().run(new Computer(), act);

				System.out.println("Was succesfull? " + a.wasSuccessful());
				for(Failure f: a.getFailures()) {
					System.out.println("[MESSAGE] " + f.getMessage());
					System.out.println("[DESCRIPTION] " + f.getDescription());
					System.out.println("[EXCEPTION] ");
					f.getException().printStackTrace();
				}





			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
	
	
	protected class ClientProjectClassLoader {

		private URLClassLoader innerCL;

		/** an automatic generated delegate method */
		public Class<?> loadClass(String name) throws ClassNotFoundException {
		return innerCL.loadClass(name);
		}

		public ClientProjectClassLoader(ICompilationUnit c, ClassLoader parent) {
		String unitPath = c.getResource().getProject().getLocationURI()
		.toString();
		URL binURI = null;
		try {
		binURI = new URL(unitPath + "/bin/");
		} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		innerCL = new URLClassLoader(new URL[] { binURI }, parent);
		}

	}
	
}

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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.swt.graphics.Image;
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
import it.unimib.disco.essere.deduplicator.behaviouralcheck.JUnitCheck;
import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.MethodHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.PreprocessingFacade;
import it.unimib.disco.essere.deduplicator.rad.moea.MethodSelector;
import it.unimib.disco.essere.deduplicator.rad.moea.MultiObjective;
import it.unimib.disco.essere.deduplicator.rad.moea.SingleObjective;
import it.unimib.disco.essere.deduplicator.refactoring.CCRefactoring;

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
			//accomplishRefactoring(selectedProject);
			new CheckCompilation().check(selectedProject);

			try {
				System.out.println("Output location: " + selectedProject.getOutputLocation());
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			IVMInstall vm;
			ILaunch launch = null;
			try {
				vm = JavaRuntime.getVMInstall(selectedProject);
				//vm = JavaRuntime.getDefaultVMInstall (); 
				//				if (vm == null) {
				//					vm = JavaRuntime.getDefaultVMInstall (); 
				//				}
				IVMRunner vmr = vm.getVMRunner (ILaunchManager.RUN_MODE);
				String[] cp = JavaRuntime.computeDefaultRuntimeClassPath (selectedProject);

				for(String x: cp)
					System.out.println("Cp__________________________ " + x);

				VMRunnerConfiguration config = new VMRunnerConfiguration("test.TestRun", cp);

				//				String[] env = {"CLASSPATH=" + cp[0]};
				//				config.setEnvironment(env);

				//config.setModulepath(env);

				String[] args = {};//{"java", "test.TestRun"};
				config.setProgramArguments (args);
				launch = new Launch (null, ILaunchManager.RUN_MODE, null);
				//vmr.run (config, launch, null);

				//executeCommand("ls");

				String s = vmr.showCommandLine(config, launch, new NullProgressMonitor());
				
				Thread.sleep(250);

				executeCommand(s);

				System.out.println(s);
				//			
				//executeCommand("/usr/lib/jvm/java-8-oracle/bin/java -classpath /home/umberto/Desktop/Tesi/runtime-EclipseApplication/TestHierarchyRefactoring/bin test.Subclass2");
				//executeCommand("/usr/lib/jvm/java-8-oracle/bin/java -classpath /home/umberto/Desktop/Tesi/runtime-EclipseApplication/TestHierarchyRefactoring/bin test.TestRun");
				//				System.out.println("String showCommandLine:  " + s);

			} catch (CoreException e1) {
				e1.printStackTrace();
			}
			//			
			//System.out.println("Lanch Process size:  " + launch.getProcesses().length);
 catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//			while(!launch.isTerminated()) {
			//				try {

			//					Thread.sleep(250);
			//				} catch (InterruptedException e) {
			//					e.printStackTrace();
			//				}
			//				System.out.println("WAIT THE Launch!");
			//			}

			//
			try {
				for(IProcess a: launch.getProcesses()) {
					while(!a.isTerminated()) {
						Thread.sleep(250);
						System.out.println("WAIT THE PROCESS!");
					}
					System.out.println("Exit value:   " + a.getExitValue());
					System.out.println("Error Stream  " + a.getStreamsProxy().getErrorStreamMonitor().getContents());
					System.out.println("Output Stream " + a.getStreamsProxy().getOutputStreamMonitor().getContents());
				}
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			//			executeCommand("rm -r ./test ");
			//			executeCommand("cp -r Desktop/Tesi/runtime-EclipseApplication/TestHierarchyRefactoring/bin ./ ");
			//			executeCommand("java test.TestRun");

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

	private void executeCommand(String command) {

		Process p;
		try {
			p = new ProcessBuilder(command.split(" ")).start();
			final int retval = p.waitFor();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				System.out.println("$$" + line);
			}

			BufferedReader in2 = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));
			String line2 = null;
			while ((line2 = in2.readLine()) != null) {
				System.out.println("!!" + line2);
			}


			p.waitFor();

			System.out.println(">>>Exit value: " + p.exitValue());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


//		CommandLine commandLine = CommandLine.parse(command);
//		DefaultExecutor executor = new DefaultExecutor();
//		try {
//			int exitValue = executor.execute(commandLine);
//			System.out.println("+++++Exit value: " + exitValue);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}



		//		Process p = null;
		//		try {
		//			System.out.println("____________Command processed:  " + command);
		//
		//			p = Runtime.getRuntime().exec(command.split(" "));
		//
		//			//System.out.println("toString: " + p.getOutputStream());
		//
		//			BufferedReader in = new BufferedReader(
		//					new InputStreamReader(p.getInputStream()));
		//			String line = null;
		//			while ((line = in.readLine()) != null) {
		//				System.out.println("$$" + line);
		//			}
		//
		//			BufferedReader in2 = new BufferedReader(
		//					new InputStreamReader(p.getErrorStream()));
		//			String line2 = null;
		//			while ((line2 = in2.readLine()) != null) {
		//				System.out.println("!!" + line2);
		//			}
		//
		//
		//			p.waitFor();
		//
		//			System.out.println(">>>Exit value: " + p.exitValue());
		//
		//
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
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

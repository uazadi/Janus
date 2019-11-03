package it.unimib.disco.essere.deduplicator.behaviouralcheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public class MainClassCheck extends BehavioralCheck {

	/** 
	 * The set of the fully qualified names of the 
	 * classes containing a main method that has to
	 * be executed successfully in order to pass
	 * this behavioral check
	 */
	private Set<String> mainClassesNames;

	public MainClassCheck(IJavaProject selectedProject) {
		super(selectedProject);
		this.mainClassesNames = new HashSet<String>();
	}

	public MainClassCheck(IJavaProject selectedProject, 
			Set<String> mainClassesFullyQualifiedNames) {
		super(selectedProject);
		this.mainClassesNames = mainClassesFullyQualifiedNames;
	}

	public void addMainClassName(String mainClassFullyQualifiedName){
		this.mainClassesNames.add(mainClassFullyQualifiedName);
	} 

	public boolean run() throws BehevioralCheckException {
		IVMInstall vm;
		ILaunch launch = null;

		for(String name: this.mainClassesNames) {
			try {
				int exitValue = runSingleClass(name);

				if(exitValue != 0) {
					return false;
				}

			} catch (CoreException | InterruptedException | IOException e) {
				throw new BehevioralCheckException(
						e.getMessage());
			}
		}

		return true;
	}

	private int runSingleClass(String name) 
			throws CoreException, InterruptedException, ExecuteException, IOException {
		IVMInstall vm;
		ILaunch launch;
		vm = JavaRuntime.getVMInstall(selectedProject);
		IVMRunner vmr = vm.getVMRunner (ILaunchManager.RUN_MODE);
		String[] cp = JavaRuntime.computeDefaultRuntimeClassPath (selectedProject);

		VMRunnerConfiguration config = new VMRunnerConfiguration(name, cp);

		String[] args = {};
		config.setProgramArguments (args);
		launch = new Launch (null, ILaunchManager.RUN_MODE, null);

		String commandLineString = vmr.showCommandLine(config, launch, new NullProgressMonitor());

		// wait for showCommandLine 
		Thread.sleep(250);

		System.out.println("Command Line executed: " + commandLineString);
		int exitValue = executeCommand(commandLineString);
		return exitValue;
	}

	private int executeCommand(String command) throws ExecuteException, IOException {

		Process p;
		int exitValue = 1;

		CommandLine commandLine = CommandLine.parse(command);
		DefaultExecutor executor = new DefaultExecutor();	

		exitValue = executor.execute(commandLine);
		System.out.println("+++++Exit value: " + exitValue);	

		return exitValue;
	}

}

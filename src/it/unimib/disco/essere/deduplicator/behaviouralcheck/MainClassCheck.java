package it.unimib.disco.essere.deduplicator.behaviouralcheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
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

public class MainClassCheck extends BehaviouralCheck {
	
	
	public MainClassCheck(IJavaProject selectedProject) {
		super(selectedProject);
	}

	public boolean run() {
		IVMInstall vm;
		ILaunch launch = null;
		try {
			vm = JavaRuntime.getVMInstall(selectedProject);
			IVMRunner vmr = vm.getVMRunner (ILaunchManager.RUN_MODE);
			String[] cp = JavaRuntime.computeDefaultRuntimeClassPath (selectedProject);
			
			//String name = "it.unimib.disco.essere.core.InputParser";
			String name = "test.TestRun";

			VMRunnerConfiguration config = new VMRunnerConfiguration(name, cp);

			String[] args = {};
			config.setProgramArguments (args);
			launch = new Launch (null, ILaunchManager.RUN_MODE, null);

			String commandLineString = vmr.showCommandLine(config, launch, new NullProgressMonitor());

			// wait for show command
			Thread.sleep(250);

			System.out.println("Command Line executed: " + commandLineString);
			int exitValue = executeCommand(commandLineString);
			
			if(exitValue != 0) {
				return false;
			}

		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private int executeCommand(String command) {

		Process p;
		int exitValue = 1;
		
		CommandLine commandLine = CommandLine.parse(command);
		DefaultExecutor executor = new DefaultExecutor();	
		try {	
			exitValue = executor.execute(commandLine);
			System.out.println("+++++Exit value: " + exitValue);	
		} catch (IOException e) {	
			// TODO Auto-generated catch block	
			e.printStackTrace();	
		}
		
		return exitValue;

	}
	
}

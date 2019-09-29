package it.unimib.disco.essere.deduplicator.behaviouralcheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.junit.launcher.JUnit3TestFinder;
import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder;
import org.eclipse.jdt.internal.junit.launcher.JUnit5TestFinder;
import org.eclipse.jdt.launching.JavaRuntime;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class BehaviouralChecks {
	
	public boolean checkCompilation() {
		return false;
	}
	
	

	/**
	 * @param fullPathBytecode The absolute path of the folder in which all the .class file are contained
	 * @param className        The name of the class, __conplite with the package name__ of the main class that 
	 * @return
	 */
	public boolean checkMainClass(IJavaProject project, String className) {
		
		try {
			System.out.println("Output location: " + project.getOutputLocation());
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		executeCommand("rm -r ./test ");
		executeCommand("cp -r Desktop/Tesi/runtime-EclipseApplication/TestHierarchyRefactoring/bin ./ ");
		executeCommand("java test.TestRun");
		return false;
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
	
	public boolean checkJUnitClass() {
		return false;
	}
}

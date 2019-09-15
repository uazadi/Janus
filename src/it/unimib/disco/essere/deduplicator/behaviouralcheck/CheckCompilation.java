package it.unimib.disco.essere.deduplicator.behaviouralcheck;

import java.io.PrintWriter;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

public class CheckCompilation {

	public boolean check(IJavaProject project) {
		try {
			IProject myProject = project.getProject();
			System.out.println("_______________________________________________isOpen:" + myProject.isOpen());
			
			
			
			myProject.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
			
//			for(IClasspathEntry icp: project.getRawClasspath()) {
//				System.out.println("_________________________IClasspathEntry" + icp.);
//			}
//			
//			org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(
//					   "-classpath ",
//					   new PrintWriter(System.out),
//					   new PrintWriter(System.err),
//					   null);
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}	
}

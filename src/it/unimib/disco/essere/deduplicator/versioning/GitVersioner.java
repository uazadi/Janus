package it.unimib.disco.essere.deduplicator.versioning;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitVersioner extends Versioner {
	
	Git repo;

	public GitVersioner(IJavaProject verionedProject) {
		super(verionedProject);
		
		File projectPath = new File(getProjectPath(verionedProject));
		
		verionedProject.getPath().toFile();
		
		try {
			repo = Git.open(projectPath);
		} catch (IOException e2) {
			try {
				Git.init().setDirectory(projectPath).call();
				repo = Git.open(projectPath);
			} catch (IllegalStateException | GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	
	private String getProjectPath(IJavaProject verionedProject) {
		String projectPath = "";
		try {
			String name = verionedProject.getElementName();
			String[] cp = JavaRuntime.computeDefaultRuntimeClassPath (verionedProject);
			int index = cp[0].lastIndexOf(name);
			projectPath = cp[0].substring(0, index) + name + "/";
		} catch (CoreException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		return projectPath;
	}


	@Override
	public void commit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollback() {
		// TODO Auto-generated method stub

	}


	@Override
	public Object getRepo() {
		return repo;
	}

}

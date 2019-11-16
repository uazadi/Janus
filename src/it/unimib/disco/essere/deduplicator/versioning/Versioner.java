package it.unimib.disco.essere.deduplicator.versioning;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

public abstract class Versioner {
	
	public static final String DEFAULT_BRANCH_NAME = 
			"Code_clones_refactoring";
	
	
	
	public Versioner(IJavaProject verionedProject) {}
	
	public abstract Object getRepo();
	
	public abstract void newBranch(String branchName) 
			throws VersionerException;
	
	public abstract void newBranch() 
			throws VersionerException;
	
	public abstract void commit(List<String> compilationUnitToCommit)
			 throws VersionerException;
	
	public abstract void rollback()  
			throws VersionerException ;

}

package it.unimib.disco.essere.deduplicator.versioning;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

public abstract class Versioner {
	
	public Versioner(IJavaProject verionedProject) {}
	
	public abstract Object getRepo();
	
	public abstract void newBranch(String branchName);
	
	public abstract void commit(List<String> compilationUnitToCommit);
	
	public abstract void rollback();

}

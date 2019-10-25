package it.unimib.disco.essere.deduplicator.versioning;

import org.eclipse.jdt.core.IJavaProject;

public abstract class Versioner {
	
	public Versioner(IJavaProject verionedProject) {}
	
	public abstract Object getRepo();
	
	public abstract void commit();
	
	public abstract void rollback();

}

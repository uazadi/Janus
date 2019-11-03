package it.unimib.disco.essere.deduplicator.behaviouralcheck;

import org.eclipse.jdt.core.IJavaProject;

public abstract class BehavioralCheck {
	
	protected IJavaProject selectedProject;
	
	public BehavioralCheck(IJavaProject selectedProject) {
		this.selectedProject = selectedProject;
	}

	public abstract boolean run() throws BehevioralCheckException;
	
}

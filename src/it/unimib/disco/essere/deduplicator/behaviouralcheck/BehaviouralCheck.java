package it.unimib.disco.essere.deduplicator.behaviouralcheck;

import org.eclipse.jdt.core.IJavaProject;

public abstract class BehaviouralCheck {
	
	protected IJavaProject selectedProject;
	
	public BehaviouralCheck(IJavaProject selectedProject) {
		this.selectedProject = selectedProject;
	}

	public abstract boolean run();
	
}

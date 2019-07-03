package it.unimib.disco.essere.deduplicator.preprocessing.modelling;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public interface JavaComponent {
	
	
	public JavaContainer getParent();
	
	/** @return the list of all the children, where:
	 * 		JavaFiles are children of JavaDirectory; 
	 * 		JavaClasses are children of JavaFile; 
	 * 		JavaMethods are children of JavaClass;
	 * 		JavaStatement are children of JavaMethod;
	 * */
	public List<JavaComponent> getChildren();
	
	public String getName();
	
	public ASTNode getNode();
}

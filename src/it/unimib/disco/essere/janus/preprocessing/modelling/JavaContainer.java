package it.unimib.disco.essere.janus.preprocessing.modelling;

import java.util.List;

public abstract class JavaContainer implements JavaComponent{ 
	
	/** The children of this node,  where:
	 * 		JavaFiles are children of JavaDirectory; 
	 * 		JavaClasses are children of JavaFile; 
	 * 		JavaMethods are children of JavaClass. 
	 * 		JavaStatement are children of JavaMethod;
	 * */
	protected List<JavaComponent> children;
	
	/**
	 * Extract the children, where:
	 * 		JavaFiles are children of JavaDirectory; 
	 * 		JavaClasses are children of JavaFile; 
	 * 		JavaMethods are children of JavaClass.
	 * 		JavaStatement are children of JavaMethod;
	 **/
	protected abstract List<JavaComponent> extractChildren() throws Exception; 
	
	@Override
	public List<JavaComponent> getChildren() {
		return children;
	}
}

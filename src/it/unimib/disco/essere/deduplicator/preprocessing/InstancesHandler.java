package it.unimib.disco.essere.deduplicator.preprocessing;

import java.util.LinkedHashSet;
import java.util.Set;

public interface InstancesHandler {

	public void setMinNumLines(int minNumLines);

	/**
	 * @param methodID the identifier of the method
	 * @return the instance of the method identified by methodID
	 * @throws Exception if the method is not found
	 */
	public Instance getMethod(int methodID);

	public int getNumOfInstance();
	
	public void clear();

	/**
	 * @param javaClassName the name of the subClass (it can be accessed through the Java Class method "getName()")
	 * @return the name of the superclass
	 */
	public String getSuperClassName(String javaClassName);


	/**
	 * To be called ONLY when all the classes has been added!
	 * (otherwise the superclasses paths generated could be incomplete).
	 * 
	 * It will return a list containing:
	 * <ul>
	 * 		<li> the custom classes defined in software system </li>
	 * 		<li> the first NOT custom class extended by a custom classes </li>
	 * </ul>
	 * 
	 * (see the "getHierarchyPath" JavaDoc for example)
	 * 
	 * @param 	javaClassName the name of the subClass (it can be accessed through the Java Class method "getName()")
	 * @return 	list of names of all superclasses (ordered) iff the javaClassName has been obtained through the 
	 * 			"getName()" method of a JavaClass instance, NULL otherwise 
	 */
	public LinkedHashSet<String> getHierarchyPath(String javaClassName);

	/**
	 * To be called ONLY when all the classes has been added!
	 * (otherwise the superclasses paths generated could be incomplete).
	 * 
	 * This method find the "local" nearest common superclass,
	 * meaning that it is able only to locate:
	 * <ul>
	 * 		<li> the custom classes defined in software system </li>
	 * 		<li> the first NOT custom class extended by a custom classes </li>
	 * </ul>
	 * 
	 * (see the "getHierarchyPath" JavaDoc for example)
	 * 
	 * @param firstClassName	the name of the first class
	 * @param secondClassName	the name of the second class
	 * @return the "local" nearest common superclass, NULL otherwise
	 */
	public String findNearestCommonSuperclass(String firstClassName, String secondClassName);
	
	public Set<String> getMainClasses();

}

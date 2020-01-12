package it.unimib.disco.essere.janus.preprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimib.disco.essere.janus.preprocessing.exceptions.MethodNotFoundException;

public class MethodHandler implements InstancesHandler{
	private static MethodHandler instance;

	/** The methods expressed as pair <index, method> 
	 *  that can be selected for refactoring, i.e. the 
	 *  so-called set of worth normalized methods*/
	private HashMap<Integer, Instance> methods;

	/** Variable that keep count of the number of the method 
	 *  and it is use to know the next available index. */
	private int lowerAvaibleIndex; 

	/** All the classes expressed as pair <name of a class, name of superclass> 
	 *  (N.B. if a class doesn't have a superclass the default value is java.lang.Object)
	 */
	private Map<String, String> hierarchyMap;

	/** All the classes expressed as pair <name of a class, list of names of all superclasses (ordered)>,
	 *  where each class is considered also superclass of itself.
	 *  (N.B. all classes have at least a superclass, which is java.lang.Object)
	 */
	private HashMap<String, LinkedHashSet<String>> hierarchyFullPathMap;
	
	/** List of the fully qualified names of all the classes that contain
	 *  a main method.
	 * */
	private Set<String> mainClasses; 

	private int minNumLines = 2;

	private MethodHandler() {
		this.methods = new HashMap<>();
		this.hierarchyMap = new HashMap<>();
		//this.hierarchyFullPathMap = new HashMap<String, LinkedHashSet<String>>();
		this.mainClasses = new HashSet<>();
		this.lowerAvaibleIndex = 0;
	}

	/**
	 * @return the only instance allowed for this class
	 */
	public static synchronized MethodHandler getInstance() {
		if (instance == null) {
			instance = new MethodHandler();
		}
		return instance;
	}
	
	@Override
	public void clear() {
		instance = new MethodHandler();
	}

	public void setMinNumLines(int minNumLines) {
		this.minNumLines = minNumLines;
	}

	public int registerInstance(Instance ins) { 
		methods.put(lowerAvaibleIndex, ins);
		String superClass = null;
		if("java.lang.Object".equals(ins.getSuperClassName()))
			superClass = "java.lang.Object";
		else 
			superClass = ins.getFullSuperClassName();
		hierarchyMap.put(ins.getClassName(), superClass);
		
		if(ins.isMain())
			this.mainClasses.add(ins.getClassName());
		
		lowerAvaibleIndex++;
		return lowerAvaibleIndex - 1; // -1 because I need to increment the counter before the return
	}

	/**
	 * @param method the method that has to be added
	 * @return the method ID
	 */
//	public int registerMethod(JavaMethod method) { 
//		if(method.getCharFreqInBody('\n') > this.minNumLines) {
//		}
//		// -1 because I need to increment the counter before the return
//		return (lowerAvaibleIndex - 1);
//	}

	/**
	 * @param methodID the identifier of the method
	 * @return the instance of the method identified by methodID
	 * @throws Exception if the method is not found
	 */
	public Instance getMethod(int methodID){
		Instance method = methods.get(methodID);
		if(method == null)
			throw new MethodNotFoundException(methodID);
		return method;
	}

	public int getNumOfInstance() {	
		return methods.size();
	}

	/**
	 * @param javaClassName the name of the subClass (it can be accessed through the Java Class method "getName()")
	 * @return the name of the superclass
	 */
	public String getSuperClassName(String javaClassName) {
		return hierarchyMap.get(javaClassName);
	}

	/**
	 * To be called ONLY when all the classes has been added!
	 * (otherwise the superclasses paths generated could be incomplete)
	 * 
	 * It is able only to locate:
	 * <ul>
	 * 		<li> the custom classes defined in software system </li>
	 * 		<li> the first NOT custom class extended by a custom classes </li>
	 * </ul>
	 * 
	 * <b>Example:</b><br>	
	 * CustomException extends AnotherCustomException extends (java.lang.)RuntimeException extends (java.lang.)Exception<br>
	 * then the list related to CustomException will be: "CustomException -> AnotherCustomException -> RuntimeException"<br><br>
	 */
	private void generateFullHierarchyPaths() {
		hierarchyFullPathMap = new HashMap<>();
		for(String javaClass: hierarchyMap.keySet()) {
			newEntry(javaClass);
			addHierarchyPath(javaClass);
		}
	}

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
	public LinkedHashSet<String> getHierarchyPath(String javaClassName){
		if(hierarchyFullPathMap == null) 
			generateFullHierarchyPaths();
		return hierarchyFullPathMap.get(javaClassName);
	}

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
	public String findNearestCommonSuperclass(String firstClassName, String secondClassName) {
		if(hierarchyFullPathMap == null) 
			generateFullHierarchyPaths();

		LinkedHashSet<String> firstHierPath = getHierarchyPath(firstClassName);
		LinkedHashSet<String> secondHierPath = getHierarchyPath(secondClassName);

//		System.out.println("11111 " + firstHierPath);
//		System.out.println("22222 " + secondHierPath);

		if(firstHierPath == null || secondHierPath == null) {
			return null;
		}

		for(String superClass: firstHierPath) {
			//System.out.println(superClass);
			if(secondHierPath.contains(superClass))
				return superClass;
		}

		return null;
	}

	private void newEntry(String javaClass) {
		hierarchyFullPathMap.put(javaClass, new LinkedHashSet<String>());
	}

	private void addHierarchyPath(String javaClass) {
		// add the class itself
		hierarchyFullPathMap.get(javaClass).add(javaClass);

		// add all the superclasses
		String superClassName = getSuperClassName(javaClass);
		while(superClassName != null) {
			hierarchyFullPathMap.get(javaClass).add(superClassName);
			superClassName = getSuperClassName(superClassName);
		}
	}

	@Override
	public Set<String> getMainClasses() {
		return this.mainClasses;
	}
}

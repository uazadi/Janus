package it.unimib.disco.essere.janus.preprocessing.modelling;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class JavaClass extends JavaContainer{

	/** The default value will determined the way in which the refactoring
	 *  is accomplish. If java.lang.Object is selected to be the default value
	 *  the ExtractSuperclass refactoring technique will be always feasible
	 *  because there will be always a common superclass
	 * */
	private static String DEFAULT_SUPERCLASS_VALUE = ""; // "java.lang.Object";
	
	/** The name of the class */
	private String name;

	/** The superclass of the class, 
	 * i.e. the class from which this class inherit the code. 
	 * (Default value: java.lang.Object)*/
	private String superClass;

	/** The class that allows to work with the Abstract Syntax Tree of the class*/
	private TypeDeclaration node;
	
	/** The file in which the class is contained */
	private JavaFile file;

	/**
	 * Create an instance the class Class and create an instance of all its children (JavaMethod) 
	 * 
	 * @param ast	the TypeDeclaration, i.e. the AST node related to this class
	 * @throws Exception   
	 */
	public JavaClass(TypeDeclaration ast, JavaFile parent) throws Exception { 
		this.node 		= ast;
		this.file 		= parent;
		this.name 		= parent.getPackage() + "." + node.getName().getIdentifier();
		this.superClass = extractSuperClass();
		this.children 	= extractChildren();
	}

	@Override
	public String getName() {
		return name;
	}

	public String getSuperClass() {
		return superClass;
	}

	@Override
	public JavaContainer getParent() {
		return file;
	}

	@Override
	public ASTNode getNode() {
		return node;
	}

	@Override
	public String toString() {
		return "Class [Name = " + name + ", SuperClass = " 
				+ superClass + ", File = " + file.getName() + "]";
	}
	
	/**
	 * @param obj the object which equality must be controlled
	 * @return 	true if the attributes "name" of the two JavaClasses are equal, 
	 * 				 or "obj" is a String equal to the attribute "name"; 
	 * 			false otherwise.
	 */
	public boolean equals(Object obj) {
		if(obj instanceof JavaClass)
			return this.name.equals(((JavaClass) obj).getName());
		return false;
	}

	private String extractSuperClass() {
		if(node.getSuperclassType() != null)
			return node.getSuperclassType().toString();
		return DEFAULT_SUPERCLASS_VALUE;
	}

	@Override
	protected List<JavaComponent> extractChildren() throws Exception {
		ArrayList<JavaComponent> methods = new ArrayList<JavaComponent>();
		MethodDeclaration[] methodsNode = node.getMethods();
		for(int i=0; i<methodsNode.length; i++) {
			methods.add(new JavaMethod(methodsNode[i], this));
		}
		return methods;
	}

}

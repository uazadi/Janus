package it.unimib.disco.essere.janus.preprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import it.unimib.disco.essere.janus.preprocessing.modelling.JavaClass;
import it.unimib.disco.essere.janus.preprocessing.modelling.JavaComponent;
import it.unimib.disco.essere.janus.preprocessing.modelling.JavaFile;
import it.unimib.disco.essere.janus.preprocessing.modelling.JavaMethod;

public class Method implements Instance{
	
	
	private ASTNode astNode;
	
	/** The path of the .java file */
	private String javaFilePath;
	
	/** The name of the class */
	private String className;

	/** The superclass of the class, 
	 * i.e. the class from which this class inherit the code. 
	 * (Default value: java.lang.Object)*/
	private String superClassName;
	
	/** The name of method */
	private String methodName;

	/** The list of parameter required by the class */
	private List<String> methodParameters; 

	/** The return type of the method if it isn't a constructor, null otherwise*/
	private String methodReturnType;

	/** The body of the method, i.e. the statements contained in the method */
	private String methodBody;

	/** The concatenation of all the comments related to the method
	 * (i.e. both JavaDOC and internal comments) */
	private String methodComment;
	
	private String fullSuperClassName;
	
	/** The identifier of the method*/
	private int methodID;
	
	private long numLines;

	/** The name of all the variable used in the method
	 *  (class fields, parameters and local variables)*/
	private List<String> variableNames;

	/** The type of all the fields of the class in which 
	 * the method is defined and all the type of the 
	 * variables declared within the method body.*/
	private Set<String> variableTypes;
	
	/** The values of all the constant used in the method
	 *  (integer string, boolean, char, null, static field)*/
	private List<String> constantValues;
	
	/**
	 * <Pased statement, ASTNode of the statement>
	 * */
	private List<Pair<String, ASTNode>> statements;
	
	private List<String> parsedStatements;
	
	private boolean isMain;
	
	
	public Method(JavaMethod method) {
		
		this.astNode = method.getNode();
		
		this.javaFilePath = method.getParent().getParent().getName();
		this.className = method.getParent().getName();
		this.superClassName = method.getParent().getSuperClass();
		this.methodName = method.getName();
		this.methodParameters = method.getParameters();
		this.methodReturnType = method.getReturnType();
		this.methodBody = method.getBody();
		this.methodComment = method.getComment();
		//this.methodID = method.getID();
		this.variableNames = method.getAllVariableNames();
		this.variableTypes = method.getAllVariableTypes();
		this.constantValues = method.getAllConstantValue(method.getParent());
		this.fullSuperClassName = this.buildFullSuperClassName(method.getParent());
		this.numLines = method.getCharFreqInBody('\n');
		this.parsedStatements = new ArrayList<String>();
		this.isMain = method.isMain();
	
		this.methodID = MethodHandler.getInstance().registerInstance(this);
		
		this.statements = new ArrayList<Pair<String, ASTNode>>();
		for(JavaComponent stmt: method.getChildren()) {
			this.parsedStatements.add(stmt.getName());
			this.statements.add(
					new ImmutablePair<String, ASTNode>(
							stmt.getName(), 
							stmt.getNode()));
		}	
	}

	public ASTNode getAstNode() {
		return astNode;
	}

	public String getJavaFilePath() {
		return javaFilePath;
	}


	public String getClassName() {
		return className;
	}


	public String getSuperClassName() {
		return superClassName;
	}


	public String getMethodName() {
		return methodName;
	}


	public List<String> getMethodParameters() {
		return methodParameters;
	}


	public String getMethodReturnType() {
		return methodReturnType;
	}


	public String getMethodBody() {
		return methodBody;
	}


	public String getMethodComment() {
		return methodComment;
	}


	public int getMethodID() {
		return methodID;
	}
	


	public List<String> getVariableNames() {
		return variableNames;
	}


	public Set<String> getVariableTypes() {
		return variableTypes;
	}


	public List<String> getConstantValues() {
		return constantValues;
	}


	public List<String> getParsedStatement() {
		return parsedStatements;
	}
	
	public String getFullSuperClassName() {
		return fullSuperClassName;
	}
	
	
	private String buildFullSuperClassName(JavaClass javaClass) {
		String superClass = null;
		JavaFile javaFile = (JavaFile) javaClass.getParent();
		
		// Search for an import with the superclass name
		for(String imp: javaFile.getImports()) {
			if(imp.contains(javaClass.getSuperClass())){
				superClass = imp.replace("\n", "");
			}
		}
		
		// If there isn't an import for the super class
		// then it has to be in the same package
		if(superClass == null && javaClass.getSuperClass() != "") {
			String name = javaClass.getName();
			superClass = name.substring(0, name.lastIndexOf(".")) + "." + javaClass.getSuperClass(); 
		}
		
		return superClass;
	}


	public long getNumLines() {
		return numLines;
	}


	public void setNumLines(long numLines) {
		this.numLines = numLines;
	}

	/* List of statement, most likely the list will contain 
	 * just one node, except when the method contains several
	 * duplicated statements.
	 * */
	public List<ASTNode> getStatement(String parsedStatement) {
		List<ASTNode> nodes = new ArrayList<ASTNode>();
		for(Pair<String, ASTNode> stmt: this.statements) {
			if(stmt.getKey().equals(parsedStatement))
				nodes.add(stmt.getValue());
		}
		return nodes;
	}

	public boolean isMain() {
		return isMain;
	}
	
}

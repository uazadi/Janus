package it.unimib.disco.essere.deduplicator.preprocessing.modelling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Statement;

import it.unimib.disco.essere.deduplicator.preprocessing.Method;
import it.unimib.disco.essere.deduplicator.preprocessing.modelling.JavaStatement;
import it.unimib.disco.essere.deduplicator.preprocessing.visitors.ConstantValuesVisitor;
import it.unimib.disco.essere.deduplicator.preprocessing.visitors.CustomAstVisitor;
import it.unimib.disco.essere.deduplicator.preprocessing.visitors.VariableNamesVisitor;

public class JavaMethod extends JavaContainer {

	/** The instance of JavaClass that represent the class in which the method is defined,
	 * it will be not iff the instance has been created through 
	 * the PopulationExtractor workflow */
	private JavaClass javaClass;

	/** The name of method */
	private String name;

	/** The list of parameter required by the class */
	private List<String> parameters; 

	/** The return type of the method if it isn't a constructor, null otherwise*/
	private String returnType;

	/** The body of the method, i.e. the statements contained in the method */
	private String body;

	/** The concatenation of all the comments related to the method
	 * (i.e. both JavaDOC and internal comments) */
	private String comment;

	/** The node of the AST related to this method */
	private MethodDeclaration node;

	/** The identifier of the method*/
	private int id;

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

	/** A method is considered a main method if it's static,
	 *  called "main", (iii) it has one parameter of type
	 *  String[] (array of String)
	 * */
	private boolean isMain;

	/**
	 * Create an instance of the class Method
	 * 
	 * @param ast the MethodDeclaration, i.e. the AST node related to this method
	 * @throws Exception 
	 */
	public JavaMethod(MethodDeclaration astNode, JavaClass parents) throws Exception {
		this.node 		= astNode;
		this.javaClass 	= parents;
		this.name 		= node.getName().getIdentifier();
		this.parameters = extractParameter();
		this.body 		= (node.getBody() != null) ? node.getBody().toString() : "";
		this.comment 	= (node.getJavadoc() != null) ? node.getJavadoc().toString().replaceAll("\n", "\t") : "" ;
		/** node.isConstructor() generate problems when it load constructor methods of nested class */
		this.returnType = (node.getReturnType2() == null) ? "contructor" : node.getReturnType2().toString() ;
		this.children 	= this.extractChildren();
		
		this.isMain = checkIsMain();

		// Register the method as an instance
		new Method(this);
		//this.id 		= InstanceHandbook.getInstance().registerInstance(new Method(this));
	}

	private boolean checkIsMain() {
		boolean isStatic = false;
		for(int i=0; i < node.modifiers().size(); i++) {
			if(node.modifiers().get(i).toString().equals("static")) {
				isStatic = true;
				break;
			}
		}
		
		
		boolean containStringArrayParam = false;
		if(!node.parameters().isEmpty()) {
			containStringArrayParam = node.parameters().get(0).toString().replace(" ", "").contains("String[]");
		}
				
		if("main".equals(name) &&
			isStatic &&
			parameters.size() == 1 &&
			containStringArrayParam
			) {
			return true;
		}
		
		return false;
	}

	private List<String> extractParameter() {
		List<String> params = new ArrayList<String>();
		for(Object par: node.parameters())
			params.add(par.toString());
		return params;
	}

	@Override
	public JavaClass getParent() {
		return javaClass; 
	}

	@Override
	public ASTNode getNode() {
		return node;
	}

	@Override
	public String getName() {
		return name;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public String getReturnType() {
		return returnType;
	}

	public String getBody() {
		return body;
	}
	
	public boolean isMain() {
		return this.isMain;
	}

	/**
	 * Return the body without:
	 * <ul>
	 * 		<li> comments: if "toRemove" contain  					comm </li>
	 * 		<li> constant values: if "toRemove" contain  			const </li>
	 * 		<li> variable names: if "toRemove" contain 				var </li>
	 * 		<li> newline, spaces and tabs: if "toRemove" contain 	nl </li> 
	 * 		<li> curly braces: if "toRemove" contain 				curly </li>
	 * </ul>
	 * */
	//	public String getParsedBody(String toRemove) {
	//		String parsedBody = body;
	//		if(toRemove.contains("var"))
	//			parsedBody = removeValues(new VariableNamesVisitor(name), parsedBody, "var");
	//		if(toRemove.contains("const"))
	//			parsedBody = removeValues(new ConstantValuesVisitor(name), parsedBody, "const");
	//		if(toRemove.contains("comm"))
	//			parsedBody = removeComments(parsedBody);
	//		if(toRemove.contains("curly"))
	//			parsedBody = parsedBody.replace("{", "").replace("}", "");
	//		if(toRemove.contains("nl"))
	//			parsedBody = parsedBody.replaceAll("[ \\n\\t;]", "");
	//		return parsedBody;
	//	}

	public List<String> getAllVariableNames(){
		if(variableNames == null) 
			visitVariable();
		return variableNames;
	}

	private void visitVariable() {
		VariableNamesVisitor visitor = new VariableNamesVisitor(name);
		this.javaClass.getNode().accept(visitor);
		this.variableNames = visitor.getValues();
		this.variableTypes = visitor.getTypes();
		this.variableNames.sort(
			new Comparator<String>()
			{
				public int compare(String s1,String s2)
				{
					return s2.length() - s1.length();
				}
			}
		);
	}

	public Set<String> getAllVariableTypes(){
		if(variableTypes == null)
			this.visitVariable();
		return variableTypes;
	}

	public List<String> getAllConstantValue(JavaClass javaClass){
		if(constantValues == null) {
			visitConstants(javaClass);
		}
		return constantValues;
	}

	private void visitConstants(JavaClass javaClass) {
		
		ConstantValuesVisitor visitor = new ConstantValuesVisitor(name);
		javaClass.getNode().accept(visitor);
		this.constantValues = visitor.getValues();
		this.constantValues.sort(
				new Comparator<String>()
				{
					public int compare(String s1,String s2)
					{
						return s2.length() - s1.length();
					}
				}
			);
	}

	public String removeValues(CustomAstVisitor visitor, String parsedBody, String newValue) {
		String newParsedBody = parsedBody;
		this.javaClass.getNode().accept(visitor);
		for(String s: visitor.getValues()) {
			newParsedBody = newParsedBody.replace(s, newValue);
		}
		return newParsedBody;
	}

	public String getComment() {
		return comment;
	}

	public int getID() {
		return id;
	}

	public long getCharFreqInBody(char c) {
		return body.chars().filter(num -> num == c).count();
	}

	@Override
	public String toString() {
		return "Method [Class = " + javaClass.getName() + ",\n Name = " + name + ",\n Parameters = " + parameters
				+ ",\n ReturnType = " + returnType + ",\n Comment = " + comment + ", \nFullText:\n"
				+ node + "]";
	}

	public String removeComments(String result) {
		return result.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
	}

	@Override
	protected List<JavaComponent> extractChildren() throws Exception {
		ArrayList<JavaComponent> stmts = new ArrayList<JavaComponent>();
		//		if(node.getBody() == null) {
		//			JavaStatement jstmt = new JavaStatement("empty", this);
		//			stmts.add(jstmt);
		//		}
		if(node.getBody() != null) {
			for(Object obj: node.getBody().statements()) {
				Statement stmt = ((Statement) obj);
				JavaStatement jstmt = new JavaStatement(stmt, this);
				stmts.add(jstmt);
			}
		}
		return stmts;
	}

}

package it.unimib.disco.essere.deduplicator.preprocessing.modelling;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import it.unimib.disco.essere.deduplicator.preprocessing.modelling.JavaMethod;
import it.unimib.disco.essere.deduplicator.preprocessing.visitors.ConstantValuesVisitor;
import it.unimib.disco.essere.deduplicator.preprocessing.visitors.VariableNamesVisitor;

public class JavaStatement implements JavaComponent {
	
	private String originalStatement;
	
	private String parsedStatement;
	
	private int numOfCopies;
	
	private int length;
	
	private JavaMethod parent;
	
	private Statement node;
	
	private List<JavaStatement> methodsWithCopiedStatement;
	
	public JavaStatement(Statement statement, JavaMethod method) {
		this.originalStatement = statement.toString();
		this.parent = method;
		this.node = statement;
		this.parsedStatement = this.getParsedStatement("all");
		this.length = parsedStatement.length();
		this.methodsWithCopiedStatement = new ArrayList<JavaStatement>();
		
		float f = 1.2f;
	}
	
	/**
	 * Return the statement without:
	 * <ul>
	 * 		<li> comments: if "toRemove" contain  					comm </li>
	 * 		<li> constant values: if "toRemove" contain  			const </li>
	 * 		<li> variable names: if "toRemove" contain 				var </li>
	 * 		<li> newline, spaces and tabs: if "toRemove" contain 	nl </li>
	 * 		<li> curly braces: if "toRemove" contain 				curly </li>
	 * 		<li> all the previous mentioned strings: if "toRemove" contain 	all </li>
	 * </ul>
	 * */
	private String getParsedStatement(String toRemove) {
		String parsedStmt = originalStatement;
		if(toRemove.contains("const") || toRemove.contains("all"))
			parsedStmt = removeConst(parent.getAllConstantValue(this.parent.getParent()), parsedStmt);
		if(toRemove.contains("var") || toRemove.contains("all"))
			parsedStmt = removeVarNames(parent.getAllVariableNames(), parsedStmt);
		if(toRemove.contains("comm") || toRemove.contains("all"))
			parsedStmt = parent.removeComments(parsedStmt);
		if(toRemove.contains("curly") || toRemove.contains("all"))
			parsedStmt = parsedStmt.replace("{", "").replace("}", "");
		if(toRemove.contains("nl") || toRemove.contains("all"))
			parsedStmt = parsedStmt.replaceAll("[ \\n\\t;]", "");		
		return parsedStmt;
	}
	
	public String getParsedStatement() {
		return this.parsedStatement;
	}
	
	public void addClonedStatement(JavaStatement method) {
		methodsWithCopiedStatement.add(method);
	}
	

	private String removeVarNames(List<String> allVariableNames, String stmt) {
		for(String var: allVariableNames) {
			String inizialization1 = var + " ";
			stmt = stmt.replace(inizialization1, " var ");
			String inizialization2 = " " + var;
			stmt = stmt.replace(inizialization2, " var ");
			String inizialization3 = var + "=";
			stmt = stmt.replace(inizialization3, " var =");
			String functionCall = var + ".";
			stmt = stmt.replace(functionCall, "var.");
			String accessThroughClass = "." + var; // es: this.var
			stmt = stmt.replace(accessThroughClass, ".var");
			stmt = stmt.replace(" ", "");
			String parameter = "(" + var + ")"; 
			stmt = stmt.replace(parameter, "(var)");
			String parameter1 = "(" + var + ","; 
			stmt = stmt.replace(parameter1, "(var, ");
			String parameter2 = "," + var + ")"; 
			stmt = stmt.replace(parameter2, ", var)");
			String parameter3 = "," + var + ","; 
			stmt = stmt.replace(parameter3, ",var)");
			String array = "[\\)|\\+|,]*(" + var + "){1}\\[";
			stmt = stmt.replaceAll(array, " var[");
		}
		return stmt;
	}

	private String removeConst(List<String> constantValues, String stmt) {
		for(String cons: constantValues) {
			if(cons.matches("(true|false)"))
				stmt = stmt.replace(cons, "false");
			if(cons.matches("[0-9]+.[0-9]+f"))
				stmt = stmt.replace(cons, "0.0f");
			if(cons.matches("[0-9]+.[0-9]+"))
				stmt = stmt.replace(cons, "0.0");
			if(cons.matches("[0-9]+"))
				stmt = stmt.replace(cons, "0");
			if(cons.matches("'.'"))
				stmt = stmt.replace(cons, "'x'");
			if(cons.matches("\".*\"")) 
				stmt = stmt.replace(cons, "const");
		}
		return stmt;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof JavaStatement) {
			JavaStatement stmt = (JavaStatement) o;
			return this.parsedStatement.equals(stmt.parsedStatement);
		}			
		return false;	
	}

	public String getOriginalStatement() {
		return originalStatement;
	}

	public int getNumOfCopies() {
		return numOfCopies;
	}

	public int getLength() {
		return length;
	}

	public List<JavaStatement> getMethodsWithCopiedStatement() {
		return methodsWithCopiedStatement;
	}

	public void setMethodsWithCopiedStatement(List<JavaStatement> methodsWithCopiedStatement) {
		this.methodsWithCopiedStatement = methodsWithCopiedStatement;
	}

	@Override
	public JavaMethod getParent() {
		return parent;
	}

	/**
	 * @return null (this is a leaf node)
	 * */
	@Override
	public List<JavaComponent> getChildren() {
		return null;
	}
	
	/**
	 * @return the parsed statement
	 * */
	@Override
	public String getName() {
		return this.parsedStatement;
	}

	@Override
	public ASTNode getNode() {
		return node;
	}

	
}

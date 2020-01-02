package it.unimib.disco.essere.deduplicator.preprocessing;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

public interface Instance {
	
	public ASTNode getAstNode();

	public String getJavaFilePath();


	public String getClassName();


	public String getSuperClassName();


	public String getMethodName();


	public List<String> getMethodParameters();


	public String getMethodReturnType();


	public String getMethodBody();


	public String getMethodComment();


	public int getMethodID();


	public List<String> getVariableNames();


	public Set<String> getVariableTypes();


	public List<String> getConstantValues();


	public List<String> getParsedStatement();
	
	public String getFullSuperClassName();
	
	public List<ASTNode> getStatement(String parsedStatement);
	
	public boolean isMain();
	
}

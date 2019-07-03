package it.unimib.disco.essere.deduplicator.preprocessing.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;

public abstract class CustomAstVisitor extends ASTVisitor {
	
	protected List<String> values;

	protected String nameMethod;

	public CustomAstVisitor(String nameMethod) {
		super();
		this.values = new ArrayList<String>();
		this.nameMethod = nameMethod;
	}
	
	public List<String> getValues() {
		return values;
	}

}

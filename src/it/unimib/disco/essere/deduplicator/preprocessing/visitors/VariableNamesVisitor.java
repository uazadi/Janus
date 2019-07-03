package it.unimib.disco.essere.deduplicator.preprocessing.visitors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class VariableNamesVisitor extends CustomAstVisitor {
	
	private Set<String> types;

	public VariableNamesVisitor(String nameMethod) {
		super(nameMethod);
		types = new HashSet<String>();
	}
	
	public Set<String> getTypes(){
		return types;
	}
	
	public boolean visit(VariableDeclarationFragment var) {
		values.add(var.getName().toString());
		return false;
	}


	public boolean visit(MethodDeclaration md) {
		if(md.getName().toString().equals(nameMethod))
			md.accept(new ASTVisitor() {
				public boolean visit(SingleVariableDeclaration var) {
					values.add(var.getName().toString());
					types.add(var.getType().toString());
					return false;
				}	
				public boolean visit(VariableDeclarationFragment var) {
					values.add(var.getName().toString());
					return false;
				}
			});
		return false;
	}
	
	
	
	public boolean visit(FieldDeclaration field) {
		types.add(field.getType().toString());
		return false;
	}

}

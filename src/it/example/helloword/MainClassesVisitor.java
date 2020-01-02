package it.example.helloword;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MainClassesVisitor extends ASTVisitor {
	
	protected List<String> classes;

	public MainClassesVisitor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		
		String name = node.getName().getIdentifier();
		List<?> modifier = node.modifiers();
		
		for(int i=0; i < modifier.size(); i++)
			System.out.println(modifier.get(i));
		
		List<?> param = node.typeParameters();
		
		for(int i=0; i < param.size(); i++)
			System.out.println(param.get(i));
		
		return super.visit(node);
	}

	
	
}

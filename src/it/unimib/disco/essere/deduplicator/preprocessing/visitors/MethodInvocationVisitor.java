package it.unimib.disco.essere.deduplicator.preprocessing.visitors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class MethodInvocationVisitor extends CustomAstVisitor {

	//private JavaMethod method;
	private String className;

	public MethodInvocationVisitor(String methodName, String className) {
		super(methodName);
		this.className = this.className.substring(className.lastIndexOf('.') + 1);
	}

	@Override
	public boolean visit(CompilationUnit cu) {

		boolean visit = checkNotSameClass(cu);
				//&& checkTypesUsedByMethod(cu);

		if(visit) {
			cu.accept(new ASTVisitor() {

				public boolean visit(MethodDeclaration md) {

					md.accept(new ASTVisitor() {

						public boolean visit(MethodInvocation mi) {
							if(mi.getName().toString().equals(nameMethod) &&
									mi.toString().split("\\(")[0].contains("."))
								values.add(((TypeDeclaration) cu.types().get(0)).getName().toString() + "#" + mi.toString());
							return false;
						}	

					});

					return false;
				}
			});
		}

		return false;
	}

//	private boolean checkTypesUsedByMethod(CompilationUnit cu) {
//		Set<String> setTypes = new HashSet<String>();
//		for(Object obj: cu.types()) {
//			TypeDeclaration td = (TypeDeclaration) obj; 
//			setTypes.add(td.getName().toString());
//		}
//		
//		//System.out.println(">>>>>>>>>>>>>>" + setTypes);
//		//System.out.println("<<<<<<<<<<<<<<" + method.getAllVariableTypes() + "       { " + method.getName() + " " + this.className);
//		
//		setTypes.retainAll(method.getAllVariableTypes());
//		return !setTypes.isEmpty();
//	}

	private boolean checkNotSameClass(CompilationUnit cu) {
		boolean visit = true; 

		for(Object obj: cu.types()) {
			TypeDeclaration td = (TypeDeclaration) obj; 
			if(td.getName().toString().equals(className))
				visit = false;
		}
		return visit;
	}
}

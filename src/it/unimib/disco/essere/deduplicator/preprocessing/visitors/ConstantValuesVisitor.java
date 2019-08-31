package it.unimib.disco.essere.deduplicator.preprocessing.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeLiteral;

public class ConstantValuesVisitor extends CustomAstVisitor {
	
	String newValue = "const";

	public ConstantValuesVisitor(String nameMethod) {
		super(nameMethod);
	}
	
	public ConstantValuesVisitor(String nameMethod, String newValue) {
		super(nameMethod);
		this.newValue = newValue;
	}

	@Override
	public boolean visit(MethodDeclaration md) {
		if(md.getName().toString().equals(nameMethod))
			md.accept(new ASTVisitor() {
				public boolean visit(BooleanLiteral cons) {
					values.add(cons.toString());
					
					//System.out.println("bool: "  +  cons);
					//cons.setBooleanValue(false);
					return false;
				}	

				public boolean visit(CharacterLiteral cons) {
					values.add(cons.toString());
					
					//System.out.println("char: "  +  cons);
					//cons.setCharValue('x');
					return false;
				}

				public boolean visit(NullLiteral cons) {
					//values.add(cons.toString());
					
					//System.out.println("null: "  +  cons);
					
					return false;
				}

				public boolean visit(NumberLiteral cons) {
					values.add(cons.toString());
				
					System.out.println("number: "  +  cons);	
					//cons.setToken("0");
					return false;
				}

				public boolean visit(StringLiteral cons) {
					values.add(cons.toString());
					
					System.out.println("String: "  +  cons);
					//cons.setLiteralValue(newValue);
					return false;
				}

				public boolean visit(TypeLiteral cons) {
					
					//System.out.println("Type: "  +  cons);
					values.add(cons.toString());
					return false;
				}
			});
		return false;
	}
}

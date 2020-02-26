
package it.unimib.disco.essere.janus.refactoring;

import java.util.List;
import java.util.Random;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.structure.ExtractSupertypeProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import it.unimib.disco.essere.janus.refactoring.exception.UnsuccessfulRefactoringException;

public class CCExtractSuperclass extends CCRefactoring {

	public CCExtractSuperclass(
			List<ASTNode> cloneSet, 
			List<ICompilationUnit> icus_involved, 
			IJavaProject project) {
		super(cloneSet, icus_involved, project);
	}

	@Override
	public void apply() throws UnsuccessfulRefactoringException {
		checkCloneType();
		sortNodes(cloneSet);
		try {
			extractMethods();
			IMethod kept = selectMethodToKeep(extractedMethods);
			applyExtractSuperclass(kept);
			kept.delete(true, null);
		} catch (CoreException | 
				OperationCanceledException |
				MalformedTreeException | 
				BadLocationException e) {
			throw new UnsuccessfulRefactoringException(e.getMessage());
		}
	}

	private void applyExtractSuperclass(IMethod kept) throws OperationCanceledException, CoreException, MalformedTreeException, BadLocationException {
		try {
			CodeGenerationSettings settings =
					JavaPreferencesSettings.getCodeGenerationSettings(kept.getCompilationUnit().getJavaProject());


			IMember[] ims = {kept};

			if(this.extractedClassName == null || this.extractedClassName.equals("")) {
				Random rand = new Random();
				extractedClassName = "ExtractedSuperClass" + rand.nextInt(1000);
			}


			ExtractSupertypeProcessor refactoring = new ExtractSupertypeProcessor(ims, settings);

			refactoring.checkInitialConditions(new NullProgressMonitor());
			refactoring.setTypeName(extractedClassName);
			CheckConditionsContext ccc = new CheckConditionsContext();
			ccc.add(new ResourceChangeChecker());
			refactoring.checkFinalConditions(new NullProgressMonitor(), ccc);
			Change change = refactoring.createChange(new NullProgressMonitor());
			change.perform(new NullProgressMonitor());

			ICompilationUnit selectedICU = kept.getCompilationUnit();
			CompilationUnit selectedCU =  fromICUtoCU(selectedICU);

			ASTParser parser = ASTParser.newParser(AST.JLS11); 
			parser.setSource(selectedICU);

			Type superClassType = ((TypeDeclaration)selectedCU.types().get(0)).getSuperclassType();

			for(ICompilationUnit icu: icus_involved) {
				if(icu != selectedICU) {
					CompilationUnit cu = fromICUtoCU(icu);

					// new code to test
					ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
					ICompilationUnit newIcu = (ICompilationUnit) cu.getJavaElement();
					Document document = new Document(newIcu.getBuffer().getContents());

					// Select only the first type in the CompilationUnit because usually
					// there only one main class in each java file and that is the one
					// that should be modified
					//				((TypeDeclaration) cu.types().get(0))
					//					.setSuperclassType(
					//							cu.getAST().newSimpleType(
					//									cu.getAST().newSimpleName(superClassName)));

					rewriter.replace(((TypeDeclaration) cu.types().get(0)).getSuperclassType(), cu.getAST().newSimpleName(extractedClassName), null);

					//Document document = new Document(icu.getSource());

					String s = ((TypeDeclaration) cu.types().get(0)).toString();


					//				Old version
					//Document document = new Document(cu.toString());

					TextEdit edits = rewriter.rewriteAST(document, null);
					edits.apply(document);

					icu.getBuffer().setContents(document.get());

				}
			}

			// Add the new Superclass in the list of compilation Unit involved in the refactoring
			String fullyQualifiedNameSuperClass = selectedICU.getTypes()[0].getPackageFragment().getElementName() + "." + extractedClassName;
			IType superClassIType = project.findType(fullyQualifiedNameSuperClass);
			icus_involved.add(superClassIType.getCompilationUnit());

		}catch(NullPointerException e) {
			System.out.println("[CCExtractSuperclass] ERROR NullPointerException during superclass extraction");
		}
	}
}
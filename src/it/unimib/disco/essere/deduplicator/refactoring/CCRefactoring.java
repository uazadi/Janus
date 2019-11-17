package it.unimib.disco.essere.deduplicator.refactoring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;

public abstract class CCRefactoring {

	protected List<ASTNode> cloneSet;
	protected List<ICompilationUnit> icus_involved;
	protected String extractedMethodName;
	protected Set<IMethod> extractedMethods;
	protected IJavaProject project;

	public static List<CCRefactoring> selectRefactoringTechniques(
			InstancesHandler ih, 
			List<List<ASTNode>> cloneSets,
			IJavaProject selectedProject) 
					throws JavaModelException, NotRefactorableCodeClones {

		List<CCRefactoring> refactorings = new ArrayList<>();

		for(List<ASTNode> cloneSet: cloneSets) {

			CCRefactoring refactoring = null;

			for(int i=0; i < cloneSet.size(); i++) {
				Statement stmt = (Statement) cloneSet.get(i);
				System.out.println("[CCRefactoring - selectRefactoringTechniques] Method" + ((MethodDeclaration) stmt.getParent().getParent()).getName());
				System.out.println("[CCRefactoring - selectRefactoringTechniques]\n" + stmt.toString());
			}

			List<ICompilationUnit> icus_involved = new LinkedList<>();

			// For each clone within the group selected 
			for(int i=0; i < cloneSet.size(); i++) {
				Statement stmt = (Statement) cloneSet.get(i);
				ICompilationUnit workingCopy = getICompilationUnit(stmt);
				workingCopy.becomeWorkingCopy(new NullProgressMonitor());
				icus_involved.add(workingCopy);
			}

			boolean sameClass = true;
			for(int i=0; i < icus_involved.size(); i++) {
				for(int j=i+1; j < icus_involved.size(); j++) {

					String name1 = buildFullName(icus_involved.get(i));
					String name2 = buildFullName(icus_involved.get(j));

					if(!name1.equals(name2)) {
						sameClass = false;
						break;
					}
				}
			}



			if(sameClass) {
				refactoring = new CCExtractMethod(cloneSet, icus_involved, selectedProject);
			}
			else { // try same hierarchy
				refactoring = attemptSameHierarchyRefactoring(ih, 
						selectedProject, 
						cloneSet, 
						refactoring,
						icus_involved);
			}

			if(refactoring != null)
				refactorings.add(refactoring);
			else
				throw new NotRefactorableCodeClones();
		}
		return refactorings;
	}

	private static CCRefactoring attemptSameHierarchyRefactoring(InstancesHandler ih, IJavaProject selectedProject,
			List<ASTNode> cloneSet, CCRefactoring refactoring, List<ICompilationUnit> icus_involved)
					throws JavaModelException {
		String lcsSoFar = 
				buildFullName(icus_involved.get(0)).replace(".java", "");
		for(int i=1; i < icus_involved.size(); i++) {
			String className =  
					buildFullName(icus_involved.get(i)).replace(".java", "");
			lcsSoFar = ih.findNearestCommonSuperclass(lcsSoFar, className);		

			System.out.println("[CCRefactoring - selectRefactoringTechniques]  LCS SO FAR: " + lcsSoFar);
		}

		System.out.println("[CCRefactoring - selectRefactoringTechniques]  LCS SO FAR: " + lcsSoFar);

		if(lcsSoFar != null) { 

			ICompilationUnit superClass = 
					getSuperClassICompilationUnit(lcsSoFar, selectedProject);

			if(icus_involved.size() >= 2) {
				if(superClass == null) {
					refactoring = new CCExtractSuperclass(
							cloneSet, 
							icus_involved,
							selectedProject);
				}
				else {
					refactoring = new CCExtractPullUpMethods(
							cloneSet, 
							icus_involved,
							selectedProject,
							lcsSoFar,
							superClass);
				}
			}
		}
		return refactoring;
	}

	public CCRefactoring(
			List<ASTNode> cloneSet,
			List<ICompilationUnit> icus_involved,
			IJavaProject project) {
		this.cloneSet = cloneSet;
		this.icus_involved = icus_involved;
		this.project = project;
	}

	public List<ICompilationUnit> getCompilationUnitInvolved(){
		return this.icus_involved;
	}

	private static ICompilationUnit getSuperClassICompilationUnit(
			String lcsSoFar, 
			IJavaProject selectedProject) 
					throws JavaModelException {
		ICompilationUnit superClass = null;

		try {
			for(IPackageFragmentRoot ipdr: selectedProject.getPackageFragmentRoots()) {
				String[] path = lcsSoFar.split("\\.");
				String className = path[path.length - 1];
				String packageName = lcsSoFar.replace("." + className, "");
				IPackageFragment ipf = ipdr.getPackageFragment(packageName);
				superClass = ipf.getCompilationUnit(className + ".java");
				if(superClass != null) {
					superClass.open(new NullProgressMonitor());
					break;
				}
			}
		}catch(JavaModelException e) {
			// The superclass is not a class defined in the system 
			// but is imported from a library
			return null;
		}
		return superClass;
	}


	private static String buildFullName(ICompilationUnit icu) 
			throws JavaModelException {
		String packageDirty = icu.getPackageDeclarations()[0].toString();
		String className = icu.getElementName();
		return packageDirty.split(" ")[1] + "." + className;
	}


	private static ICompilationUnit getICompilationUnit(Statement stmt) {
		ASTNode parent = stmt.getParent();
		while(!(parent instanceof CompilationUnit))
			parent = parent.getParent();
		CompilationUnit cu = (CompilationUnit) parent;
		ICompilationUnit icu = (ICompilationUnit) cu.getJavaElement();
		return icu;
	}

	protected void extractMethods() throws CoreException {
		icus_involved = new LinkedList<>();
		for(int i=0; i < cloneSet.size(); i++) {
			Statement stmt = (Statement) cloneSet.get(i);
			ICompilationUnit workingCopy = extractMethod(stmt);
			icus_involved.add(workingCopy);
		}
		this.extractedMethods = getExtractedMethods();
	}

	private ICompilationUnit extractMethod(Statement stmt) 
			throws JavaModelException, CoreException {
		ICompilationUnit workingCopy = getICompilationUnit(stmt);
		workingCopy.becomeWorkingCopy(new NullProgressMonitor());

		handleConstantConflict(workingCopy);

		ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(
				workingCopy, stmt.getStartPosition(), stmt.getLength());

		this.extractedMethodName = refactoring.getMethodName();
		refactoring.checkAllConditions(new NullProgressMonitor());
		Change change = refactoring.createChange(new NullProgressMonitor());
		change.perform(new NullProgressMonitor());

		return workingCopy;
	}

	private Set<IMethod> getExtractedMethods() throws JavaModelException {
		Set<IMethod> extractedMethods = new HashSet<>();

		for(ICompilationUnit workingCopy: this.icus_involved) {

			workingCopy.commitWorkingCopy(true, new NullProgressMonitor());
			workingCopy.reconcile(AST.JLS11, true, null, new NullProgressMonitor());

			IMethod[] methods = workingCopy.getTypes()[0].getMethods();

			// Find the extracted methods
			for(int j=0; j < methods.length; j++) {
				IMethod im = methods[j];

				if(im.getElementName().equals(this.extractedMethodName)){
					extractedMethods.add(im);
				}
			}
		}
		return extractedMethods;
	}

	private void handleConstantConflict(ICompilationUnit workingCopy) {
		CompilationUnit cu = this.fromICUtoCU(workingCopy);


	}



	protected void sortNodes(List<ASTNode> nodes) {
		/** Sort the statements in descending order, in this way
		    if two statement are in the same class, the one one in 
		    the lower position is extracted before then the one 
		    in the higher position. 
		    The other way around will generate a conflict concerning 
		    the position of the code!
		 */ 
		nodes.sort(new Comparator<ASTNode>() {
			@Override
			public int compare(ASTNode node1, ASTNode node2) {
				if(node1.getStartPosition() > node2.getStartPosition())
					return -1;
				else 
					return 1;
			}
		});
	}

	protected IMethod selectMethodToKeep(Set<IMethod> extractedMethods) {
		IMethod keep = null;
		if(extractedMethods.size() >= 2) {
			boolean toKeepChosen = false;
			Iterator<IMethod> extractedMethodsIterator  = extractedMethods.iterator();
			while(extractedMethodsIterator.hasNext()) {
				try {
					IMethod extr = extractedMethodsIterator.next();

					// A method has to be kept if:
					// 1) No other method has already been chosen
					// AND
					// 2) either this is the last of the methods extracted 
					//    OR this is the first "static" method extracted
					if(!toKeepChosen &&
							(extr.getSignature().contains(" static ") ||
									!extractedMethodsIterator.hasNext())) {
						toKeepChosen = true;	
						keep = extr;
					}
					else {
						extr.delete(true, null);
					}
				}catch (JavaModelException e) {
					// TODO Investigate methods inserted more then one time
					// The method that should be handle has already be deleted. 
					// This error can be ignored but is worth investigate why
					// this happen.
				}
			}
		}
		return keep;
	}

	protected void checkCloneType() {
		String type = "";
		for(int i=0; i < this.cloneSet.size(); i++) {
			for(int j=i+1; j < this.cloneSet.size(); j++) {
				Statement stmt1 = (Statement) this.cloneSet.get(i);
				Statement stmt2 = (Statement) this.cloneSet.get(j);

				ASTMatcher matcher = new ASTMatcher();

				if(stmt1.subtreeMatch(matcher, stmt2)) {					
					System.out.println("[[[[[[[[[[[[[[[[EXACT MATCH]]]]]]]]]]]]]]]]");
				}else {
					System.out.println("[[[[[[[[[[[[[[[[NOT EXACT MATCH]]]]]]]]]]]]]]]]");					
					break;
				}
			}
		}

		if(cloneSet.size() > 1) {

			Set<ASTNode> diffExprs = 
					new HashSet<ASTNode>();

			getDiffs(cloneSet, diffExprs);

			Set<CompilationUnit> CompUnitActMod = new HashSet<>();

			List<VariableDeclarationStatement> ssss = new ArrayList<VariableDeclarationStatement>();

			for(ASTNode stmt: cloneSet) {

				ASTNode tmp = stmt.getParent();
				while(!(tmp instanceof CompilationUnit)) {
					tmp = tmp.getParent();
				}
				CompilationUnit cu = ((CompilationUnit) tmp);
				if(!(CompUnitActMod.contains((CompilationUnit) cu))) {
					cu.recordModifications();
					CompUnitActMod.add((CompilationUnit) cu);
				}

				ICompilationUnit icu = (ICompilationUnit)cu.getJavaElement();

				Block block = ((Block) stmt.getParent());
				AST ast = cu.getAST();

				for(MethodDeclaration md: ((TypeDeclaration) cu.types().get(0)).getMethods()) 
					for(Object o: md.getBody().statements())
						if(stmt.equals(o)) {
							System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
							block = (Block) stmt.getParent();
						}




				ASTRewrite rewriter = ASTRewrite.create(ast);

				String source = "";
				try {
					source = icu.getBuffer().getContents();
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
				Document document = new Document(source);




				for(ASTNode diff: diffExprs) {



					if(diff instanceof StringLiteral && stmt.toString().contains(diff.toString())) {
						System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% BEFORE");
						System.out.println(stmt.getParent());




						Random rand = new Random();
						String varName = "const" + rand.nextInt(1000);

						VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
						vdf.setName(ast.newSimpleName(varName));

						System.out.println("diff.toString()  ->  " + diff.toString());

						// (StringLiteral) ASTNode.copySubtree(ast.newStringLiteral().getAST()

						StringLiteral nn = ast.newStringLiteral();
						nn.setLiteralValue(diff.toString().replace("\"", ""));
						vdf.setInitializer(nn);

						VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);

						vds.setType(ast.newSimpleType(ast.newSimpleName("String")));

						System.out.println("Starting position: " + stmt.getStartPosition());


						VariableDeclarationFragment vdf_varname = ast.newVariableDeclarationFragment();
						vdf_varname.setName(ast.newSimpleName(varName));


						rewriter.replace(diff, vdf_varname, null);


						int index = block.statements().indexOf(stmt);

						block.statements().add(index - 1, vds);



						System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% AFTER");
						System.out.println(stmt.getParent());


						int newStartingPosition = stmt.getStartPosition() + vds.getLength();

						stmt.setSourceRange(newStartingPosition, stmt.getLength());

					}
				}




//				for(VariableDeclarationStatement vds: ssss) {
//					int index = block.statements().indexOf(stmt);	
//					block.statements().add(index - 1, vds);
//				}

				TextEdit edits = rewriter.rewriteAST(document, null);

				try {
					edits.apply(document);
					String newSource = document.get();
					icu.getBuffer().setContents(newSource);
				} catch (MalformedTreeException | BadLocationException e) {
					e.printStackTrace();
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}





			//			for(CompilationUnit cu: CompUnitActMod) {
			//				try {
			//					ICompilationUnit icu = (ICompilationUnit)cu.getJavaElement();
			//					String source = icu.getBuffer().getContents();
			//					Document document = new Document(source);
			//					commitChanges(icu, cu, document);
			//				} catch (MalformedTreeException | JavaModelException | BadLocationException e) {
			//					e.printStackTrace();
			//				}
			//			}
		}
	}

	protected void commitChanges(ICompilationUnit iUnit, CompilationUnit unit, Document document) throws MalformedTreeException, BadLocationException, JavaModelException {
		TextEdit edits = unit.rewrite(document, iUnit.getJavaProject().getOptions(true));
		edits.apply(document);

		String newSource = document.get();
		iUnit.getBuffer().setContents(newSource);

		//		iUnit.reconcile(ICompilationUnit.NO_AST, false, null, null);
		//		iUnit.commitWorkingCopy(true, null);
		//iUnit.discardWorkingCopy();
	}


	@SuppressWarnings("unchecked")
	private void  getDiffs(List<ASTNode> nodes, Set<ASTNode> diff) {

		List<StructuralPropertyDescriptor> props = nodes.get(0)
				.structuralPropertiesForType();


		for (StructuralPropertyDescriptor property : props) {

			List<Object> strucProps = new ArrayList<Object>();

			for(ASTNode node: nodes) {
				strucProps.add(node.getStructuralProperty(property));
			}

			if (property.isSimpleProperty()) {
				// check for simple properties (primitive types, Strings, ...)
				// with normal equality

				if(nodes.get(0) instanceof BooleanLiteral        ||
						nodes.get(0) instanceof CharacterLiteral ||
						nodes.get(0) instanceof NumberLiteral    ||
						nodes.get(0) instanceof StringLiteral    ||
						nodes.get(0) instanceof TypeLiteral) {




					if(!strucProps.stream().allMatch(strucProps.get(0)::equals)){
						System.out.println("_______________________________SIMPLE PROPERTY_______________________________");
						for(ASTNode node: nodes) {
							System.out.println("[CCRefactoring      ---      checkCloneType]   " + node);
							System.out.println("[CCRefactoring      ---      checkCloneType]   Class  ->  " + node.getClass());
						}
						System.out.println("______________________________________________________________________________");

						for(ASTNode node: nodes) {
							diff.add(node);
						}
					}

				}


			} else if (property.isChildProperty()) {
				// recursively call this function on child nodes

				List<ASTNode> newNodes = strucProps.stream()
						.map(element->(ASTNode) element)
						.collect(Collectors.toList());


				if(!newNodes.contains(null)) {

					getDiffs(newNodes, diff);
				}

			} else if (property.isChildListProperty()) {


				List<Iterator<ASTNode>> iterators  = strucProps.stream()
						.map(element-> ((Iterable<ASTNode>) element).iterator())
						.collect(Collectors.toList());

				while (iterators.stream().allMatch(Iterator::hasNext)) {
					// recursively call this function on child nodes

					List<ASTNode> newNodes  = iterators.stream()
							.map(element->element.next())
							.collect(Collectors.toList());



					getDiffs(newNodes, diff);

				}

			}
		}
	}


	public abstract void apply()  throws UnsuccessfulRefactoringException;

	protected CompilationUnit fromICUtoCU(ICompilationUnit icu) {
		ASTParser parser = ASTParser.newParser(AST.JLS11); 
		parser.setSource(icu);
		parser.setResolveBindings(true); // we need bindings later on
		parser.setProject(project);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
		return cu;
	}

}


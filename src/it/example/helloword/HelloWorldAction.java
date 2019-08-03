package it.example.helloword;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.structure.ExtractSupertypeProcessor;
import org.eclipse.jdt.internal.corext.refactoring.structure.PullUpRefactoringProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;
import it.unimib.disco.essere.deduplicator.preprocessing.PreprocessingFacade;
import it.unimib.disco.essere.deduplicator.rad.moea.MethodSelector;
import it.unimib.disco.essere.deduplicator.rad.moea.MultiObjective;
import it.unimib.disco.essere.deduplicator.rad.moea.SingleObjective;

@SuppressWarnings("restriction")
public class HelloWorldAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	private Action action = this;
	private IJavaProject selectedProject;

	private String extMethodName;


	@Override
	public void run(IAction arg0) {
		// MessageDialog.openInformation(window.getShell(), "FirstPlugin", "Hello,
		// World!");

		IStructuredSelection selection = 
				(IStructuredSelection) window.getSelectionService().getSelection();
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof IAdaptable) {
			if(firstElement instanceof Project) {
				Project projectTmp = (Project) firstElement;
				selectedProject = JavaCore.create(projectTmp);
			}else if (firstElement instanceof JavaProject) {
				JavaProject projectTmp = (JavaProject) firstElement;
				selectedProject = projectTmp.getJavaProject();
			}
			accomplishRefactoring(selectedProject);
		}
	}

	private void accomplishRefactoring(IJavaProject project) {
		try {
			PreprocessingFacade pf = new PreprocessingFacade();
			InstancesHandler ih = pf.parseSourceCode(project);

			//MultiObjective mo = new MultiObjective(ih, 30);
			SingleObjective so = new SingleObjective(ih);
			int numberOfIteration = 30;
			String resolutionMethod = "NSGA-II";

			MethodSelector ms = new MethodSelector(so, resolutionMethod, numberOfIteration, ih);
			List<List<ASTNode>> p = ms.selectInstances();

			System.out.println("[HelloWorldAction - accomplishRefactoring]  " + "Code Clones:");

			// For each set of clone to be refactored
			toBecomeClass(ih, p);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void toBecomeClass(InstancesHandler ih, List<List<ASTNode>> p)
			throws JavaModelException, CoreException, BadLocationException {
		for(List<ASTNode> cloneSet: p) {

			List<ICompilationUnit> icus_involved = new LinkedList<>();
			sortNodes(cloneSet);

			// For each clone within the group selected 
			for(int i=0; i < cloneSet.size(); i++) {
				Statement stmt = (Statement) cloneSet.get(i);

				System.out.println("[HelloWorldAction - accomplishRefactoring]  " + stmt.toString());

				extractMethod(stmt, icus_involved);
			}
			List<IMethod> extractedMethods = getExtractedMethods(icus_involved);

			boolean sameClass = true;
			for(int i=0; i < icus_involved.size(); i++) {
				for(int j=i+1; j < icus_involved.size(); j++) {

					String name1 = buildFullName(icus_involved.get(i));
					String name2 = buildFullName(icus_involved.get(j));

					//System.out.println("[HelloWorldAction  - accomplishRefactoring]" + name1);

					if(!name1.equals(name2)) {
						sameClass = false;
						break;
					}

				}
			}

			if(sameClass)
				selectOneOfExtracted(extractedMethods);
			else
				selectOneOfExtractedSameHierarchy(extractedMethods, icus_involved, ih);

			for(ICompilationUnit icu: icus_involved)
				icu.close();
		}
	}

	private void selectOneOfExtracted(List<IMethod> extractedMethods) throws JavaModelException {
		if(extractedMethods.size() >= 2) {
			boolean toKeepChosen = false;
			for(int i=0; i < extractedMethods.size(); i++) {
				IMethod extr = extractedMethods.get(i);
				// A method has to be kept if:
				// 1) No other method has already been chosen
				// AND
				// 2) either this is the last of the methods extracted 
				//    OR this is the first "static" method extracted
				if(!toKeepChosen &&
						(extr.getSignature().contains(" static ") ||
								(i + 1) == extractedMethods.size())) {
					toKeepChosen = true;	
				}
				else {
					extr.delete(true, null);
				}
			}
		}
	}

	private String buildFullName(ICompilationUnit icu) throws JavaModelException {
		String packageDirty = icu.getPackageDeclarations()[0].toString();
		String className = icu.getElementName();
		return packageDirty.split(" ")[1] + "." + className;
	}

	private void selectOneOfExtractedSameHierarchy(
			List<IMethod> extractedMethods,
			List<ICompilationUnit> icus_involved,
			InstancesHandler ih 
			) throws JavaModelException, MalformedTreeException, BadLocationException {


		String lcsSoFar = buildFullName(icus_involved.get(0)).replace(".java", "");
		for(int i=1; i < icus_involved.size(); i++) {
			String className =  buildFullName(icus_involved.get(i)).replace(".java", "");
			lcsSoFar = ih.findNearestCommonSuperclass(lcsSoFar, className);		
		}

		ICompilationUnit superClass = getSuperClassICompilationUnit(lcsSoFar);
		//CompilationUnit superClassCU = fromICUtoCU(superClass);


		IMethod kept = null;
		if(extractedMethods.size() >= 2) {
			kept = selectMethodToKeep(extractedMethods, kept);
			if(superClass == null) {
				applyExtractClass(icus_involved, kept);
			}
			else
				applyPullUpRefactoring(extractedMethods, lcsSoFar, superClass);
		}
	}

	private void applyExtractClass(List<ICompilationUnit> icus_involved, IMethod kept)
			throws BadLocationException, JavaModelException {
		// ApplyExtractSuperClass
		applyExtractSuperclass(kept);
		ICompilationUnit selectedICU = kept.getCompilationUnit();
		CompilationUnit selectedCU =  this.fromICUtoCU(selectedICU);

		ASTParser parser = ASTParser.newParser(AST.JLS11); 
		parser.setSource(selectedICU);

		//------------------------------------------
		//				System.out.println("[HelloWorldAction -  fromICUtoCU]  " + (ITypeRoot) selectedICU);
		//				
		//				parser.setResolveBindings(true); // we need bindings later on
		//				parser.setProject(selectedProject);
		//				CompilationUnit cu = (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
		//				
		//				parser.createASTs(ICompilationUnit[] compilationUnits, String[] bindingKeys, ASTRequestor requestor, IProgressMonitor monitor);

		//-------------------------------------------

		System.out.println("[HelloWorldAction  -  selectOneOfExtractedSameHierarchy] Parsent of ICompilationUnit"  +  selectedCU.getParent());				

		icus_involved.remove(selectedICU);
		Type superClassType = ((TypeDeclaration)selectedCU.types().get(0)).getSuperclassType();



		for(ICompilationUnit icu: icus_involved) {
			CompilationUnit cu = fromICUtoCU(icu);

			ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
//
//					TypeDeclaration td = (TypeDeclaration) cu.types().get(0);
//					
//					Type superCU = ((TypeDeclaration)cu.types().get(0)).getSuperclassType();

//					ITrackedNodePosition tdLocation = rewriter.track(td);
//					ListRewrite lrw = rewriter.getListRewrite(cu, CompilationUnit.IMPORTS_PROPERTY);
//					lrw.replace(superCU, superClassType, null);
			
//					TextEdit edits = rewriter.rewriteAST(icu., null);
//					UndoEdit undo = null;
//					try {
//						undo = edits.apply(document);
//					} catch(MalformedTreeException e) {
//						e.printStackTrace();
//					} catch(BadLocationException e) {
//						e.printStackTrace();
//					}
			
			

			((TypeDeclaration) cu.types().get(0)).setSuperclassType(cu.getAST().newSimpleType(cu.getAST().newSimpleName("Prova")));
			

			//Document document = new Document(icu.getSource());
			Document document = new Document(cu.toString());
			
			TextEdit edits = rewriter.rewriteAST(document, null);
			edits.apply(document);
		    icu.getBuffer().setContents(document.get());
			
		    System.out.println("[HelloWorldAction  -  selectOneOfExtractedSameHierarchy  -  Document] ");
			System.out.println(document.get());
			
			
			//cu.rewrite(document, options)

			System.out.println("[HelloWorldAction  -  selectOneOfExtractedSameHierarchy  -  CompilationUnit]");
			System.out.println(cu);
		}



		kept.delete(true, null);
	}

	private CompilationUnit fromICUtoCU(ICompilationUnit icu) {
		ASTParser parser = ASTParser.newParser(AST.JLS11); 
		parser.setSource(icu);

		System.out.println("[HelloWorldAction -  fromICUtoCU]  " + (ITypeRoot) icu);

		parser.setResolveBindings(true); // we need bindings later on
		parser.setProject(selectedProject);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
		return cu;
	}

	private void applyExtractSuperclass(IMethod kept) {
		CodeGenerationSettings settings =
				JavaPreferencesSettings.getCodeGenerationSettings(kept.getCompilationUnit().getJavaProject());

		IMember[] ims = {kept};

		ExtractSupertypeProcessor refactoring = new ExtractSupertypeProcessor(ims, settings);
		try {
			refactoring.checkInitialConditions(new NullProgressMonitor());
			refactoring.setTypeName("Prova");
			CheckConditionsContext ccc = new CheckConditionsContext();
			ccc.add(new ResourceChangeChecker());
			refactoring.checkFinalConditions(new NullProgressMonitor(), ccc);
			Change change = refactoring.createChange(new NullProgressMonitor());
			change.perform(new NullProgressMonitor());
		} catch (OperationCanceledException | CoreException e) {
			e.printStackTrace();
		}
	}

	private void applyPullUpRefactoring(List<IMethod> extractedMethods, String lcsSoFar, ICompilationUnit superClass)
			throws JavaModelException {
		IMethod kept = null;
		if(extractedMethods.size() >= 2) {
			kept = selectMethodToKeep(extractedMethods, kept);
			pullUpMethod(lcsSoFar, superClass, kept);
			// delete the method in the subclass
			kept.delete(true, null);
		}
	}


	/**
	 * This method delete all the <extractedMethods> except one, which is the 
	 * one returned by the function
	 * 
	 * @param extractedMethods
	 * @param kept
	 * @return
	 * @throws JavaModelException
	 */
	private IMethod selectMethodToKeep(List<IMethod> extractedMethods, IMethod kept) throws JavaModelException {
		boolean toKeepChosen = false;
		for(int i=0; i < extractedMethods.size(); i++) {
			IMethod extr = extractedMethods.get(i);

			// A method has to be kept if:
			// 1) No other method has already been chosen
			// AND
			// 2) either this is the last of the methods extracted 
			//    OR this is the first "static" method extracted
			if(!toKeepChosen &&
					(extr.getSignature().contains(" static ") ||
							(i + 1) == extractedMethods.size())) {
				kept = extr;
				toKeepChosen = true;	
			}
			else {
				extr.delete(true, null);
			}
		}
		return kept;
	}

	private void pullUpMethod(String lcsSoFar, ICompilationUnit superClass, IMethod kept) throws JavaModelException {
		//PullUpDescriptor pud = new PullUpDescriptor();

		CodeGenerationSettings settings =
				JavaPreferencesSettings.getCodeGenerationSettings(kept.getCompilationUnit().getJavaProject());

		IMember[] ims = {kept};

		PullUpRefactoringProcessor mimp = new PullUpRefactoringProcessor(ims, settings);

		String[] path = lcsSoFar.split("\\.");
		String className = path[path.length - 1];

		superClass.getType(className);

		mimp.setDestinationType(superClass.getAllTypes()[0]);

		try {
			mimp.checkInitialConditions(new NullProgressMonitor());
			CheckConditionsContext ccc = new CheckConditionsContext();
			ccc.add(new ResourceChangeChecker());
			mimp.checkFinalConditions(new NullProgressMonitor(), ccc);
			Change change = mimp.createChange(new NullProgressMonitor());
			change.perform(new NullProgressMonitor());
		} catch (OperationCanceledException | CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ICompilationUnit getSuperClassICompilationUnit(String lcsSoFar) throws JavaModelException {
		ICompilationUnit superClass = null;

		try {
			for(IPackageFragmentRoot ipdr: this.selectedProject.getPackageFragmentRoots()) {
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
			// but is imported froma library
			return null;
		}
		//superClass.open(new NullProgressMonitor());
		return superClass;
	}

	private List<IMethod> getExtractedMethods(List<ICompilationUnit> workingCopies) throws JavaModelException {
		List<IMethod> extractedMethods = new LinkedList<>();

		for(ICompilationUnit workingCopy: workingCopies) {
			workingCopy.commitWorkingCopy(true, new NullProgressMonitor());
			workingCopy.reconcile(AST.JLS11, true, null, new NullProgressMonitor());

			IMethod[] methods = workingCopy.getTypes()[0].getMethods();

			// Find the extracted methods
			for(int j=0; j < methods.length; j++) {
				IMethod im = methods[j];
				if(im.getElementName().equals(this.extMethodName)){
					extractedMethods.add(im);
				}
			}
		}
		return extractedMethods;
	}

	private void extractMethod(Statement stmt, List<ICompilationUnit> icus_involved) throws JavaModelException, CoreException {
		ICompilationUnit workingCopy = getICompilationUnit(stmt);
		workingCopy.becomeWorkingCopy(new NullProgressMonitor());

		ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(
				workingCopy, stmt.getStartPosition(), stmt.getLength());

		this.extMethodName = refactoring.getMethodName();
		refactoring.checkAllConditions(new NullProgressMonitor());
		Change change = refactoring.createChange(new NullProgressMonitor());
		change.perform(new NullProgressMonitor());
		icus_involved.add(workingCopy);

	}

	private void sortNodes(List<ASTNode> nodes) {
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

	private ICompilationUnit getICompilationUnit(Statement stmt) {
		ASTNode parent = stmt.getParent();
		while(!(parent instanceof CompilationUnit))
			parent = parent.getParent();
		CompilationUnit cu = (CompilationUnit) parent;
		ICompilationUnit icu = (ICompilationUnit) cu.getJavaElement();
		return icu;
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}

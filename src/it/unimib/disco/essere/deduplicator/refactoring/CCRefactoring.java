package it.unimib.disco.essere.deduplicator.refactoring;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.text.edits.MalformedTreeException;

import it.unimib.disco.essere.deduplicator.preprocessing.InstancesHandler;

public abstract class CCRefactoring {

	protected List<ASTNode> cloneSet;
	protected List<ICompilationUnit> icus_involved;
	protected String extractedMethodName;
	protected List<IMethod> extractedMethods;
	protected IJavaProject project;

	public static CCRefactoring selectRefactoringTechniques(
			InstancesHandler ih, 
			List<List<ASTNode>> cloneSets,
			IJavaProject selectedProject) throws JavaModelException {

		CCRefactoring refactoring = null;
		
		for(List<ASTNode> cloneSet: cloneSets) {
			
			for(int i=0; i < cloneSet.size(); i++) {
				Statement stmt = (Statement) cloneSet.get(i);

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
				String lcsSoFar = 
						buildFullName(icus_involved.get(0)).replace(".java", "");
				for(int i=1; i < icus_involved.size(); i++) {
					String className =  
							buildFullName(icus_involved.get(i)).replace(".java", "");
					lcsSoFar = ih.findNearestCommonSuperclass(lcsSoFar, className);		
				}

				System.out.println("[CCRefactoring - selectRefactoringTechniques]  " + lcsSoFar);
				
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
		}


		//		for(ICompilationUnit icu: icus_involved)
		//			icu.close();

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

		ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(
				workingCopy, stmt.getStartPosition(), stmt.getLength());

		this.extractedMethodName = refactoring.getMethodName();
		refactoring.checkAllConditions(new NullProgressMonitor());
		Change change = refactoring.createChange(new NullProgressMonitor());
		change.perform(new NullProgressMonitor());

		return workingCopy;
	}

	private List<IMethod> getExtractedMethods() throws JavaModelException {
		List<IMethod> extractedMethods = new LinkedList<>();

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

	protected IMethod selectMethodToKeep(List<IMethod> extractedMethods) throws JavaModelException {
		IMethod keep = null;
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
					keep = extr;
				}
				else {
					extr.delete(true, null);
				}
			}
		}
		return keep;
	}

	public abstract void apply()  throws UnsuccessfulRefactoringException;

}


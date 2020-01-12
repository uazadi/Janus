package it.unimib.disco.essere.janus.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import it.unimib.disco.essere.janus.behaviouralcheck.BehevioralCheckException;
import it.unimib.disco.essere.janus.behaviouralcheck.CheckCompilation;
import it.unimib.disco.essere.janus.behaviouralcheck.JUnitCheck;
import it.unimib.disco.essere.janus.behaviouralcheck.MainClassCheck;
import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.preprocessing.PreprocessingFacade;
import it.unimib.disco.essere.janus.rad.moea.CustomAbstractProblem;
import it.unimib.disco.essere.janus.rad.moea.MethodSelector;
import it.unimib.disco.essere.janus.rad.moea.MultiObjective;
import it.unimib.disco.essere.janus.rad.moea.SingleObjective;
import it.unimib.disco.essere.janus.refactoring.CCRefactoring;
import it.unimib.disco.essere.janus.refactoring.NotRefactorableCodeClones;
import it.unimib.disco.essere.janus.refactoring.UnsuccessfulRefactoringException;
import it.unimib.disco.essere.janus.versioning.GitVersioner;
import it.unimib.disco.essere.janus.versioning.VersionerException;

public class WorkflowHandler {

	private Configuration config;

	private GitVersioner versioner;
	
	private InstancesHandler ih;
	
	private static WorkflowHandler instance;

	private List<ICompilationUnit> compUnitInvolved;
	private List<List<ASTNode>> clones;

	private WorkflowHandler() {}
	
	public static WorkflowHandler getInstance() {
		if(instance == null) {
			instance = new WorkflowHandler();
		}
		return instance;
	}
	
	public void setConfig(Configuration config) {
		this.config = config;
	}
	
	public void initGitRepo() throws VersionerException {
		System.out.println("[WorkflowHandler] Initiating git repo...");
		
		if(config.gitRepo == null || config.gitRepo.equals("")) {
			versioner = new GitVersioner(config.selectedProject);
		}else {
			versioner = new GitVersioner(config.gitRepo);
		}

		versioner.newBranch("CCrefactoring");
	}

	public void saveChanges() throws JavaModelException {
		
		System.out.println("[WorkflowHandler] Saving changes...");

		
		for(ICompilationUnit icu: compUnitInvolved) {
			try {
				icu.commitWorkingCopy(true, new NullProgressMonitor());
			}catch(JavaModelException e) {
				// file added because of CCExtractSuperclass
			}
			icu.close();
		}

		IWorkbenchPage page = config.workbenchPage;
		IEditorPart editor = page.getActiveEditor();
		page.saveEditor(editor, false /* confirm */);

		for(ICompilationUnit icu: compUnitInvolved)
			icu.close();
	}

	public void commitChanges() throws VersionerException {
		
		System.out.println("[WorkflowHandler] Commiting changes...");
		
		List<String> pathOfCompUnitInvolved = new ArrayList<String>();
		for(ICompilationUnit icu: compUnitInvolved) {
			pathOfCompUnitInvolved.add(icu.getPath().toString());
		}

		versioner.commit(pathOfCompUnitInvolved);
	}

	public boolean runTests(Configuration conf2) throws BehevioralCheckException {
		
		System.out.println("[WorkflowHandler] Running tests...");
		
		//new CheckCompilation().check(config.selectedProject);
		
		MainClassCheck mainCheck = new MainClassCheck(conf2.selectedProject, conf2.mainClasses);
		boolean mainResults = mainCheck.run();
		
		if(!mainResults)
			return false;

		
		
		JUnitCheck junitCheck = new JUnitCheck(config.selectedProject);
		junitCheck.setJunitClasses(config.junitClasses);
		boolean junitResults = junitCheck.run();
		
		if(!junitResults)
			return false;
		
		return true;
	}

	public void rollbackChanges() throws VersionerException {
		
		System.out.println("[WorkflowHandler] Applying rollback...");
		
		versioner.rollback();
	}

	public List<List<ASTNode>> selectClones() {
		
		System.out.println("[WorkflowHandler] Detectiong the clones to refactor...");

		try {
			
			PreprocessingFacade preprossesing = new PreprocessingFacade();
			ih = preprossesing.parseSourceCode(config.selectedProject);

			CustomAbstractProblem opt = null;
			List<Double> weightDupCode = new ArrayList<Double>();
			weightDupCode.add(0, config.weightNocs);
			weightDupCode.add(1, config.weightAlcs);
			weightDupCode.add(2, config.weightAnoc);
			List<Double> weightRefRisk = new ArrayList<Double>();
			weightRefRisk.add(0, config.weightVarSim);
			weightRefRisk.add(1, config.weightCodePos);
			if(config.typeOfOptimization.equals("Multi"))
				opt = new MultiObjective(ih, 30, weightDupCode);	
			else 
				opt = new SingleObjective(ih, weightDupCode, weightRefRisk);

			MethodSelector ms = new MethodSelector(
					opt, 
					config.algorithmName, 
					config.iterationRad, 
					ih);
			clones = ms.selectInstances();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return clones;

	}
	
	public void accomplishRefactoring() throws JavaModelException {
		
		System.out.println("[WorkflowHandler] Appling refactoring...");
		
		List<CCRefactoring> refactorings;
		
		compUnitInvolved = new ArrayList<ICompilationUnit>();
		
		try {
			refactorings = CCRefactoring.selectRefactoringTechniques(ih, clones, config.selectedProject);
			
			System.out.println("------------------>" + refactorings.size());
			
			for(CCRefactoring ccr: refactorings) {
				System.out.println("[WorkflowHandler] Technique selected: " + ccr.getClass());
				ccr.apply();
				compUnitInvolved.addAll(ccr.getCompilationUnitInvolved());
			}

			ih.clear();
		} catch (NotRefactorableCodeClones | UnsuccessfulRefactoringException e) {
			
			//e.printStackTrace();
			
			if(!config.suggestNotExactMatch) {
				//TODO add this method to the one that should not be refactored
			}
		}
		
		
	}
}

package it.unimib.disco.essere.janus.gui;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.IWorkbenchPage;

public class Configuration {
	
	protected IJavaProject selectedProject;
	
	protected IWorkbenchPage workbenchPage;
	
	protected boolean runStepByStep = true;
	
	
	// General
	protected int iterationAlg = 101;
	
	protected String gitRepo = null;
	
	protected boolean suggestNotExactMatch = false;
	
	// Preprocessing
	
	protected boolean includeGui = false;
	
	protected boolean includeTest = false;
	
	// Detection
	protected int iterationRad = 30;
	
	protected double weightNocs = 0.5;
	protected double weightAlcs = 0.25;
	protected double weightAnoc = 0.25;
	
	protected double weightVarSim = 0.5;
	protected double weightCodePos = 0.5;
	
	protected String algorithmName = "NSGAII";
	
	// Only two possible values: Multi or Single
	protected String typeOfOptimization = "Multi";
	
	
	// Refactoring
	
	protected boolean sameClass;
	
	protected boolean sameHierInt;
	
	protected boolean sameHierExt;
	
	// Test
	
	protected Set<String> mainClasses;
	
	protected Map<IType, Boolean> junitClasses;

	public Configuration() {}

	@Override
	public String toString() {
		return "Configuration [selectedProject=" + selectedProject.getElementName() + ", iterationAlg=" + iterationAlg
				+ ", gitRepo=" + gitRepo + ", suggestNotExactMatch=" + suggestNotExactMatch + ", includeGui="
				+ includeGui + ", includeTest=" + includeTest + ", iterationRad=" + iterationRad + ", weightNocs="
				+ weightNocs + ", weightAlcs=" + weightAlcs + ", weightAnoc=" + weightAnoc + ", weightVarSim="
				+ weightVarSim + ", weightCodePos=" + weightCodePos + ", algorithmName=" + algorithmName
				+ ", sameClass=" + sameClass + ", sameHierInt=" + sameHierInt + ", sameHierExt=" + sameHierExt
				+ ", mainClasses=" + mainClasses + ", junitClasses=" + junitClasses + "]";
	}
	
	

}

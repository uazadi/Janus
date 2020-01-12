package it.unimib.disco.essere.janus.preprocessing.modelling;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;

public class JavaDirectory extends JavaContainer {

	public static String javaExtension = ".java";

	/** The path of the directory that contain all 
	 *  the .java that have to be analyzed*/
	private String directory;

	private IJavaProject eclipseProject; 

	private String prevDir;

	/**
	 * Create an instance of JavaDirectory and create an instance of all its children (JavaFile) 
	 * 
	 * @param directory the directory that contain all the .java files that have to be analyzed
	 * @throws Exception 
	 */
	public JavaDirectory(String directory) throws IOException {
		this.directory = directory;
	}

	public JavaDirectory(IJavaProject project) throws IOException {
		this.eclipseProject = project;
	}

	public void startPreprocessing() throws IOException {
		children = extractChildren();
	}

	@Override
	public JavaContainer getParent() {
		return null;
	}

	@Override
	public String getName() {
		return directory;
	}

	@Override
	public String toString() {
		return "JavaDirectory [directory=" + directory + ", javaFiles=" + children + "]";
	}	

	@Override
	protected List<JavaComponent> extractChildren() throws IOException {
		if(eclipseProject == null)
			return extractFromDir();
		else
			return extractFromEclipse();
	}


	private List<JavaComponent> extractFromEclipse() {
		List<JavaComponent> javaFiles = new LinkedList<JavaComponent>();
		try {
			for (IPackageFragmentRoot pfr : eclipseProject.getPackageFragmentRoots()) {
				// if it is not a library (jar file)
				if (!pfr.toString().contains(".jar")) {
					for (IJavaElement pf : pfr.getChildren()) {
						//if it not a package containing test cases
						if(!pf.getElementName().toLowerCase().contains("test")) {
							for (ICompilationUnit ci : ((IPackageFragment) pf).getCompilationUnits()) {
								addJavaFile(javaFiles, ci);
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return javaFiles;
	}

	private List<JavaComponent> extractFromDir() throws IOException {
		List<JavaComponent> javaFiles = new LinkedList<JavaComponent>();
		Files.walk(Paths.get(directory), FileVisitOption.FOLLOW_LINKS).filter(file -> {
			return file.toString().contains(javaExtension);
		}).forEach(javaFile -> {
			String dir = javaFile.toString().substring(0, javaFile.toString().lastIndexOf("/"));
			if(!dir.equals(prevDir))
				System.out.println("Loading " + dir + "...");
			prevDir = dir;
			addJavaFile(javaFiles, javaFile);
		});
		return javaFiles;
	}

	private void addJavaFile(List<JavaComponent> javaFiles, Path javaFile) {
		try {
			javaFiles.add(new JavaFile(javaFile.toString(), this));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void addJavaFile(
			List<JavaComponent> javaFiles, 
			ICompilationUnit javaFile) {
		try {
			javaFiles.add(new JavaFile(javaFile, this));
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}

	/**
	 * The directory does not have a node in the Eclipse JDT AST
	 * @return null
	 * */
	@Override
	public ASTNode getNode() {
		return null;
	}

}

package it.unimib.disco.essere.janus.preprocessing.modelling;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.Document;
import org.osgi.framework.BundleContext;
//import org.eclipse.jface.text.Document;

public class JavaFile extends JavaContainer {

	/** The path of the .java file */
	private String path;

	/**
	 * The instance that allows to work with the Abstract Syntax Tree of the Java
	 * file
	 */
	private CompilationUnit node;

	/** The directory in which */
	private JavaDirectory dir;

	/** The package in which the class is defined */
	private String packageName;

	/** The list of all classes/packages imported by the class */
	private List<String> importNames;

	/**
	 * Create an instance of JavaFile and create an instance of all its children
	 * (JavaClass)
	 * 
	 * @param path the path of the .java file
	 * @throws Exception
	 */
	public JavaFile(String path, JavaDirectory dir) throws Exception {
		this.dir = dir;
		this.path = path.substring(dir.getName().length());
		this.node = generateAST(path);
		this.packageName = (node.getPackage() != null)
				? node.getPackage().getName().toString()
				: "";

		this.importNames = extractImports();
		this.children = extractChildren();
	}

	public JavaFile(ICompilationUnit icu, JavaDirectory dir) throws Exception {
		this.dir = dir;
		this.path = null;
		this.node = generateAST(icu);
		this.packageName = (node.getPackage() != null)
				? node.getPackage().getName().toString()
				: "";

		this.importNames = extractImports();
		this.children = extractChildren();
	}

	@Override
	public String getName() {
		return path;
	}

	@Override
	public ASTNode getNode() {
		return node;
	}

	public String getPackage() {
		return packageName;
	}

	public List<String> getImports() {
		return importNames;
	}

	@Override
	public JavaContainer getParent() {
		return dir;
	}

	@Override
	public String toString() {
		return "JavaFile [path=" + path + ", node=" + node + ", javaClasses=" + children + ", dir=" + dir
				+ ", packageName=" + packageName + ", importNames=" + importNames + "]";
	}

	@Override
	protected List<JavaComponent> extractChildren() throws Exception {
		ArrayList<JavaComponent> classes = new ArrayList<JavaComponent>();
		for (Object obj : node.types()) {
			JavaClass c = new JavaClass((TypeDeclaration) obj, this);
			classes.add(c);
		}
		return classes;
	}

	private List<String> extractImports() {
		ArrayList<String> imports = new ArrayList<String>();
		for (Object imp : node.imports()) {
			imports.add(imp.toString().replace("import", "").replaceAll("[ ;]", ""));
		}

		return imports;
	}

	private CompilationUnit generateAST(ICompilationUnit cu) throws IOException {
		ASTParser parser = ASTParser.newParser(AST.JLS11);
		parser.setSource(cu);
		return (CompilationUnit) parser.createAST(null);
	}

	private CompilationUnit generateAST(String stringPath) throws IOException {
		String content = readJavaFile(stringPath, Charset.defaultCharset());
		Document doc = new Document(content);
		ASTParser parser = ASTParser.newParser(AST.JLS11);
		parser.setResolveBindings(true);
		parser.setSource(doc.get().toCharArray());
		return (CompilationUnit) parser.createAST(null);
	}

//-----------------------------------------------------------------------------		

//		ResourcesPlugin p = new ResourcesPlugin();
//    	try {
//			p.start(null);
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//		}
//		ResourcesPlugin plugin = new ResourcesPlugin();
//		plugin.start(null);
//		IPath path = new Path(name);
//		IFile file = project.getFile(path);
//-----------------------------------------------------------------------------

//		ASTParser parser = ASTParser.newParser(AST.JLS11);
//		
//		
//		IWorkspace workspace = ResourcesPlugin.getWorkspace(); 
//		IPath path = Path.fromOSString(stringPath);
//		IFile file = workspace.getRoot().getFileForLocation(path);
//		ICompilationUnit compilationUnit = (ICompilationUnit)JavaCore.create(file);
//		parser.setSource(compilationUnit);
//		
//		return (CompilationUnit) parser.createAST(null);

	// }

	private String readJavaFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

}

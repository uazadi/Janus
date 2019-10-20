package it.unimib.disco.essere.deduplicator.behaviouralcheck;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.junit.launcher.ITestFinder;
import org.eclipse.jdt.internal.junit.launcher.JUnit5TestFinder;
import org.eclipse.jdt.launching.JavaRuntime;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class JUnitCheck extends BehaviouralCheck {

	private Map<IType, Boolean> junitClasses;

	public JUnitCheck(IJavaProject selectedProject) {
		super(selectedProject);
		junitClasses = new HashMap<IType, Boolean>();
	}
	
	public Map<IType, Boolean> getJunitClasses(){
		return junitClasses;
	}
	
	public void setJunitClasses(Map<IType, Boolean> junitClasses){
		this.junitClasses = junitClasses;
	}

	/**
	 * @param finder allows to specify the version of JUnit to be used: 
	 * 				new JUnit3TestFinder(), new JUnit4TestFinder() 
	 * 				or new JUnit5TestFinder().
	 * @return the map in which the keys are all the test classes found and
	 * 			the value allows to specify if each class has to be used (true) 
	 * 			or not (false) during the behavioral check.
	 * @throws CoreException
	 */
	public Map<IType, Boolean> findJunitClasses(ITestFinder finder) throws CoreException{
		Set<IType> junits =  new HashSet<IType>();
		finder.findTestsInContainer(selectedProject, junits, null);

		for(IType junitClass: junits) {
			junitClasses.put(junitClass, true);
		}

		return junitClasses;
	}
	
	public Map<IType, Boolean> findJunitClasses() throws CoreException{
		return findJunitClasses(new JUnit5TestFinder());
	}

	public boolean run() {

		for(IType junitClass: this.junitClasses.keySet()) {
			
			String name = junitClass.getFullyQualifiedName();
			
			if(this.junitClasses.get(junitClass)) {

				try {

					ICompilationUnit anUnit = selectedProject.findType(name).getCompilationUnit();

					String unitPath = anUnit.getResource().getProject().getLocationURI().toString();
					URL binURI = new URL(unitPath + "/target/test-classes/");

					String[] classPathEntries = JavaRuntime.computeDefaultRuntimeClassPath(selectedProject);
					List<URL> urlList = new ArrayList<URL>();
					urlList.add(binURI);
					for (int i = 0; i < classPathEntries.length; i++) {
						String entry = classPathEntries[i];
						IPath iPath = new Path(entry);
						URL url = iPath.toFile().toURI().toURL();
						urlList.add(url);

					}

					ClassLoader parentClassLoader = this.getClass().getClassLoader();
					URL[] urls = urlList.toArray(new URL[urlList.size()]);
					URLClassLoader classLoader = new URLClassLoader(urls, parentClassLoader);

					Class<?> act = classLoader.loadClass(name);
					
					Result result = new JUnitCore().run(act);

					System.out.println("Was succesfull? " + result.wasSuccessful());
					for(Failure f: result.getFailures()) {
						System.out.println("[MESSAGE] " + f.getMessage());
						System.out.println("[DESCRIPTION] " + f.getDescription());
						System.out.println("[EXCEPTION] ");
						f.getException().printStackTrace();
					}
					
					if(!result.wasSuccessful())
						return false;

				} catch (CoreException | MalformedURLException | ClassNotFoundException e) {
					System.out.println("[JUnitCheck] Error in JUnit Class: " + name);
					System.out.println("[JUnitCheck] The error is: " + e.getMessage());
				}
				
			}
		}
		
		return true;
	}







}

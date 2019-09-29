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
import org.eclipse.jdt.internal.junit.launcher.JUnit3TestFinder;
import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder;
import org.eclipse.jdt.internal.junit.launcher.JUnit5TestFinder;
import org.eclipse.jdt.launching.JavaRuntime;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class JUnitCheck {

	private IJavaProject selectedProject;
	private Map<IType, Boolean> junitClasses;

	public JUnitCheck(IJavaProject selectedProject) {
		this.selectedProject = selectedProject;
		junitClasses = new HashMap<IType, Boolean>();
	}
	
	public Map<IType, Boolean> getJunitClasses(){
		return junitClasses;
	}
	
	public void setJunitClasses(Map<IType, Boolean> junitClasses){
		this.junitClasses = junitClasses;
	}

	public Map<IType, Boolean> findJunitClasses() throws CoreException{
		//		Set<IType> retVal3 =  new HashSet<IType>();
		//		new JUnit3TestFinder().findTestsInContainer(selectedProject, retVal3, null);
		//		System.out.println("JUnit3TestFinder " + retVal3.size());

		//		Set<IType> retVal4 =  new HashSet<IType>();
		//		new JUnit4TestFinder().findTestsInContainer(selectedProject, retVal4, null);
		//		System.out.println("JUnit4TestFinder " + retVal4.size());

		Set<IType> junits =  new HashSet<IType>();
		new JUnit5TestFinder().findTestsInContainer(selectedProject, junits, null);

		for(IType junitClass: junits) {
			junitClasses.put(junitClass, true);
		}

		return junitClasses;



		//		IType[] retVal4arr = new IType[junits.size()];
		//		junits.toArray(retVal4arr);

		//		String name = retVal4arr[4].getFullyQualifiedName();
		//		System.out.println("Fully Qualified Name: " + name);
	}

	public void run() {

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

					Result a = new JUnitCore().run(act);

					System.out.println("Was succesfull? " + a.wasSuccessful());
					for(Failure f: a.getFailures()) {
						System.out.println("[MESSAGE] " + f.getMessage());
						System.out.println("[DESCRIPTION] " + f.getDescription());
						System.out.println("[EXCEPTION] ");
						f.getException().printStackTrace();
					}

				} catch (CoreException | MalformedURLException | ClassNotFoundException e) {
					System.out.println("[JUnitCheck] Error JUnit Class " + name);
				}
			}
		}
	}







}

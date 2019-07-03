package it.unimib.disco.essere.deduplicator.preprocessing;

import java.io.File;

import org.eclipse.jdt.core.IJavaProject;

import it.unimib.disco.essere.deduplicator.preprocessing.modelling.JavaDirectory;
public class PreprocessingFacade {
	
	public InstancesHandler parseSourceCode(String directory) throws Exception {
		File f = new File(directory);
		if (f.isDirectory()){
			JavaDirectory jd = new JavaDirectory(directory);
			jd.startPreprocessing();
		}
		return MethodHandler.getInstance();
	}
	
	public InstancesHandler parseSourceCode(IJavaProject project) throws Exception {
		JavaDirectory jd = new JavaDirectory(project);
		jd.startPreprocessing();
		return MethodHandler.getInstance();
	}
	
	public static void main(String args[]) {
		PreprocessingFacade f = new PreprocessingFacade();
		try {
			InstancesHandler a = f.parseSourceCode("/home/umberto/Documents/OUTLINE/OUTLINE/src/main/java/it/unimib/disco/essere");
			System.out.println(a.findNearestCommonSuperclass(
					"it.unimib.disco.essere.core.PredictionHandler", 
					"it.unimib.disco.essere.core.ClassificationHandler"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
}

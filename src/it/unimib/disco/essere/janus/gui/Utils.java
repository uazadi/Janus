package it.unimib.disco.essere.janus.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.json.JSONArray;
import org.json.JSONObject;



public class Utils {
	
	public static IProject PROJECT;

	private Utils()  {}
	
	public static List<Object> checkJanusignore(String key){
		
		List<Object> toBeIgnore = new ArrayList<Object>();
		
		File file = new File(PROJECT.getPathVariableManager().getURIValue("PROJECT_LOC").getPath() + "/.janusignore");
		
		
	    String content;
		try {
			content = FileUtils.readFileToString(new File(file.getPath()), "utf-8");
			
			// Convert JSON string to JSONObject
		    JSONObject ignores = new JSONObject(content); 
		    
		    toBeIgnore = ((JSONArray) ignores.get(key)).toList();
		    
		    
		} catch (Exception e) {
			
			e.printStackTrace();
			
			System.out.println("Unable to read the .janusignore file, please check format");
		}
		
		return toBeIgnore;
	}

	public static List<List<String>> loadAlgorithmsConfigFile() throws IOException{

		List<List<String>> decodedCSV= new ArrayList<List<String>>();

		FileReader fr = 
				//new FileReader("./icons/algorithms.csv"); 
				new FileReader("/home/umberto/Desktop/Tesi/eclipse_workspace/Janus/icons/algorithms.csv");
				//new FileReader("/home/uazadi/Documents/Tesi/Janus/icons/algorithms.csv"); 

		BufferedReader br = new BufferedReader(fr);  
		
		// Skip the first line since it's the header
		br.readLine();
		String line = br.readLine();  
		
		while( line != null) {  
			line = line.replace("\n", "");
			String[] fields = line.split(";");
			decodedCSV.add(Arrays.asList(fields));
			line = br.readLine();  
		}
		
		br.close();
		
		return decodedCSV;
	}
	
	public static void main() {
		
	}
}

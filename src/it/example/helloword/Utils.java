package it.example.helloword;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

	private Utils() {
		// TODO Auto-generated constructor stub
	}

	public static List<List<String>> loadAlgorithmsConfigFile() throws IOException{

		List<List<String>> decodedCSV= new ArrayList<List<String>>();

		FileReader fr = 
				new FileReader("/home/umberto/Desktop/Tesi/eclipse_workspace/it.package.helloword/icons/algorithms.csv"); 

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
}

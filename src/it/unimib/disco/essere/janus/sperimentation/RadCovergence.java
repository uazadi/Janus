package it.unimib.disco.essere.janus.sperimentation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

import it.unimib.disco.essere.janus.preprocessing.InstancesHandler;
import it.unimib.disco.essere.janus.preprocessing.PreprocessingFacade;
import it.unimib.disco.essere.janus.rad.moea.CustomAbstractProblem;
import it.unimib.disco.essere.janus.rad.moea.MethodSelector;
import it.unimib.disco.essere.janus.rad.moea.MultiObjective;
import it.unimib.disco.essere.janus.rad.moea.SingleObjective;

public class RadCovergence {

	public RadCovergence() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {
		PreprocessingFacade preprossesing = new PreprocessingFacade();
		InstancesHandler ih = preprossesing.parseSourceCode("/home/umberto/Documents/WN_Sources/jasml/jasml-0.10/src/src");


		List<Double> weightDupCode = new ArrayList<Double>();
		weightDupCode.add(0, 0.33);
		weightDupCode.add(1, 0.33);
		weightDupCode.add(2, 0.33);
		List<Double> weightRefRisk = new ArrayList<Double>();
		weightRefRisk.add(0, 0.5);
		weightRefRisk.add(1, 0.5);


		int numOfItertion = 100;
		int numOfAttempt = 1;


		runMultiObjSelector(ih, weightDupCode, numOfItertion, numOfAttempt, "DBEA", "");

	}

	private static void runMultiObjSelector(
			InstancesHandler ih, 
			List<Double> weightDupCode, 
			int numOfItertion,
			int numOfAttempt,
			String algorithm,
			String project) {

		PrintWriter writer;
		try {
			writer = new PrintWriter(algorithm + "_" + project + ".json", "UTF-8");

			writer.println("{");

			writer.println("\t\"algorithm\": \"" + algorithm + "\",");
			writer.println("\t\"project\": \"" + project + "\",");
			writer.println("\t\"attempts\": [");


			for(int i=0; i < numOfAttempt; i++) {

				System.out.println("Printing iteration: " + i);

				Instant start = Instant.now();

				MethodSelector ms =new MethodSelector(
						new MultiObjective(ih, 30, weightDupCode), 
						algorithm, 
						numOfItertion, 
						ih);

				Instant end = Instant.now();

				long time = Duration.between(start, end).getSeconds();

				List<List<ASTNode>> clones = ms.selectInstances();
				List<List<Double>> values = ms.getListOfFittests();

				writer.println("\t\t{");

				writer.println("\t\t\t\"number\": " + i + ",");

				writer.println("\t\t\t\"duration\": " + time + ",");

				String funcValue = "\t\t\t\"func_value\": ["
						+ values.get(0).get(values.get(0).size() - 1);
				for (int j=1; j < values.size(); j++)
					funcValue += ", " + values.get(j).get(values.get(j).size() - 1);
				writer.println(funcValue + "],");

				String clonesJson = "\t\t\t\"clones\": [\n";


				for(int j=0; j < clones.size(); j++) {

					writer.flush();

					String info = "\t\t\t\t{\n";
					info += "\t\t\t\t\t\"info\": [\n";
					for(int k=0; k < clones.get(j).size(); k++) {
						info += "\t\t\t\t\t\t{\"package\": "
								+ "\"" +((CompilationUnit) clones.get(j).get(k).getParent().getParent().getParent().getParent()).getPackage().toString().split(" ")[1].replace("\n", "") + "\", "			
								+ "\"class\": "
								+ "\"" + ((TypeDeclaration) clones.get(j).get(k).getParent().getParent().getParent()).getName() + "\", "
								+ "\"method\": "
								+ "\"" + ((MethodDeclaration) clones.get(j).get(k).getParent().getParent()).getName() + "\", "
								+ "\"initial_pos\": "
								+  clones.get(j).get(k).getStartPosition() 
								+ "},\n";
					}
					
					
					
					info = info.substring(0, info.length() - 2) + "\n"
							+ "\t\t\t\t\t],\n";

					info += "\t\t\t\t\t\"fittest_code\": \"" + clones.get(j).get(0).toString().replace("\n", " <newline> ") + "\"\n";
					info += "\t\t\t\t}";

					clonesJson += info + ",\n";

				}

				clonesJson = clonesJson.substring(0, clonesJson.length() - 2) +"\n"
						+ "\t\t\t],";

				writer.println(clonesJson);

				String iterationJson = "\t\t\t\"iteration\": [\n";

				for(int j=0; j < values.get(0).size(); j++) {

					writer.flush();

					String iter = "\t\t\t\t{\n";
					
					iter += "\t\t\t\t\t\"number\": " + j + ",\n";
					
					String funcIter = "\t\t\t\t\t\"func_value\": ["
							+ values.get(0).get(j);
					
					for (int k=1; k < values.size(); k++)
						funcIter += ", " + values.get(k).get(j);
					
					iter += funcIter + "]\n"
							+ "\t\t\t\t},\n";
					
					iterationJson += iter;
				}
				
				iterationJson = iterationJson.substring(0, iterationJson.length() - 2) 
						+ "\t\t\t]";
				
				writer.println(iterationJson);

				writer.println("\t\t}");
			}


			writer.println("\t]");

			writer.println("}");

			writer.close();


		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}
}


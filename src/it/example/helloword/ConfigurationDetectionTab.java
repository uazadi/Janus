package it.example.helloword;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ConfigurationDetectionTab extends GUIClass {

	JComboBox<String> comboSingleObj;
	JComboBox<String> comboMultiObj;
	JRadioButton radioMultiObj;
	JRadioButton radioSingleObj;

	

	public ConfigurationDetectionTab(Configuration config) {
		super(config);
	}

	public JScrollPane getPanel() {

		JPanel detectionPanel = new JPanel();

		detectionPanel.setLayout(new GridLayout(4, 0, 0, 0));

		// Begin Number of iteration of the entire algorithm________________
		JPanel numIterRadPanel = new JPanel();
		detectionPanel.add(numIterRadPanel);

		numIterRadPanel.add(
				addFieldDescription(
						"How accurate you want me to be in the code clone selection?"
						)
				);

		JSlider sliderIterRad = addSliderWithValue(numIterRadPanel, 10, 200, false);
		
		sliderIterRad.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				config.iterationRad = sliderIterRad.getValue();
			}
		});

		JLabel labelInfoNumIterRad = this.getInfoLabel(
				"The RAD (Refactoring Aware Detection) is the detection\n"
						+ "algorithm that allows me to select the best set of code\n"
						+ "clones to be refactored at each step. This problem is\n"
						+ "defined as an optimization solved through an evolutionary\n"
						+ "algorithm."
						+ "\n\n"
						+ "This field allows you to set an upper bound to the number\n"
						+ "of iterations that the evolutionary algorithm can accomplish.\n"
						+ "The higher this number is the more accurate the code clones\n"
						+ "will be in term of which kind of code clone to prioritize\n"
						+ "and in term of refactoring effort\n",
				"Info on  RAD number of iteration");
		numIterRadPanel.add(labelInfoNumIterRad, BorderLayout.LINE_END);
		// End Number of iteration of the entire algorithm_________________


		// Begin Weight Similarity_________________________________________
		JPanel weightSimilarityPanel = new JPanel();
		detectionPanel.add(weightSimilarityPanel);
		weightSimilarityPanel.setLayout(new GridLayout(4, 0, 0, 0));

		weightSimilarityPanel.add(
				addFieldDescription(
						"Select how important these aspects are from you point of view")
				);

		JPanel nocsPanel = new JPanel();
		weightSimilarityPanel.add(nocsPanel);

		nocsPanel.add(
				addFieldDescription(
						"Number of cloned statements: ")
				);
		JSlider sliderNocs = addSliderWithValue(nocsPanel, 0, 100, false);
		sliderNocs.setValue(33);
		
		sliderNocs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				config.weightNocs = sliderNocs.getValue() / 100.0;
			}
		});

		JLabel labelInfoNocs = this.getInfoLabel(
				"",
				"Info on num of cloned statements");
		nocsPanel.add(labelInfoNocs, BorderLayout.LINE_END);

		JPanel alcsPanel = new JPanel();
		weightSimilarityPanel.add(alcsPanel);

		alcsPanel.add(
				addFieldDescription(
						"Average length of the cloned statements: ")
				);
		JSlider sliderAlcs = addSliderWithValue(alcsPanel, 0, 100, false);
		sliderAlcs.setValue(33);
		
		sliderAlcs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				config.weightAlcs = sliderAlcs.getValue() / 100.0;
			}
		});

		JLabel labelInfoAlcs = this.getInfoLabel(
				"",
				"Info on abg length of the cloned statements");
		alcsPanel.add(labelInfoAlcs, BorderLayout.LINE_END);

		JPanel anocPanel = new JPanel();
		weightSimilarityPanel.add(anocPanel);

		anocPanel.add(
				addFieldDescription(
						"Average number of clones for each duplicated statement: ")
				);
		JSlider sliderAnoc = addSliderWithValue(anocPanel, 0, 100, false);
		sliderAnoc.setValue(33);
		
		sliderAnoc.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				config.weightAnoc = sliderAnoc.getValue() / 100.0;
			}
		});

		JLabel labelInfoAnoc = this.getInfoLabel(
				"",
				"Info on avg length of the cloned statements");
		anocPanel.add(labelInfoAnoc, BorderLayout.LINE_END);
		// End Weight Similarity___________________________________________


		// Begin Weight Refactoring Risk___________________________________
		JPanel weightRefRiskPanel = new JPanel();
		detectionPanel.add(weightRefRiskPanel);	
		weightRefRiskPanel.setLayout(new GridLayout(4, 0, 0, 0));

		weightRefRiskPanel.add(
				addFieldDescription(
						"Select how important these aspects are from you point of view")
				);

		JPanel varsimPanel = new JPanel();
		weightRefRiskPanel.add(varsimPanel);

		varsimPanel.add(
				addFieldDescription(
						"Variable Similarity: ")
				);
		JSlider sliderVarSim = addSliderWithValue(varsimPanel, 0, 100, false);
		sliderVarSim.setValue(50);
		
		sliderVarSim.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				config.weightVarSim = sliderVarSim.getValue() / 100.0;
			}
		});

		JLabel labelInfoVarSim = this.getInfoLabel(
				"",
				"Info on variable similarity");
		varsimPanel.add(labelInfoVarSim);

		JPanel codePosPanel = new JPanel();
		weightRefRiskPanel.add(codePosPanel);

		codePosPanel.add(
				addFieldDescription(
						"Code Position: ")
				);
		JSlider sliderCodePos = addSliderWithValue(codePosPanel, 0, 100, false);
		sliderCodePos.setValue(50);
		
		sliderCodePos.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				config.weightCodePos = sliderCodePos.getValue() / 100.0;
			}
		});

		JLabel labelInfoCodePos = this.getInfoLabel(
				"",
				"Info on code position");
		codePosPanel.add(labelInfoCodePos);

		// End Weight Refactoring Risk_____________________________________	



		// Begin Evolutionary Algorithm Selection__________________________
		// Put Single Objective as possible alternative
		JPanel evolAlgSelectionPanel = new JPanel();
		detectionPanel.add(evolAlgSelectionPanel);

		comboSingleObj = new JComboBox<String>();
		comboSingleObj.setEnabled(false);
		comboMultiObj = new JComboBox<String>();
		radioMultiObj = new JRadioButton();
		radioMultiObj.setSelected(true);
		radioSingleObj = new JRadioButton();

		evolAlgSelectionPanel.add(radioSingleObj);
		radioSingleObj.setText("Single Objective: ");

		radioSingleObj.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				comboMultiObj.setEnabled(false);
				radioMultiObj.setSelected(false);
				
				comboSingleObj.setEnabled(true);
				config.algorithmName = (String) comboSingleObj.getSelectedItem();
				config.typeOfOptimization = "Single";
			}
		});


		evolAlgSelectionPanel.add(comboSingleObj);

		evolAlgSelectionPanel.add(addFieldDescription("or"));

		evolAlgSelectionPanel.add(radioMultiObj);
		radioMultiObj.setText("Multi Objective: ");

		radioMultiObj.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				comboSingleObj.setEnabled(false);
				radioSingleObj.setSelected(false);
				
				comboMultiObj.setEnabled(true);
				config.algorithmName = (String) comboMultiObj.getSelectedItem();
				config.typeOfOptimization = "Multi";
			}
		});


		evolAlgSelectionPanel.add(comboMultiObj);


		try {
			for(List<String> algorithm: Utils.loadAlgorithmsConfigFile()) {
				String typeOfOptimization = algorithm.get(1);
				if("Single".equals(typeOfOptimization)) {
					comboSingleObj.addItem(algorithm.get(0));
				}
				if("Multi".equals(typeOfOptimization)) {
					comboMultiObj.addItem(algorithm.get(0));
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		comboMultiObj.setSelectedItem("NSGAII");
		
		comboMultiObj.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				config.algorithmName = (String) comboMultiObj.getSelectedItem();
			}
		});
		
		comboSingleObj.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				config.algorithmName = (String) comboSingleObj.getSelectedItem();
			}
		});

		JLabel labelInfoAlgorithm = this.getInfoLabel(
				"The ",
				"Info on optimization algorithm");
		evolAlgSelectionPanel.add(labelInfoAlgorithm);

		// End Evolutionary Algorithm Selection____________________________



		JScrollPane ss = new JScrollPane(detectionPanel);

		return ss;



	}

}

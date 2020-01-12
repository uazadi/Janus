package it.unimib.disco.essere.janus.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ConfigurationPreprocessingTab extends GUIClass {

	public ConfigurationPreprocessingTab(Configuration config) {
		super(config);
	}

	public JPanel getPanel() {
		JPanel preprocessingPanel = new JPanel();
		
		preprocessingPanel.setLayout(new GridLayout(6, 0, 0, 0));

		addEmptyPanel(preprocessingPanel);

		// Begin Test Case Row_____________________________________________
		JPanel testClassesPanel = new JPanel();
		preprocessingPanel.add(testClassesPanel);

		JCheckBox chckbxIncludeTestClasses = 
				new JCheckBox("Include Test Classes");
		testClassesPanel.add(chckbxIncludeTestClasses);

		chckbxIncludeTestClasses.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
					config.includeTest = true;
				else
					config.includeTest = false;
			}
		});

		JLabel labelInfoTestClasses = this.getInfoLabel(
				"Short explanation: include also the test cases in the code\n"
						+ "analysed, that by default are not included."
						+ "\n\n"
						+ "Long explanation: \n"
						+ "By enabling this setting you are going to include the test\n"
						+ "cases in the search for code clones, the duplication within\n"
						+ "this type of code is often very high, but it might be necessary\n"
						+ "or intentional, therefore in general is not advisable to include\n"
						+ "them. If you are experiencing any problem be aware that the\n"
						+ "safest way to let me know which classes contain test cases is\n"
						+ "to put them in a packaged that contains the string \"test\" in it.\n",
				"Info on test classes handling");
		testClassesPanel.add(labelInfoTestClasses, BorderLayout.LINE_END);
		// End Test Case Row_______________________________________________



		// Begin GUI Code Row_______________________________________________
		JPanel guiClassesPanel = new JPanel();
		preprocessingPanel.add(guiClassesPanel);

		JCheckBox chckbxIncludeGUIClasses = 
				new JCheckBox("Include GUI Classes");
		guiClassesPanel.add(chckbxIncludeGUIClasses);

		chckbxIncludeGUIClasses.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
					config.includeGui = true;
				else
					config.includeGui = false;
			}
		});

		JLabel labelInfoGUIClasses = this.getInfoLabel(
				"Short explanation: include also the code related "
						+ "to the GUI, that by default are not included.\n"
						+ "\n\n"
						+ "Long explanation: \n",
				"Info on GUI code handling");
		guiClassesPanel.add(labelInfoGUIClasses, BorderLayout.LINE_END);
		// End GUI Code Row_________________________________________________
		
		return preprocessingPanel;

	}

}

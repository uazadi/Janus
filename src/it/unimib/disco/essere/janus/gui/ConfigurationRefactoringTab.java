package it.unimib.disco.essere.janus.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ConfigurationRefactoringTab extends GUIClass {

	public ConfigurationRefactoringTab(Configuration config) {
		super(config);
	}

	public JPanel getPanel() {
		JPanel refactoringPanel = new JPanel();
		refactoringPanel.setLayout(new GridLayout(4, 0, 0, 0));

		addEmptyPanel(refactoringPanel);

		// Begin Same Class_______________________________________________
		JPanel sameClassPanel = new JPanel();
		refactoringPanel.add(sameClassPanel);

		JCheckBox chckbxSameClass = 
				new JCheckBox("Allows Same Class Refactoring");
		sameClassPanel.add(chckbxSameClass);

		chckbxSameClass.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
					config.sameClass = true;
				else
					config.sameClass = false;
			}
		});

		JLabel labelSameClass = this.getInfoLabel(
				"Short explanation: \n"
						+ "\n\n"
						+ "Long explanation: \n",
				"Info on GUI code handling");
		sameClassPanel.add(labelSameClass, BorderLayout.LINE_END);
		// End Same Class___________________________________________________

		// Begin Same Hierarchy Internal___________________________________
		JPanel sameHierIntPanel = new JPanel();
		refactoringPanel.add(sameHierIntPanel);

		JCheckBox chckbxSameHierInt = 
				new JCheckBox("Allows Same Hierarchy Internal Refactoring");
		sameHierIntPanel.add(chckbxSameHierInt);

		chckbxSameHierInt.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
					config.sameHierInt = true;
				else
					config.sameHierInt = false;
			}
		});

		JLabel labelSameHierInt = this.getInfoLabel(
				"Short explanation: \n"
						+ "\n\n"
						+ "Long explanation: \n",
				"Info on GUI code handling");
		sameHierIntPanel.add(labelSameHierInt, BorderLayout.LINE_END);
		// End Same Hierarchy Internal______________________________________

		// Begin Same Hierarchy Internal___________________________________
		JPanel sameHierExtPanel = new JPanel();
		refactoringPanel.add(sameHierExtPanel);

		JCheckBox chckbxSameHierExt = 
				new JCheckBox("Allows Same Hierarchy External Refactoring");
		sameHierExtPanel.add(chckbxSameHierExt);

		chckbxSameHierExt.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
					config.sameHierExt = true;
				else
					config.sameHierExt = false;
			}
		});

		JLabel labelSameHierExt = this.getInfoLabel(
				"Short explanation: \n"
						+ "\n\n"
						+ "Long explanation: \n",
				"Info on GUI code handling");
		sameHierExtPanel.add(labelSameHierExt, BorderLayout.LINE_END);
		// End Same Hierarchy Internal______________________________________			

		return refactoringPanel;
		
	}


}

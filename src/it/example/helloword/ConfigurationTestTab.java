package it.example.helloword;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;

import it.unimib.disco.essere.deduplicator.behaviouralcheck.JUnitCheck;

public class ConfigurationTestTab extends GUIClass{

	public ConfigurationTestTab(Configuration config) {
		super(config);
	}

	public JPanel getPanel() throws CoreException {
		JPanel testPanel = new JPanel();

		testPanel.setLayout(new GridLayout(2, 0, 0, 0));

		JPanel mainClassesPanel = new JPanel();
		testPanel.add(mainClassesPanel);
		//mainClassesPanel.setLayout(new BorderLayout(0, 0));

		mainClassesPanel.add(
				addFieldDescription("Select which Main class you want to use:"), 
				BorderLayout.NORTH);

		JPanel mainListPanel = new JPanel();
		mainClassesPanel.add(mainListPanel, BorderLayout.CENTER);

		for(String mainClass: config.mainClasses) {

			JCheckBox chckbxTestCase = new JCheckBox(mainClass);
			chckbxTestCase.setSelected(true);
			mainClassesPanel.add(chckbxTestCase);

			chckbxTestCase.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED)
						config.mainClasses.add(mainClass);
					else
						config.mainClasses.remove(mainClass);
				}
			});
		}

		JPanel junitPanel = new JPanel();
		testPanel.add(junitPanel);
		junitPanel.setLayout(new BorderLayout(0, 0));

		junitPanel.add(
				addFieldDescription("Select which JUnit test classes you want to use:"), 
				BorderLayout.NORTH);

		JPanel junitListPanel = new JPanel();
		junitPanel.add(junitListPanel, BorderLayout.CENTER);

		JUnitCheck junitCheck = new JUnitCheck(config.selectedProject);

		config.junitClasses = junitCheck.findJunitClasses();

		for(IType testCase: config.junitClasses.keySet()) {
			JCheckBox chckbxTestCase = new JCheckBox(testCase.getElementName());
			chckbxTestCase.setSelected(true);
			junitListPanel.add(chckbxTestCase);

			chckbxTestCase.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						for(IType junitClass: config.junitClasses.keySet()) {
							if(junitClass.getElementName().equals(testCase.getElementName())) {
								config.junitClasses.replace(junitClass, true);
								break;
							}
						}
					}
					else {
						for(IType junitClass: config.junitClasses.keySet()) {
							if(junitClass.getElementName().equals(testCase.getElementName())) {
								config.junitClasses.replace(junitClass, false);
								break;
							}
						}
					}
				}
			});

//			chckbxTestCase.addActionListener(new ActionListener() {
//
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					for(IType junitClass: config.junitClasses.keySet()) {
//						if(junitClass.getElementName().equals(testCase.getElementName())) {
//							config.junitClasses.replace(junitClass, false);
//							break;
//						}
//					}
//				}
//			});
		}

		return testPanel;
	}

}

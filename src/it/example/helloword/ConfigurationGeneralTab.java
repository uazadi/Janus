package it.example.helloword;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ConfigurationGeneralTab extends GUIClass{

	public ConfigurationGeneralTab(Configuration config) {
		super(config);
	}

	public JPanel getPanel() {
		JPanel generalPanel = new JPanel();

		generalPanel.setLayout(new GridLayout(4, 0, 0, 0));

		this.addEmptyPanel(generalPanel);
		
		// Begin Number of iteration of the entire algorithm________________
		JPanel numIterAlgPanel = new JPanel();
		generalPanel.add(numIterAlgPanel);

		numIterAlgPanel.add(
				addFieldDescription(
						"How many code clones you want me to refactor?"
						)
				);

		JSlider sliderIterAlg = addSliderWithValue(numIterAlgPanel, 0, 100, true);
		sliderIterAlg.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				config.iterationAlg = sliderIterAlg.getValue();
			}
		});

		JLabel labelInfoNumIterAlg = this.getInfoLabel(
				"This field allows you to select how many refactoring steps\n"
						+ "will be accomplished. This value roughly correspond to\n"
						+ "the number of code clones to be refactored, however is\n"
						+ "not exactly that value since in one step more then one\n"
						+ "code clones can be identified and refactored."
						+ "\n\n"
						+ "!!! 0 = \"go on until there are code clones that can be refactored\" !!!",
				"Info on Iteration of entire algorithm");
		numIterAlgPanel.add(labelInfoNumIterAlg, BorderLayout.LINE_END);
		// End Number of iteration of the entire algorithm_________________

		// Begin Git Repo Row_______________________________________________
		JPanel gitPanel = new JPanel();
		generalPanel.add(gitPanel);

		gitPanel.add(
				addFieldDescription(
						"Select the git repository:"
						)
				);


		final JFileChooser fileChooserPanel = new JFileChooser();
		fileChooserPanel.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		JTextField gitRepoPath = new  JTextField();
		gitRepoPath.setColumns(30);
		gitPanel.add(gitRepoPath);
		
		gitRepoPath.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateConfig();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateConfig();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateConfig();
			}
			
			public void updateConfig() {
				config.gitRepo = gitRepoPath.getText();
			}
		});
		
		
		

		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int returnVal = fileChooserPanel.showOpenDialog(new JPanel());

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooserPanel.getSelectedFile();
					gitRepoPath.setText(file.getAbsolutePath());
				} 
			}
		});
		gitPanel.add(btnBrowse);

		JLabel labelInfoGui = this.getInfoLabel(
				"Short explanation: \n"
						+ "\n\n"
						+ "Long explanation: \n",
				"Info on versioning handling");
		gitPanel.add(labelInfoGui, BorderLayout.LINE_END);
		// End Git Repo Row_________________________________________________

		// Begin Detect Not Exact Match____________________________________
		JPanel detectNotExactMatchPanel = new JPanel();
		generalPanel.add(detectNotExactMatchPanel);

		JCheckBox chckbxSuggestNotExactMatch = 
				new JCheckBox("Suggest refactoring opportunity fir not exact match clones");
		detectNotExactMatchPanel.add(chckbxSuggestNotExactMatch);
		
		chckbxSuggestNotExactMatch.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
					config.suggestNotExactMatch = true;
				else
					config.suggestNotExactMatch = false;
			}
		});

		JLabel labelSameHierExt = this.getInfoLabel(
				"Short explanation: \n"
						+ "\n\n"
						+ "Long explanation: \n",
				"Info on not exact match suggestion");
		detectNotExactMatchPanel.add(labelSameHierExt, BorderLayout.LINE_END);
		// End Detect Not Exact Match______________________________________

		return generalPanel;
	}
}
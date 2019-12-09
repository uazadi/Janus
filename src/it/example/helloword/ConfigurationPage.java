package it.example.helloword;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import java.awt.ScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.GridLayout;
import javax.swing.JRadioButton;

public class ConfigurationPage {

	private JFrame frame;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConfigurationPage window = new ConfigurationPage();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ConfigurationPage() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Configuration");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane);
		
		// Begin Pre-processing tab---------------------------------------------
		JPanel preprocessingPanel = new JPanel();
		tabbedPane.addTab("Preprocessing", null, preprocessingPanel, null);
		preprocessingPanel.setLayout(new GridLayout(0, 5, 0, 0));
		
		JRadioButton rdbtnIncludeTestCases = new JRadioButton("Include Test Cases");
		preprocessingPanel.add(rdbtnIncludeTestCases);
		
		// End Pre-processing tab-----------------------------------------------
		
		
		
		// Begin Detection tab--------------------------------------------------
		JPanel detectionPanel = new JPanel();
		tabbedPane.addTab("Detection", null, detectionPanel, null);
		
		// End Detection tab----------------------------------------------------
		
		
		// Begin Refactoring tab------------------------------------------------
		JPanel refactoringPanel = new JPanel();
		tabbedPane.addTab("Refactoring", null, refactoringPanel, null);
		// End Refactoring tab--------------------------------------------------
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JButton btnNewButton = new JButton("Back");
		panel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Start refactoring");
		panel.add(btnNewButton_1);
		
		

	}

}

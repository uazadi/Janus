package it.unimib.disco.essere.janus.gui;

import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;

import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;

import it.unimib.disco.essere.janus.behaviouralcheck.JUnitCheck;
import it.unimib.disco.essere.janus.versioning.VersionerException;

import java.awt.GridLayout;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.JComboBox;
import javax.swing.JSeparator;

public class ConfigurationPage extends GUIClass{

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConfigurationPage window = new ConfigurationPage(new Configuration());
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws CoreException 
	 */
	public ConfigurationPage(Configuration config) throws CoreException {
		super(config);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws CoreException 
	 */
	private void initialize() throws CoreException {
		frame = new JFrame();
		frame.setTitle("Configuration");
		frame.setBounds(100, 100, 700, 500);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane);


		ConfigurationGeneralTab general = new ConfigurationGeneralTab(this.config);
		tabbedPane.addTab("General", null,  general.getPanel(), null);

		ConfigurationPreprocessingTab preprocessing = new ConfigurationPreprocessingTab(this.config);
		tabbedPane.addTab("Preprocessing", null, preprocessing.getPanel(), null);

		ConfigurationDetectionTab detection = new ConfigurationDetectionTab(this.config);
		tabbedPane.addTab("Detection", null, detection.getPanel(), null);

		ConfigurationRefactoringTab refactoring = new ConfigurationRefactoringTab(config);
		tabbedPane.addTab("Refactoring", null, refactoring.getPanel(), null);

		ConfigurationTestTab test = new ConfigurationTestTab(this.config);
		tabbedPane.addTab("Test", null, test.getPanel(), null);

		addFootPage();

	}

	private void addFootPage() {
		// Foot page with buttons
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(0, 2, 0, 0));

		JButton btnBack = new JButton("Back");
		panel.add(btnBack);

		btnBack.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				frame.dispose();
				
				HomePage window = new HomePage(config);
				window.launch();
			}
		});


		JButton btnStart = new JButton("Start refactoring");
		panel.add(btnStart);

		if(config.runStepByStep) {
			btnStart.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {}

				@Override
				public void mousePressed(MouseEvent e) {}

				@Override
				public void mouseExited(MouseEvent e) {}

				@Override
				public void mouseEntered(MouseEvent e) {}

				@Override
				public void mouseClicked(MouseEvent e) {
					WorkflowHandler handler = WorkflowHandler.getInstance();
					handler.setConfig(config);
					
					try {
						handler.initGitRepo();
						List<List<ASTNode>> stmts = handler.selectClones();
						
						frame.dispose();
						
						RefactoringStepPage window = new RefactoringStepPage(stmts, config);
						window.launch();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
		}else {
			btnStart.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {}

				@Override
				public void mousePressed(MouseEvent e) {}

				@Override
				public void mouseExited(MouseEvent e) {}

				@Override
				public void mouseEntered(MouseEvent e) {}

				@Override
				public void mouseClicked(MouseEvent e) {
					frame.dispose();
					
					AutonomusRefactoring window = new AutonomusRefactoring(config);
					window.frame.pack();
					window.launch();
				}
			});
		}
		
		
	}
}

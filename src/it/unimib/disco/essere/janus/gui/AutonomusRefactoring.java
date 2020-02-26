package it.unimib.disco.essere.janus.gui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import javax.swing.SwingConstants;

public class AutonomusRefactoring extends GUIClass
implements ActionListener, 
PropertyChangeListener {


	private JProgressBar progressBar;
	private JButton startButton;
	private JTextArea taskOutput;
	private Task task;

	class Task extends SwingWorker<Void, Void> {
		/*
		 * Main task. Executed in background thread.
		 */
		@Override
		public Void doInBackground() {
			int progress = 0;

			for(int i=0; i < config.iterationAlg; i++) {

				WorkflowHandler handler = WorkflowHandler.getInstance();
				handler.setConfig(config);

				try {
					
					handler.initGitRepo();
					
					int countForConvergence = 0;
					List<List<ASTNode>> stmts = handler.selectClones();

					while(stmts.size() == 0) {
						countForConvergence++;
						stmts = handler.selectClones();
						if(countForConvergence == config.attemptBeforeConvergence) {
							setProgress(config.iterationAlg);
							taskOutput.append("[WARNING] No more clone to be refactored!");
							return null;
						}
					}

					taskOutput.append("[INFO] Step " + i + ", Statement involved involved: \n" );
					for(List<ASTNode> clonesInstance: stmts) {
						for(ASTNode stmt: clonesInstance) {
							int methodStartingPosition = 
									((CompilationUnit) stmt.getParent().getParent().getParent().getParent())
									.getLineNumber(((MethodDeclaration) stmt.getParent().getParent()).getStartPosition());

							int stmtStartingPosition = 
									((CompilationUnit) stmt.getParent().getParent().getParent().getParent())
									.getLineNumber(((Statement) stmt).getStartPosition());

							String fullStmt = "Class: " + ((TypeDeclaration) stmt.getParent().getParent().getParent()).getName()
									+ ", Method: " + ((MethodDeclaration) stmt.getParent().getParent()).getName()
									+ " at line " + methodStartingPosition 
									+ ", Statement at line " + stmtStartingPosition;

							taskOutput.append(fullStmt + "\n");
						}
					}

					handler.accomplishRefactoring();

					handler.saveChanges();

					handler.commitChanges();

					try {
						if(!handler.runTests(config))
							handler.rollbackChanges();
					} catch(Exception e2) {
						handler.rollbackChanges();
					}

					setProgress(Math.min(i+1, config.iterationAlg));

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			return null;
		}

		/*
		 * Executed in event dispatch thread
		 */
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			startButton.setEnabled(true);
			taskOutput.append("Done!\n");
		}
	}

	public AutonomusRefactoring(Configuration config) {
		super(config);

		frame = new JFrame("Automatic Refactoring");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);


		startButton = new JButton("Start refactoring");
		startButton.setActionCommand("start");
		startButton.addActionListener(this);

		progressBar = new JProgressBar(0, config.iterationAlg);
		progressBar.setValue(0);

		//Call setStringPainted now so that the progress bar height
		//stays the same whether or not the string is shown.
		progressBar.setStringPainted(true); 

		taskOutput = new JTextArea(10, 75);
		taskOutput.setMargin(new Insets(5,5,5,5));
		taskOutput.setEditable(false);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel southPanel = new JPanel();
		
		JButton stopButton = new JButton("Stop refactoring");
		
		southPanel.add(startButton);
		southPanel.add(stopButton);
		

		panel.add(progressBar, BorderLayout.NORTH);
		panel.add(southPanel, BorderLayout.SOUTH);
		panel.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	}

	/**
	 * Invoked when the user presses the start button.
	 */
	public void actionPerformed(ActionEvent evt) {
		task = new Task();
		task.addPropertyChangeListener(this);
		task.execute();
	}


	/**
	 * Invoked when task's progress property changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setIndeterminate(false);
			progressBar.setValue(progress);
		}
	}


	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
		//            public void run() {
		//                createAndShowGUI();
		//            }
		//        });

		AutonomusRefactoring a = new AutonomusRefactoring(new Configuration());
		a.frame.pack();
		a.frame.setVisible(true);
	}
}



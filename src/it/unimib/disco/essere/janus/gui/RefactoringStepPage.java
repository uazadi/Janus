package it.unimib.disco.essere.janus.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;

import javax.swing.Box;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import it.unimib.disco.essere.janus.versioning.VersionerException;

import javax.swing.JTextPane;

public class RefactoringStepPage extends GUIClass{

	/**
	 * Create the application.
	 */
	public RefactoringStepPage(List<List<ASTNode>> stmts, Configuration config) {
		super(config);
		initialize(stmts);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(List<List<ASTNode>> stmts) {
		frame = new JFrame();
		
		System.out.println("____________________________________________"  + frame);
		
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		
		frame.getContentPane().add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel menuPanel = new JPanel();
		panel.add(menuPanel, BorderLayout.NORTH);
		
		JMenuBar menuBar = new JMenuBar();
		menuPanel.add(menuBar);
		
		JMenu actions = new JMenu("Action");
		menuBar.add(actions);
		
		JMenuItem menuItem1 = new JMenuItem("Stop refactoring");
		actions.add(menuItem1);
		
		JMenu properties = new JMenu("Properties");
		menuBar.add(properties);
		
		JMenuItem menuItem2 = new JMenuItem("Configuration");
		properties.add(menuItem2);
		
		menuBar.add(Box.createHorizontalGlue());
		
		JPanel cloneList = new JPanel();
		
		int count = 1;
		for(List<ASTNode> clonesInstance: stmts) {
			for(ASTNode stmt: clonesInstance) {
				count += 1; 
			}
		}
		
		cloneList.setLayout(new GridLayout(count, 0, 0, 0));
		
		for(List<ASTNode> clonesInstance: stmts) {
			for(ASTNode stmt: clonesInstance) {
				
				int methodStartingPosition = 
						((CompilationUnit) stmt.getParent().getParent().getParent().getParent())
							.getLineNumber(((MethodDeclaration) stmt.getParent().getParent()).getStartPosition());
				
				int stmtStartingPosition = 
						((CompilationUnit) stmt.getParent().getParent().getParent().getParent())
							.getLineNumber(((Statement) stmt).getStartPosition());
				
				String fullStmt = "Class: " + ((TypeDeclaration) stmt.getParent().getParent().getParent()).getName()
						+ "\nMethod: " + ((MethodDeclaration) stmt.getParent().getParent()).getName()
						+  "  (line " + methodStartingPosition + ")"
						+ "\nStatement (line " + stmtStartingPosition + "):\n " + stmt.toString();
				
				JTextArea clone = new JTextArea();
				clone.setEditable(false);
				clone.setText(fullStmt);
				cloneList.add(clone);
			}
		}
		

		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new GridLayout(0, 2, 0, 0));
		
		JButton btnRollback = new JButton("Skip");
		panel_1.add(btnRollback);
		
		JButton btnPerform = new JButton("Refactor");
		panel_1.add(btnPerform);
		
		btnPerform.addMouseListener(new MouseListener() {

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
					handler.accomplishRefactoring();

					handler.saveChanges();
						
					handler.commitChanges();
					
					try {
						if(!handler.runTests(config))
							handler.rollbackChanges();
					} catch(Exception e2) {
						handler.rollbackChanges();
					}
					
					List<List<ASTNode>> newStmts = handler.selectClones();
					
					frame.dispose();
					
					RefactoringStepPage window = new RefactoringStepPage(newStmts, config);
					window.launch();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(cloneList);
		panel.add(scrollPane, BorderLayout.CENTER);
	}

}

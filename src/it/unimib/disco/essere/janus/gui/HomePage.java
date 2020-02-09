package it.unimib.disco.essere.janus.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.preferences.formatter.ModifyDialog.Images;

import java.awt.BorderLayout;
import java.awt.Window.Type;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class HomePage extends GUIClass {

	/**
	 * Create the application.
	 */
	public HomePage(Configuration config) {
		super(config);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.WHITE);
		frame.setBackground(Color.WHITE);
		frame.setFont(new Font("Lato", Font.PLAIN, 12));
		frame.setAlwaysOnTop(true);
		frame.setBounds(100, 100, 700, 500);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(3, 0, 20, 20));
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);


		// Begin construct first row -----------------------------------------
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel picLabel = new JLabel(new ImageIcon("/home/uazadi/Documents/Tesi/Janus/icons/homepage.png"));	
		panel.add(picLabel);

//		JTextPane txtpnHelloMyName = new JTextPane();
//		panel.add(txtpnHelloMyName);
//		txtpnHelloMyName.setBackground(Color.LIGHT_GRAY);
//		txtpnHelloMyName.setFont(new Font("Abyssinica SIL", Font.PLAIN, 15));
//		txtpnHelloMyName.setText("Hello, my name is Janus \n I am here to help you get rid of your code clones!");

//		StyledDocument doc = txtpnHelloMyName.getStyledDocument();
//		doc.setParagraphAttributes(0, doc.getLength(), center, false);

		// End construct first row -----------------------------------------


		// Begin construct second row -----------------------------------------
		JPanel panel_second_row = new JPanel();
		panel_second_row.setBackground(Color.WHITE);
		frame.getContentPane().add(panel_second_row);
		panel_second_row.setLayout(new GridLayout(0, 3, 0, 0));


		JPanel panel_2_1 = new JPanel();
		panel_second_row.add(panel_2_1);
		panel_2_1.setBackground(Color.WHITE);
		JButton btnStepByStep = new JButton("Refactoring \n step-by-step");
		panel_second_row.add(btnStepByStep);
		
		btnStepByStep.addMouseListener(new MouseListener() {

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
				config.runStepByStep = true;
				
				frame.dispose();
				
				try {
					ConfigurationPage window = new ConfigurationPage(config);
					window.launch();
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});


		JPanel panel_2_3 = new JPanel();
		panel_second_row.add(panel_2_3);
		panel_2_3.setBackground(Color.WHITE);
		panel_2_3.setLayout(new GridLayout(3, 0, 0, 0));

		JPanel panel_2_3_1 = new JPanel();
		panel_2_3_1.setBackground(Color.WHITE);
		panel_2_3.add(panel_2_3_1);

		JPanel panel_2_3_2 = new JPanel();
		panel_2_3.add(panel_2_3_2);
		panel_2_3_2.setBackground(Color.WHITE);
		panel_2_3_2.setLayout(new GridLayout(0, 3, 0, 0));

		JPanel panel_2_3_2_1 = new JPanel();
		panel_2_3_2_1.setBackground(Color.WHITE);
		panel_2_3_2.add(panel_2_3_2_1);
		
		JLabel labelInfoStepByStep = this.getInfoLabel(
				"Short explaination:\n"
						+ "Use this type of execution  if you don't yet trust\n"
						+ "me or if you are not familiar on how to set me."
						+ "\n\n" 
						+ "Long explaination:\n"
						+ "This  type  of  execution  will  allow you to check\n"
						+ "every  step  of  the  refactoring  and at each step\n"
						+ "choose  to  keep  or  discard  the  changes applied\n"
						+ "automatically accomplished by me.\n"
						+ "Furthermore  when  I  will do something that you\n"
						+ "don't  like, I will  ask you the reason in order to\n"
						+ "improve and better understand your needs.", 
				"Info on step-by-step execution");
		panel_2_3_2.add(labelInfoStepByStep, BorderLayout.EAST);
		// End construct second row -----------------------------------------

		// Begin construct third row -----------------------------------------
		JPanel panel_third_row = new JPanel();
		frame.getContentPane().add(panel_third_row);
		panel_third_row
		.setBackground(Color.WHITE);
		panel_third_row.setLayout(new GridLayout(0, 3, 0, 0));


		JPanel panel_3_1 = new JPanel();
		panel_third_row.add(panel_3_1);

		JButton btnAutonomus = new JButton("Autonomus \n Refactoring");
		panel_third_row.add(btnAutonomus);
		
		btnAutonomus.addMouseListener(new MouseListener() {

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
				config.runStepByStep = false;
				
				frame.dispose();
				
				try {
					ConfigurationPage window = new ConfigurationPage(config);
					window.launch();
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		

		JPanel panel_3_3 = new JPanel();
		panel_third_row.add(panel_3_3);
		panel_3_3.setLayout(new GridLayout(3, 0, 0, 0));

		JPanel panel_3_3_1 = new JPanel();
		panel_3_3.add(panel_3_3_1);

		JPanel panel_3_3_2 = new JPanel();
		panel_3_3.add(panel_3_3_2);
		panel_3_3_2.setLayout(new GridLayout(0, 3, 0, 0));

		JPanel panel_3_3_2_1 = new JPanel();
		panel_3_3_2.add(panel_3_3_2_1);
		
		JLabel labelInfoAutonomus = this.getInfoLabel(
				"Short explaination:\n"
						+ "Use this type of execution if you already trust\n"
						+ "me and if you are already familiar on how to set me."
						+ "\n\n" 
						+ "Long explaination:\n"
						+ "This type of execution is fully automated meaning that\n"
						+ "it will apply all the refactoring step automatically\n"
						+ "without  giving  you  the  possibility to review each\n"
						+ "change. However you will still be able to review and\n"
						+ "rollback all the changes beacuse I will make sure that\n"
						+ "the folder is a Git repository and I will commit every\n"
						+ "change that I am going to make.",
				"Info on autonomus execution");
		panel_3_3_2.add(labelInfoAutonomus);
		// End construct third row -----------------------------------------

	}
	
	/**
	 * Just for test
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					HomePage window = new HomePage(null);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}

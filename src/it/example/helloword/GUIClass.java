package it.example.helloword;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GUIClass {

	protected JFrame frame;

	public void open() {
		this.frame.setVisible(true);
	}
	
	public void launch(){
		
		GUIClass page = this; 
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					page.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void showInfo(String message, String title) {
		JOptionPane.showMessageDialog(
				null, 
				message, 
				title, 
				JOptionPane.INFORMATION_MESSAGE);
	}


}

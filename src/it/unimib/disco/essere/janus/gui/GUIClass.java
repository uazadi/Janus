package it.unimib.disco.essere.janus.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GUIClass {

	protected JFrame frame;

	protected Configuration config;
	
	public GUIClass(Configuration config) {
		this.config = config;
	}
	
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

	private void showInfo(String message, String title) {
		JOptionPane.showMessageDialog(
				null, 
				message, 
				title, 
				JOptionPane.INFORMATION_MESSAGE);
	}

	protected JLabel getInfoLabel(String message, String title) {

		JLabel label = new JLabel(new ImageIcon("/home/umberto/Desktop/Tesi/eclipse_workspace/Janus/icons/info24x24.png"));

		label.addMouseListener(new MouseListener() {
			public void popupInfo() {
				showInfo( message, title);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				popupInfo();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				popupInfo();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		return label;

	}

	protected JTextField addFieldDescription( String message) {
		JTextField txtField = new JTextField();
		txtField.setFont(new Font("Dialog", Font.BOLD, 12));
		txtField.setEditable(false);
		txtField.setBorder(null);
		txtField.setText(message);

		return txtField;
	}

	protected JSlider addSliderWithValue(JPanel panel, int min, int max, boolean addAll) {
		JTextField txtSliderValue = new JTextField();
		txtSliderValue.setBorder(null);
		txtSliderValue.setColumns(3);
		txtSliderValue.setEditable(false);
		panel.add(txtSliderValue);

		
		final int updatedMax = addAll ? (max + 1) : max;
		
		JSlider slider = new JSlider(min, updatedMax);
		slider.setSnapToTicks(true);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		panel.add(slider);

		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				txtSliderValue.setText(Integer.toString(slider.getValue()));
				
				if(addAll && slider.getValue() >= updatedMax)
					txtSliderValue.setText("All"); // 0 = keep going until there are clone detected
			}
		});

		return slider;
	}

	protected void addEmptyPanel(JPanel panel) {
		JPanel emptyPanel = new JPanel();
		panel.add(emptyPanel);
	}

}

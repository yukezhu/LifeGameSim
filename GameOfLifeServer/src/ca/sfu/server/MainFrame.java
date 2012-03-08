package ca.sfu.server;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	JPanel infoPanel;
	JPanel clientPanel;
	AutomataPanel automataPanel;

	public MainFrame()
	{
		super();
		infoPanel.setBackground(Color.RED);
		clientPanel.setBackground(Color.GREEN);
		automataPanel.setBackground(Color.GRAY);
		this.setContentPane(automataPanel);
	}
	
	public static void main(String[] args) {
		
		
	}

}

package ca.sfu.client;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	public MainFrame()
	{
		GridLayout layout = new GridLayout(3, 3);
		setLayout(layout);
		
		this.add(new JLabel("Client Ip: "));
		
		setSize(500, 500);
		setVisible(true);
		setTitle("Client");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public static void main(String[] argv)
	{
		@SuppressWarnings("unused")
		MainFrame frame = new MainFrame();
		
	}
	
}

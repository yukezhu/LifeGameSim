package ca.sfu.server;

import javax.swing.JFrame;

public class ControlPanel extends JFrame {

	public ControlPanel()
	{
		setSize(200, 200);
	}
	
	public static void main(String args[])
	{
		ControlPanel control = new ControlPanel();
		control.setVisible(true);
	}
	
}

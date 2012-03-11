package ca.sfu.server;

import java.awt.Graphics;

import javax.swing.JFrame;

public class ControlPanel extends JFrame {

	private static final long serialVersionUID = 1L;

	public ControlPanel()
	{
		setSize(300, 300);
		setTitle("Control Panel");
	}
	
	@Override
	public void paint(Graphics g) {
		
	}
	
	public static void main(String args[])
	{
		ControlPanel control = new ControlPanel();
		control.setVisible(true);
	}
	
}

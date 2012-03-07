package ca.sfu.server;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import ca.sfu.message.AutomataMsg;

public class AutomataPanel extends JPanel  {
	
	private static final long serialVersionUID = 1L;

	AutomataMsg auto = null;
	
	public AutomataPanel()
	{
		this.setBackground(Color.WHITE);
	}

	public void setAutomata(AutomataMsg automata)
	{
		auto = automata;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(auto != null)
		{
			int width = auto.getWidth();
			int height = auto.getHeight();
			int[][] bitmap = auto.getBitmap();
			for(int i=0; i<height; i++)
				for(int j=0; j<width; j++)
				{
					if(bitmap[i][j] == 1)
					{
						g.setColor(Color.BLACK);
						g.fillRect(10 * j, 10 * i, 10, 10);
					}
				}
		}
	}
}

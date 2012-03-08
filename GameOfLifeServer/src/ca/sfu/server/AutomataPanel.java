package ca.sfu.server;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import ca.sfu.cmpt431.facility.Board;

public class AutomataPanel extends JPanel  {
	
	private static final long serialVersionUID = 1L;
	private int cellSize; 
	
	Board b = null;
	
	public AutomataPanel()
	{
		this.setBackground(Color.WHITE);
		cellSize = 10;
	}

	public void setBoard(Board board)
	{
		b = board;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(b != null)
		{
			int width = b.width;
			int height = b.height;
			boolean[][] bitmap = b.bitmap;
			for(int i=0; i<height; i++)
				for(int j=0; j<width; j++)
				{
					if(bitmap[i][j])
					{
						g.setColor(Color.BLACK);
						g.fillRect(cellSize * j, cellSize * i, cellSize, cellSize);
					}
				}
		}
	}
}

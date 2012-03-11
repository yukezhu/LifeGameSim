package ca.sfu.server;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Stack;

import javax.swing.JPanel;

import ca.sfu.cmpt431.facility.Board;

public class AutomataPanel extends JPanel  {
	
	private static final long serialVersionUID = 1L;
	private int cellSize, cellScale; 
	
	private Board b = null;
	private boolean zoomIn, zoomOut;
	
	private Stack<Integer> startX, startY;
	
	private int alive, cell, cycle;
	
	public AutomataPanel()
	{
		setBackground(Color.WHITE);
		cellSize = 1;
		cellScale = 1;
		zoomIn = false;
		zoomOut = false;
		alive = 0;
		cell = 0;
		cycle = 0;
		
		startX = new Stack<Integer>();
		startY = new Stack<Integer>();
		startX.add(0);
		startY.add(0);
		
		addMouseListener(new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int mx = arg0.getY(), my = arg0.getX();
				int sX = startX.peek(), sY = startY.peek();
				if(zoomIn && cellScale <= 16)
				{
					cellScale *= 2;
				} else if(zoomOut && cellScale > 1)
				{
					cellScale /= 2;
				} else return;
				if(zoomIn)
				{
					if(mx < b.height / 2 && my < b.width / 2)
					{
						startX.push(sX);
						startY.push(sY);
					} else if(mx < b.height / 2 && my >= b.width / 2)
					{
						startX.push(sX);
						startY.push(sY + b.width / cellScale);
					} else if(mx >= b.height / 2 && my < b.width / 2)
					{
						startX.push(sX + b.height / cellScale);
						startY.push(sY);
					} else
					{
						startX.push(sX + b.height / cellScale);
						startY.push(sY + b.width / cellScale);
					}
					repaint();
				}
				if(zoomOut)
				{
					startX.pop();
					startY.pop();
					repaint();
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
		});
	}
	
	public void setZoomIn()
	{
		zoomIn = true;
		zoomOut = false;
	}
	
	public void setZoomOut()
	{
		zoomIn = false;
		zoomOut = true;
	}
	
	public void setNormal()
	{
		zoomIn = false;
		zoomOut = false;
	}
	
	public boolean getZoomIn()
	{
		return zoomIn;
	}
	
	public boolean getZoomOut()
	{
		return zoomOut;
	}
	
	
	public int getCellSize()
	{
		return cellSize;
	}
	
	public void setCellSize(int size)
	{
		cellSize = size;
	}
	
	public void setBoard(Board board)
	{
		b = board;
		setSize(b.width * cellSize, b.height * cellSize);
	}
	
	public int getAlive() {
		return alive;
	}

	public int getCell() {
		return cell;
	}
	
	public int getCycle() {
		return cycle;
	}
	
	public Board getBoard()
	{
		return b;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(b != null)
		{	
			int width = b.width / cellScale;
			int height = b.height / cellScale;
			boolean[][] bitmap = b.bitmap;
			int sX = startX.peek(), sY = startY.peek();
			int count = 0;
			for(int i=0; i<height; i++)
				for(int j=0; j<width; j++)
				{
					if(bitmap[sX + i][sY + j])
					{
						int size = cellSize * cellScale;
						g.setColor(Color.BLACK);
						g.fillRect(size * j, size * i, size, size);
						count ++;
					}
				}
			alive = count;
			cell = b.width * b.height;
			cycle ++;
		}
	}

}
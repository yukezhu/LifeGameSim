package ca.sfu.server;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.sfu.cmpt431.facility.Board;
import ca.sfu.cmpt431.facility.BoardOperation;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	JPanel infoPanel;
	JPanel clientPanel;
	AutomataPanel automataPanel;

	public MainFrame()
	{
		super();
		this.setSize(480, 480);
		
		Board board = new Board(1000, 1000);
		BoardOperation.Randomize(board, 0.3);
		AutomataPanel automataPanel = new AutomataPanel();
		automataPanel.setCellSize(2);
		automataPanel.setBoard(board);
		
		infoPanel = new JPanel();
		clientPanel = new JPanel();
		
		infoPanel.setBackground(Color.RED);
		clientPanel.setBackground(Color.GREEN);
		automataPanel.setBackground(Color.GRAY);
		setContentPane(automataPanel);
		this.setVisible(true);
		
		addWindowListener(windowAdapter);
	}
	
	public static void main(String[] args) {
		
		MainFrame frame = new MainFrame();
		
	}
	
	WindowAdapter windowAdapter = new WindowAdapter() {
		public void windowClosing(WindowEvent e)
		{
			// Store data if necessary
			System.exit(0);
		}
	};

}

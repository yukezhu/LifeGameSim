package ca.sfu.server;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import ca.sfu.cmpt431.facility.Board;
import ca.sfu.cmpt431.facility.BoardOperation;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	JPanel infoPanel;
	JPanel clientPanel;
	AutomataPanel automataPanel;

	Board board;
	
	public MainFrame()
	{
		super();
		this.setSize(800, 800);
		
		board = new Board(800, 800);
		BoardOperation.Randomize(board, 0.2);
		final AutomataPanel automataPanel = new AutomataPanel();
		automataPanel.setCellSize(1);
		automataPanel.setBoard(board);
		
		infoPanel = new JPanel();
		clientPanel = new JPanel();
		
		infoPanel.setBackground(Color.RED);
		clientPanel.setBackground(Color.GREEN);
		automataPanel.setBackground(Color.GRAY);
		setContentPane(automataPanel);
		setVisible(true);
				
		Timer timer = new Timer(0, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0) {
				automataPanel.setBoard(board);
				BoardOperation.NextMoment(board, null, null, null, null, false, false, false, false);;
				automataPanel.repaint();
			}
		});
		
		timer.setDelay(200);
		timer.start();
		
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

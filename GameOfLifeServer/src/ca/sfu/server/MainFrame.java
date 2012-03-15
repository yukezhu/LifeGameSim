package ca.sfu.server;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.Timer;

import ca.sfu.cmpt431.facility.Board;
import ca.sfu.cmpt431.facility.BoardOperation;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	AutomataPanel automataPanel;

	Board board;
	
	public MainFrame(Board b, int height, int width)
	{
		super();
		setSize(width, height + 50);
		setJMenuBar(createMenuBar());
		setBackground(new Color(0xeb, 0xeb, 0xeb));
		
		board = b;
		
		automataPanel = new AutomataPanel(height, width);
		automataPanel.setBoard(board);
		automataPanel.setBackground(new Color(0xeb, 0xeb, 0xeb));
		
		setContentPane(automataPanel);
		setVisible(true);
		
		setTitle("Automata");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	/**
	 * Default constructor
	 * Mainly used for unit testing
	 */
	public MainFrame()
	{
		super();
		setSize(800, 850);
		setJMenuBar(createMenuBar());
		setBackground(new Color(0xeb, 0xeb, 0xeb));
		
		board = new Board(800, 800);
		BoardOperation.Randomize(board, 0.1);
		
		automataPanel = new AutomataPanel(800, 800);
		automataPanel.setCellSize(3);
		automataPanel.setBoard(board);
		automataPanel.setBackground(new Color(0xeb, 0xeb, 0xeb));
		
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
		
		timer.setDelay(50);
		timer.start();
		
		setTitle("Automata");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	/**
	 * Create JMenuBar for the whole program
	 */
	private JMenuBar createMenuBar()
	{
		/* Menu list */
		JMenuBar menuBar = new JMenuBar();
		JMenu aboutMenu = new JMenu("About");
		JMenu windowMenu = new JMenu("Window");
		/* Menu Item */
		JMenuItem zoomIn = new JMenuItem("Zoom In");
		JMenuItem zoomOut = new JMenuItem("Zoom Out");
		JMenuItem zoomPointer = new JMenuItem("Normal");
		/* Action Listeners */
		zoomIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				automataPanel.setZoomIn();
			}});
		
		zoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				automataPanel.setZoomOut();
			}});
		
		zoomPointer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				automataPanel.setNormal();
			}});
		/* Add to menu list */
		windowMenu.add(zoomIn);
		windowMenu.add(zoomOut);
		windowMenu.add(zoomPointer);
		menuBar.add(aboutMenu);
		menuBar.add(windowMenu);
		return menuBar;
	}
	
	public void setBoard(Board board)
	{
		this.board = board;
	}
	
	public Board getBoard()
	{
		return this.board;
	}
	
	public static void main(String[] args) {
		
		@SuppressWarnings("unused")
		MainFrame frame = new MainFrame();
		
	}

}
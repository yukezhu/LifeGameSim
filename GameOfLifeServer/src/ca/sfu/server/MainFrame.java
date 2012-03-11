package ca.sfu.server;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
	
	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		JMenu aboutMenu = new JMenu("About");
		JMenu windowMenu = new JMenu("Window");
		
		JMenuItem menuWindowZoomIn = new JMenuItem("Zoom In");
		JMenuItem menuWindowZoomOut = new JMenuItem("Zoom Out");
		JMenuItem menuWindowNormal = new JMenuItem("Normal");
		menuWindowZoomIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				automataPanel.setZoomIn();
			}});
		
		
		menuWindowZoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				automataPanel.setZoomOut();
			}});
		
		menuWindowNormal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				automataPanel.setNormal();
			}});
		windowMenu.add(menuWindowZoomIn);
		windowMenu.add(menuWindowZoomOut);
		windowMenu.add(menuWindowNormal);
		menuBar.add(aboutMenu);
		menuBar.add(windowMenu);
		return menuBar;
	}
	
	public MainFrame()
	{
		super();
		setSize(805, 860);
		setJMenuBar(createMenuBar());
		
		board = new Board(800, 800);
		BoardOperation.Randomize(board, 0.1);
		automataPanel = new AutomataPanel();
		automataPanel.setCellSize(1);
		automataPanel.setBoard(board);
		
		infoPanel = new JPanel();
		clientPanel = new JPanel();
		
		infoPanel.setBackground(Color.RED);
		clientPanel.setBackground(Color.GREEN);
		automataPanel.setBackground(Color.LIGHT_GRAY);
		
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
		
		@SuppressWarnings("unused")
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
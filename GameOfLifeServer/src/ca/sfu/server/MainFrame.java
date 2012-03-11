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
	
	InformationPanel infoPanel;
	AutomataPanel automataPanel;

	Board board;
	
	public MainFrame()
	{
		super();
		setSize(800, 850);
		setJMenuBar(createMenuBar());
		setBackground(Color.LIGHT_GRAY);
		
		board = new Board(800, 800);
		BoardOperation.Randomize(board, 0.1);
		automataPanel = new AutomataPanel();
		automataPanel.setCellSize(1);
		automataPanel.setBoard(board);
		automataPanel.setBackground(Color.LIGHT_GRAY);
		
		setContentPane(automataPanel);
		setVisible(true);
				
		infoPanel = new InformationPanel();
		infoPanel.setVisible(true);
		
		Timer timer = new Timer(0, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0) {
				automataPanel.setBoard(board);
				BoardOperation.NextMoment(board, null, null, null, null, false, false, false, false);;
				infoPanel.setCellNum(automataPanel.getCell());
				infoPanel.setLifeNum(automataPanel.getAlive());
				infoPanel.setCycleNum(automataPanel.getCycle());
				automataPanel.repaint();
			}
		});
		
		timer.setDelay(200);
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
	
	public static void main(String[] args) {
		
		@SuppressWarnings("unused")
		MainFrame frame = new MainFrame();
		
	}

}
package ca.sfu.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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

		BorderLayout layout = new BorderLayout();

		automataPanel = new AutomataPanel(height, width);
		automataPanel.setBoard(board);
		automataPanel.setBackground(new Color(0xeb, 0xeb, 0xeb));
		
		setLayout(layout);
		add(createToolBar(), BorderLayout.NORTH);
		add(automataPanel, BorderLayout.CENTER);
		
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
	 * Create the tool bar component
	 * @author	yla269
	 * @return	new tool bar
	 */
	private JToolBar createToolBar()
	{
		JToolBar jToolBar = new JToolBar("ToolBar");
		jToolBar.setFloatable(false);
		jToolBar.setVisible(true);		
		jToolBar.setSize(5, 5);
		ImageIcon zoominButtonIcon = new ImageIcon("Images/zoomin.png");
		ImageIcon zoomoutButtonIcon = new ImageIcon("Images/zoomout.png");
		ImageIcon normalButtonIcon = new ImageIcon("Images/normal.png");

		JButton zoomin = new JButton("",zoominButtonIcon);
		zoomin.setSize(5, 5);

		zoomin.setBorderPainted(false);
		zoomin.setVisible(true);

		JButton original = new JButton("",normalButtonIcon);

		original.setSize(5, 5);
		original.setBorderPainted(false);
		original.setVisible(true);

		JButton zoomout = new JButton("",zoomoutButtonIcon);
		zoomout.setSize(5, 5);

		zoomout.setBorderPainted(false);
		zoomout.setVisible(true);

		jToolBar.add(zoomin);    
		jToolBar.add(zoomout);
		jToolBar.add(original);

		ActionListener a = new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				Cursor cursors = Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("Images/zoomin.png"), new Point(10, 10),"zoom_in");
				automataPanel.setCursor(cursors);
				automataPanel.requestFocusInWindow();
				automataPanel.setZoomIn();
			}
		};
		zoomin.addActionListener(a);

		ActionListener b = new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				Cursor cursors = Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("Images/zoomout.png"), new Point(10, 10),"zoom_out");
				automataPanel.setCursor(cursors);
				automataPanel.requestFocusInWindow();
				automataPanel.setZoomOut();
			}
		};		
		zoomout.addActionListener(b);

		ActionListener c = new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				Cursor cursors = Cursor.getDefaultCursor();
				automataPanel.setCursor(cursors);
				automataPanel.requestFocusInWindow();
				automataPanel.setNormal();
			}
		};		
		original.addActionListener(c);
		return jToolBar;
	}

	/**
	 * Create JMenuBar for the whole program
	 */
	private JMenuBar createMenuBar()
	{
		/* Menu list */
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu windowMenu = new JMenu("Window");
		/* Menu Item */
		JMenuItem about = new JMenuItem("About");
		JMenuItem exit = new JMenuItem("Exit");
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
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JLabel label = new JLabel();
				Font font = label.getFont();

				// create some css from the label's font
				StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
				style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
				style.append("font-size:" + font.getSize() + "pt;");

				// html content
				String text1 = "<html><body><p><strong><font size=\"5\" face=\"arial\" color=\"black\">Game of Life</font></strong></p>" +
						"<p><i>Version 1.1</i></p><p><i>School of Computing Science, Simon Fraser University</i></p>"    													 +
						"<p>Distributed cellular automaton simulation application, called world  of cell.</p>"       +  
						"<p><b>Author:</b> Yuke Zhu, Luna Lu, Yang Liu, Yao Xie, Xiaying Peng</p>"                                       +
						"<p>Sound interesting? <a href=\"https://github.com/leafpicker/LifeGameSim\">Get involved!</a></p></body></html>";
				JEditorPane ep = new JEditorPane("text/html", text1);

				// handle link events
				ep.addHyperlinkListener(new HyperlinkListener()
				{
					@Override
					public void hyperlinkUpdate(HyperlinkEvent e)
					{
						if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
							openURL(e.getURL().toString());
						//ProcessHandler.launchUrl(e.getURL().toString()); // roll your own link launcher or use Desktop if J6+
					}
				});

				ep.setEditable(false);
				ep.setBackground(label.getBackground());

				// show
				JOptionPane.showMessageDialog(null, ep);

			}});
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}});
		/* Add to menu list */
		fileMenu.add(about);
		fileMenu.addSeparator();
		fileMenu.add(exit);
		windowMenu.add(zoomIn);
		windowMenu.add(zoomOut);
		windowMenu.add(zoomPointer);
		menuBar.add(fileMenu);
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

	public static void openURL(String url) {
		try {
			browse(url);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error attempting to launch web browser:\n" + e.getLocalizedMessage());
		}
	}

	/**
	 * Browse website from default browser with multiple OS compatibility
	 */
	private static void browse(String url) throws ClassNotFoundException, IllegalAccessException,
	IllegalArgumentException, InterruptedException, InvocationTargetException, IOException,
	NoSuchMethodException {
		String osName = System.getProperty("os.name", "");
		if (osName.startsWith("Mac OS")) {
			Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
			Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
			openURL.invoke(null, new Object[] { url });
		} else if (osName.startsWith("Windows")) {
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
		} else { // assume Unix or Linux
			String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
			String browser = null;
			for (int count = 0; count < browsers.length && browser == null; count++)
				if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0)
					browser = browsers[count];
			if (browser == null)
				throw new NoSuchMethodException("Could not find web browser");
			else
				Runtime.getRuntime().exec(new String[] { browser, url });
		}
	}

	public static void main(String[] args) {

		@SuppressWarnings("unused")
		MainFrame frame = new MainFrame();

	}

}
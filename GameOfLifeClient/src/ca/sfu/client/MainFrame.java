package ca.sfu.client;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	/* Client object */
	private Client client = new Client();
	Thread clientThread;

	private static final String SERVER_IP = "142.58.35.59";
	private static final String CLIENT_IP = "";

	/* New UI widgets */
	JLabel clientIpLabel = new JLabel("Client IP");
	JTextField clientIp = new JTextField(15);
	JLabel serverIpLabel = new JLabel("Server IP");
	JTextField serverIp = new JTextField(15);
	JButton connectBtn = new JButton("Connect");

	boolean connected;

	public MainFrame() {

		Container contentPane = getContentPane();

		serverIp.setText(SERVER_IP);
		clientIp.setText(CLIENT_IP);

		SpringLayout layout = new SpringLayout();
		contentPane.setLayout(layout);

		/* Add to content pane */
		contentPane.add(clientIpLabel);
		contentPane.add(clientIp);
		contentPane.add(serverIpLabel);
		contentPane.add(serverIp);
		contentPane.add(connectBtn);
		connectBtn.setFocusable(false);

		/* Regular expression */
		//		String ValidIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.";
		//		String ValidHostnameRegex = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$";
		//		final String IPADDRESS_PATTERN = ValidIpAddressRegex + ValidHostnameRegex;

		/* Put UI constrains */
		layout.putConstraint(SpringLayout.WEST, clientIpLabel, 15, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, clientIpLabel, 30, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.NORTH, clientIp, 30, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, clientIp, 80, SpringLayout.WEST, clientIpLabel);

		layout.putConstraint(SpringLayout.WEST, serverIpLabel, 15, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, serverIpLabel, 30, SpringLayout.NORTH, clientIpLabel);
		layout.putConstraint(SpringLayout.NORTH, serverIp, 30, SpringLayout.NORTH, clientIp);
		layout.putConstraint(SpringLayout.WEST, serverIp, 80, SpringLayout.WEST, serverIpLabel);

		layout.putConstraint(SpringLayout.NORTH, connectBtn, 40, SpringLayout.NORTH, serverIpLabel);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, connectBtn, 0, SpringLayout.HORIZONTAL_CENTER, contentPane);

		/* Set listener */
		connectBtn.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(!connected)
				{
					System.out.println("Connecting...");
					clientThread = new Thread() {
						public void run() {
							try {
								client.startClient(serverIp.getText(), clientIp.getText());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
					clientThread.start();
					connectBtn.setText("Disconnect");

				} else 
				{
//					client.quit(); /* Add disconnect function here */ 
//					clientThread.interrupt();
					connectBtn.setText("Connect");
				}
				connected = !connected;
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

		connected = false;
		setSize(300, 180);
		setTitle("Client");
		setDefaultCloseOperation(EXIT_ON_CLOSE);    
	}  

}

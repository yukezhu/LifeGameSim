package ca.sfu.server;
import java.io.IOException;

import javax.swing.JFrame;

import ca.sfu.message.AutomataMsg;
import ca.sfu.network.MessageReceiver;
import ca.sfu.network.MessageSender;
import ca.sfu.network.SynchronizedMsgQueue.MessageWithIp;


public class Server{
	
	protected static final int LISTEN_PORT = 6560;
	private MessageReceiver Receiver;
	private MessageSender Sender1;
	private MessageSender Sender2;
	private String client1_ip;
	private String client2_ip;
	
	private int status;
	
	public Server() throws IOException {
		Receiver = new MessageReceiver(LISTEN_PORT);
		status = 0;
	}
	
	protected void startServer() throws IOException, ClassNotFoundException, InterruptedException
	{
		// UI
		JFrame frame = new JFrame();
		frame.setSize(480, 480);
		AutomataMsg auto = new AutomataMsg(10, 10);
		AutomataPanel panel = new AutomataPanel();
		panel.setAutomata(auto);
		frame.setContentPane(panel);
		frame.setVisible(true);
		
		System.out.println("UI");
		
		MessageWithIp m;
		
		while(true) {
			if(!Receiver.isEmpty()) {
//				System.out.println(status);
				m = Receiver.getNextMessageWithIp();
				switch(status) {
					case 0:
						client1_ip = m.getIp();
						Sender1 = new MessageSender(client1_ip, LISTEN_PORT);
						System.out.println(client1_ip + "connected!!!");
						status = 1;
						break;
					case 1:
						client2_ip = m.getIp();
						Sender2 = new MessageSender(client2_ip, LISTEN_PORT);
						System.out.println(client2_ip + "connected!!!");
						Sender1.sendMsg(client2_ip);
						status = 2;
						break;
					case 2:
						if(!m.getIp().equals(client1_ip))
							System.out.println("Error!");
						Sender2.sendMsg(client1_ip);
						status = 3;
						break;
					case 3:
						if(!m.getIp().equals(client2_ip))
							System.out.println("Error!");
						System.out.println("before");
						Sender1.sendMsg(auto.left());
//						Sender1.sendMsg(auto);
//						Sender1.sendMsg(new AutomataMsg(3, 4));
//						Sender1.sendMsg("left");
						System.out.println("after");
						status = 4;
						break;
					case 4:
						if(!m.getIp().equals(client1_ip))
							System.out.println("Error!");
						Sender2.sendMsg(auto.right());
						status = 5;
						break;
					case 5:
						if(!m.getIp().equals(client2_ip))
							System.out.println("Error!");
						Sender1.sendMsg("left");
						status = 6;
						break;
					case 6:
						if(!m.getIp().equals(client1_ip))
							System.out.println("Error!");
						Sender2.sendMsg("right");
						status = 7;
						break;
					case 7:
						if(!m.getIp().equals(client2_ip))
							System.out.println("Error!");
						Sender1.sendMsg("start");
						status = 8;
						break;
					case 8:
						if(!m.getIp().equals(client1_ip))
							System.out.println("Error!");
						Sender2.sendMsg("start");
						status = 9;
						break;
					case 9:
						if(!m.getIp().equals(client2_ip))
							System.out.println("Error!");
						status = 10;
						break;
					case 10:
//						if(m == null) System.out.println("null");
						if(m.getIp().equals(client1_ip)){
							auto.mergeLeft((AutomataMsg)m.extracMessage());
							//Sender1.sendMsg("OK");
						}
						else{
							auto.mergeRight((AutomataMsg)m.extracMessage());
							//Sender2.sendMsg("OK");
						}
						status = 11;
						break;
					case 11:
						if(m.getIp().equals(client1_ip)){
							auto.mergeLeft((AutomataMsg)m.extracMessage());
							//Sender1.sendMsg("OK");
						}
						else{
							auto.mergeRight((AutomataMsg)m.extracMessage());
							//Sender2.sendMsg("OK");
						}
						frame.repaint();
						
						Sender1.sendMsg("start");
						status = 8;
						break;
					default:
						break;
				}
			}
		}
	}

}
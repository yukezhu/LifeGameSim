package ca.sfu.client;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;

import ca.sfu.message.AutomataMsg;
import ca.sfu.network.MessageReceiver;
import ca.sfu.network.MessageSender;

public class Client {
	
	protected static final int SERVER_PORT = 6560;
	
	private int status;
	private AutomataMsg auto;
	private String direction;
	private int[] border;

	public MessageReceiver Receiver; 
	public MessageSender Sender1;
	public MessageSender Sender2;
	public int port;
	
	public Client() throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException
	{	
		while(true){
			
			Random r = new Random(); 
			port = r.nextInt(55535)+10000;
			try{
				Receiver = new MessageReceiver(port);
				System.out.println(port);
				break;				
			}
			catch(Exception e){
				continue;
			}
		}
		
		
		
		
		
		status = 0;
	}
	
	public void startClient() throws IOException, ClassNotFoundException, InterruptedException
	{
		while(true){
			if(!Receiver.isEmpty()){
				System.out.println(status);
				switch(status) {
					case 0:	
						Sender1 = new MessageSender("142.58.35.71", SERVER_PORT);
						Sender1.sendMsg(port);
						status = 1;
						break;
//					case 0:
//						String pair_ip = (String)Receiver.getNextMessageWithIp().extracMessage();
//						Sender1.sendMsg("OK");
//						Sender2 = new MessageSender(pair_ip, LISTEN_PORT);
//						System.out.println(pair_ip+"connected!");
//						status = 1;
//						break;
//					case 1:
//						auto = (AutomataMsg)Receiver.getNextMessageWithIp().extracMessage();
//						Sender1.sendMsg("OK");
//						status = 2;
//						break;
//					case 2:
//						direction = (String)Receiver.getNextMessageWithIp().extracMessage();
//						Sender1.sendMsg("OK");
//						border = new int[auto.getHeight()];
//						status = 3;
//						break;
//					case 3:
//						String start = (String)Receiver.getNextMessageWithIp().extracMessage();
//						System.out.println(start);
//						Sender1.sendMsg("OK");
//						Thread.sleep(50);
//						for(int i=0;i<border.length; i++){
//							if(direction.equals("left"))
//								border[i] = auto.bitmap[i][auto.width-1];
//							else
//								border[i] = auto.bitmap[i][0];
//						}
//						Sender2.sendMsg(border);
//						status = 4;
//						break;
//					case 4:
//						border = (int[])Receiver.getNextMessageWithIp().extracMessage();
//						auto.nextMoment(direction.equals("left"), border);
//						Sender1.sendMsg(auto);
//						status = 3;
//						break;
					default:
						break;
				}
			}
		}
		
	}
		
}



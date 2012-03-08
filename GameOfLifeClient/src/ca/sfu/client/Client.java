package ca.sfu.client;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import ca.sfu.cmpt431.facility.Comrade;
import ca.sfu.cmpt431.facility.Outfits;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;
import ca.sfu.cmpt431.message.join.JoinOutfitsMsg;
import ca.sfu.cmpt431.message.join.JoinRequestMsg;
import ca.sfu.cmpt431.message.regular.RegularConfirmMsg;
import ca.sfu.message.AutomataMsg;
import ca.sfu.network.MessageReceiver;
import ca.sfu.network.MessageSender;
import ca.sfu.network.SynchronizedMsgQueue.MessageWithIp;
public class Client {
	
	protected static final int SERVER_PORT = 6560;
	
	private int status;
	private int cid;
	private AutomataMsg auto;
	private String direction;
	private int[] border;
	private RegularConfirmMsg confirm = new RegularConfirmMsg(-10);
	private Outfits outfit;
//	private ArrayList<MessageSender> newClientSender = new ArrayList();
//	private ArrayList<Comrade>  regedClientSender = new ArrayList();

	public MessageReceiver Receiver; 
	public MessageSender Sender1;
	public MessageSender Sender2;
	public Comrade comrade1 = new Comrade(-1, Sender1);
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
		comrade1.sender = new MessageSender("142.58.35.71", SERVER_PORT);
		JoinRequestMsg Request = new JoinRequestMsg(port);
		comrade1.sender.sendMsg(Request);
//		regedClientSender.get(0).sender.sendMsg(Request);
		status = 0;
	}
	
	public void startClient() throws IOException, ClassNotFoundException, InterruptedException
	{
		while(true){
			if(!Receiver.isEmpty()){
				System.out.println(status);
				
				switch(status) {
					case 0:	
						Receiver.getNextMessageWithIp().extracMessage();
						comrade1.sender.sendMsg(confirm);						
						status = 1;
						break;
					case 1:
						MessageWithIp msgIp;
						JoinOutfitsMsg ob;
						JoinOutfitsMsg joinmsg;
						msgIp = Receiver.getNextMessageWithIp();
						ob = (JoinOutfitsMsg)msgIp.extracMessage();
						outfit = ob.yourOutfits;
						joinmsg = (JoinOutfitsMsg)ob;
						cid = outfit.myId;
						int pair_id = ob.getClientId();
						int pair_port = joinmsg.myPort;
						if(pair_port <0){
							comrade1.sender.sendMsg(confirm);
							System.out.println(pair_id);
						}
						else{
							String pair_ip = msgIp.getIp().substring(1); 
							Comrade comrade2 = new Comrade(pair_id, Sender2);
							comrade2.sender = new MessageSender(pair_ip, pair_port);							
							comrade2.sender.sendMsg(confirm);
						}
						status = 2;
						break;
					case 2:
						String start = (String)Receiver.getNextMessageWithIp().extracMessage();
						
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
//	//store all the adding request into an array
//	protected boolean handleNewAdding(MessageWithIp m, int nextStatus) throws IOException{
//		//check if m is a new adding request message
//		Message msg = (Message) m.extracMessage();
//		if(msg.getMessageCode()==MessageCodeDictionary.JOIN_REQUEST){
//			JoinRequestMsg join = (JoinRequestMsg)m.extracMessage();
//			newClientSender.add(new MessageSender(m.getIp(), join.getClientPort()));
//			System.out.println("adding new to pending");
//			//if it is a new adding request, we need to go to nextStatus
//			//most time it should be the same status
//			status = nextStatus;
//			return true;
//		}
//		return false;
//	}
//	
//	//deal with the pending adding request
//	//manage the heap
//	protected void handlePending() throws IOException{
//		while(!newClientSender.isEmpty()){
//			int cid = regedClientSender.size();
//			//manage the heap
//			if(cid!=0){ //not the first client
//				Comrade c = regedClientSender.get(0); //get it down one level
//				regedClientSender.remove(0);
//				regedClientSender.add(c);
//			}
//			regedClientSender.add(new Comrade(cid, newClientSender.get(0)));
//			
//			//remove the pending one
//			newClientSender.remove(0);
//			regedClientSender.get(cid).sender.sendMsg(new ConfirmMsg(-1));
//			waiting4confirm++;
//			System.out.println("register a new client");
//		}
//	}
//	
//	//getting a new confirm message, if there is no waiting confirm, go to nextStatus
//	protected void handleConfirm(MessageWithIp m, int nextStatus){
//		waiting4confirm--;
//		System.out.println("getting a confirm");
//		if(waiting4confirm==0)
//			status = nextStatus;
//	}	
}



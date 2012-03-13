package ca.sfu.server;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import ca.sfu.cmpt431.facility.*;
import ca.sfu.cmpt431.message.*;
import ca.sfu.cmpt431.message.join.*;
import ca.sfu.cmpt431.message.regular.*;
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
	private ArrayList<MessageSender> newClientSender = new ArrayList<MessageSender>();
	private ArrayList<Comrade>  regedClientSender = new ArrayList<Comrade>();
	private int waiting4confirm = 0;
	private int nextClock = 0;
	
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
//		AutomataMsg auto = new AutomataMsg(50, 50);
//		Board b = new Board(50, 50);
//		BoardOperation.Randomize(b,0.1);
		
		Board b = BoardOperation.LoadFile("/Users/leafpicker/Developer/Github/LifeGameSim/Patterns/oscillator1.lg");
		
		AutomataPanel panel = new AutomataPanel();
		panel.setBoard(b);
		panel.setCellSize(3);
		frame.setContentPane(panel);
		frame.setVisible(true);
		
		System.out.println("UI");
		
		MessageWithIp m;
		
		while(true) {
			if(!Receiver.isEmpty()) {
//				System.out.println(status);
				m = Receiver.getNextMessageWithIp();
//				System.out.println("waiting:"+waiting4confirm);
				
				switch(status) {
					//waiting for first client
					case 0:
						handleNewAdding(m,1);
						handlePending();
						//send it the outfit
						regedClientSender.get(0).sender.sendMsg(new JoinOutfitsMsg(-1, -1, new Outfits(0,nextClock,0,0,b)));
						waiting4confirm++;
						status = 2;
						
						break;
					
					//wait for the confirm
					//start a cycle
					case 2:
						if(handleNewAdding(m,2))
							break;
										
						handleConfirm(m,3); //expect only one message responding for JoinOutfitsMsg
						
						if(waiting4confirm == 0){
							//send you a start
							for (Comrade var : regedClientSender) {
								var.sender.sendMsg(new RegularNextClockMsg(nextClock));
								waiting4confirm++;
							}
							status = 3;
						}
						
						break;
						
					//waiting for the client to send the result back
					//handle new adding or
					//restart next cycle
					case 3:
						if(handleNewAdding(m,3))
							break;
						
						handleNewBoardInfo(m,b,3);
						if(waiting4confirm!=0){
							break;
						}
						
						Thread.sleep(50);
						frame.repaint();
//						BoardOperation.Print(b);
//						System.out.println("repaint");
						
						//handle adding
						if(handlePending()){
							status = 2;
							break;
						}
						
						//start
						if(waiting4confirm==0){
							for (Comrade var : regedClientSender) {
//								System.out.println("sending start");
								var.sender.sendMsg(new RegularNextClockMsg(nextClock));
								waiting4confirm++;
							}
						}
						break;
					//new addings (not the first client)
					case 4:
						if(handleNewAdding(m,4))
							break;
						
						handleConfirm(m,-1);
						
						if(waiting4confirm!=0) //still need waiting for confirmation
							break;
						
						//deal with add
						if(newClientSender.size()!=0){
							handlePending();
							status = 5;
							break;
						}
						
						//start
						if(waiting4confirm==0){
							for (Comrade var : regedClientSender) {
								var.sender.sendMsg(new RegularNextClockMsg(nextClock));
								waiting4confirm++;
							}
						}
						status = 3;
						break;
						
					case -1:
						client1_ip = m.getIp();
						Sender1 = new MessageSender(client1_ip, LISTEN_PORT);
						System.out.println(client1_ip + "connected!!!");
						status = 1;
						break;
					case -2:
						client2_ip = m.getIp();
						Sender2 = new MessageSender(client2_ip, LISTEN_PORT);
						System.out.println(client2_ip + "connected!!!");
						Sender1.sendMsg(client2_ip);
						status = 2;
						break;
					case -3:
						if(!m.getIp().equals(client1_ip))
							System.out.println("Error!");
						Sender2.sendMsg(client1_ip);
						status = 3;
						break;
					case -4:
						if(!m.getIp().equals(client2_ip))
							System.out.println("Error!");
						System.out.println("before");
						//Sender1.sendMsg(auto.left());
//						Sender1.sendMsg(auto);
//						Sender1.sendMsg(new AutomataMsg(3, 4));
//						Sender1.sendMsg("left");
						System.out.println("after");
						status = 4;
						break;
					case -5:
						if(!m.getIp().equals(client1_ip))
							System.out.println("Error!");
						//Sender2.sendMsg(auto.right());
						status = 5;
						break;
					case -6:
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
							//auto.mergeLeft((AutomataMsg)m.extracMessage());
							//Sender1.sendMsg("OK");
						}
						else{
							//auto.mergeRight((AutomataMsg)m.extracMessage());
							//Sender2.sendMsg("OK");
						}
						status = 11;
						break;
					case 11:
						if(m.getIp().equals(client1_ip)){
							//auto.mergeLeft((AutomataMsg)m.extracMessage());
							//Sender1.sendMsg("OK");
						}
						else{
							//auto.mergeRight((AutomataMsg)m.extracMessage());
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
	
	//store all the adding request into an array
	protected boolean handleNewAdding(MessageWithIp m, int nextStatus) throws IOException{
		//check if m is a new adding request message
		Message msg = (Message) m.extracMessage();
		if(msg.getMessageCode()==MessageCodeDictionary.JOIN_REQUEST){
			JoinRequestMsg join = (JoinRequestMsg)m.extracMessage();
			newClientSender.add(new MessageSender(m.getIp(), join.clientPort));
			System.out.println("adding a new client to pending list");
			//if it is a new adding request, we need to go to nextStatus
			//most time it should be the same status
			status = nextStatus;
			return true;
		}
		return false;
	}
	
	//deal with the pending adding request
	//manage the heap
	protected boolean handlePending() throws IOException{
		while(!newClientSender.isEmpty()){
			int cid = regedClientSender.size();
			//manage the heap
			if(cid!=0){ //not the first client
				Comrade c = regedClientSender.get(0); //get it down one level
				
				//c is the pair
				int mode;
				if((((int)(Math.log(2*cid+1)/Math.log(2)))%2)!=0)
					mode = MessageCodeDictionary.SPLIT_MODE_HORIZONTAL;
				else
					mode = MessageCodeDictionary.SPLIT_MODE_VERTICAL;
				
//				System.out.println(cid);
//				System.out.println((Math.log(2*cid+1)/Math.log(2))%2);
//				System.out.println("mode"+mode);
				System.out.println("Sending a split command to "+ c.id+", new client id: "+cid+", split mode: "+(mode==0?"vertical":"horizontal"));
				
//				System.out.println("send JoinSplitMsg");
//				System.out.println(newClientSender.get(0).hostIp);
				c.sender.sendMsg(new JoinSplitMsg(cid, newClientSender.get(0).hostListenningPort, newClientSender.get(0).hostIp, mode));
				
				regedClientSender.remove(0);
				regedClientSender.add(c);
				regedClientSender.add(new Comrade(cid, newClientSender.get(0).hostListenningPort, newClientSender.get(0).hostIp, newClientSender.get(0)));
				waiting4confirm++;
			}
			else{
				regedClientSender.add(new Comrade(cid, newClientSender.get(0).hostListenningPort, newClientSender.get(0).hostIp, newClientSender.get(0)));
				//regedClientSender.get(cid).sender.sendMsg(new RegularConfirmMsg(-1));
			}
			
			//remove the pending one
			newClientSender.remove(0);
			
			
			System.out.println("register a new client");
			return true;
		}
		return false;
	}
	
	protected void handleNewBoardInfo(MessageWithIp m, Board b, int nextStatus){
		waiting4confirm--;
//		System.out.println("getting a result");
		
		if(waiting4confirm==0)
			status = nextStatus;
		
		RegularBoardReturnMsg r = (RegularBoardReturnMsg)m.extracMessage();
		BoardOperation.Merge(b, r.board, r.top, r.left);
//		b = (Board)m.extracMessage();
	}
	
	//getting a new confirm message, if there is no waiting confirm, go to nextStatus
	protected void handleConfirm(MessageWithIp m, int nextStatus){
		waiting4confirm--;
//		System.out.println("getting a confirm");
		if(waiting4confirm==0)
			status = nextStatus;
	}
}
package ca.sfu.server;
import java.io.IOException;
import java.util.ArrayList;

import ca.sfu.cmpt431.facility.Board;
import ca.sfu.cmpt431.facility.BoardOperation;
import ca.sfu.cmpt431.facility.Comrade;
import ca.sfu.cmpt431.facility.Outfits;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;
import ca.sfu.cmpt431.message.join.JoinOutfitsMsg;
import ca.sfu.cmpt431.message.join.JoinRequestMsg;
import ca.sfu.cmpt431.message.join.JoinSplitMsg;
import ca.sfu.cmpt431.message.leave.LeaveReceiverMsg;
import ca.sfu.cmpt431.message.merge.MergeLastMsg;
import ca.sfu.cmpt431.message.regular.RegularBoardReturnMsg;
import ca.sfu.cmpt431.message.regular.RegularNextClockMsg;
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
	
	private ArrayList<Integer> toLeave = new ArrayList<Integer>();
	
	private int waiting4confirm = 0;
	private int nextClock = 0;
	
	private int status;
	
	private int phase;
	private static final int COMPUTE = 0;
	private static final int ADD = 1;
	private static final int LEAVE = 2;
	
	/* UI widgets */
	MainFrame frame = null;
	InformationPanel infoPanel = null;
	
	public Server() throws IOException {
		Receiver = new MessageReceiver(LISTEN_PORT);
		status = 0;
	}
	
	protected void startServer() throws IOException, ClassNotFoundException, InterruptedException
	{
		// UI
		Board b = BoardOperation.LoadFile("Patterns/HerschelLoop.lg");
		
		System.out.println("UI");
		frame = new MainFrame(b, 800, 800);
		infoPanel = new InformationPanel();
		
		MessageWithIp m;
		int result = -1;
		
		while(true) {
			if(!Receiver.isEmpty()) {
				m = Receiver.getNextMessageWithIp();
				
				switch(status) {
					//waiting for first client
					case 0:
						handleNewAddingLeaving(m,1);
						handlePending();
						//send it the outfit
						regedClientSender.get(0).sender.sendMsg(new JoinOutfitsMsg(-1, -1, new Outfits(0,nextClock,0,0,b)));
						waiting4confirm++;
						status = 2;
						break;
					
					//wait for the confirm
					//start a cycle
					case 2:
						if(handleNewAddingLeaving(m,2))
							break;
										
						handleConfirm(m,3); //expect only one message responding for JoinOutfitsMsg
						
						if(!newClientSender.isEmpty()){
							handlePending();
							status = 2;
							break;
						}
						
						if(waiting4confirm == 0){
							//send you a start
							System.out.println("sending start");
							infoPanel.setCycleNum(frame.automataPanel.getCycle());
							for (Comrade var : regedClientSender) {
								var.sender.sendMsg(new RegularNextClockMsg(nextClock));
								waiting4confirm++;
							}
							status = 3;
							phase = COMPUTE;
						}
						
						break;
						
					//waiting for the client to send the result back
					//handle new adding or
					//restart next cycle
					case 3:
						if(handleNewAddingLeaving(m,3))
							break;
						
						if(phase == COMPUTE){
							handleNewBoardInfo(m,b,3);
							if(waiting4confirm!=0){
								break;
							}
							
//							Thread.sleep(50);
							frame.repaint();
							infoPanel.setCellNum(frame.automataPanel.getCell());
							infoPanel.setLifeNum(frame.automataPanel.getAlive());
							
							infoPanel.setTargetNum("localhost");
							
							phase = LEAVE;
//							BoardOperation.Print(b);
							System.out.println("repaint");
						}
						
						if(phase == LEAVE){
							
							//deal with the confirm
							//manage the heap
							if(((Message)m.extracMessage()).getMessageCode()==MessageCodeDictionary.REGULAR_CONFIRM){
								if(result == 1){
									int s = regedClientSender.size();
									regedClientSender.get(s-1).sender.sendMsg(new LeaveReceiverMsg(MessageCodeDictionary.ID_SERVER, 0, ""));
									regedClientSender.get(s-1).sender.close();
									regedClientSender.remove(s-1);
									Comrade c = regedClientSender.get(s-2);
									regedClientSender.remove(s-2);
									regedClientSender.add(0, c);
								}
								else if(result == 2){
									int s = regedClientSender.size();
									regedClientSender.get(s-2).sender.sendMsg(new LeaveReceiverMsg(MessageCodeDictionary.ID_SERVER, 0, ""));
									regedClientSender.get(s-2).sender.close();
									regedClientSender.remove(s-2);
									Comrade c = regedClientSender.get(s-1);
									regedClientSender.remove(s-1);
									regedClientSender.add(0, c);
								}
								else if(result == 3){
									int cid = toLeave.get(0);
									int index = findClient(cid);
									int s = regedClientSender.size();
									regedClientSender.get(index).sender.sendMsg(new LeaveReceiverMsg(MessageCodeDictionary.ID_SERVER, 0, ""));
									regedClientSender.get(index).sender.close();
									regedClientSender.set(index, regedClientSender.get(s-1));
									regedClientSender.remove(s-1);
									Comrade c = regedClientSender.get(s-2);
									regedClientSender.remove(s-2);
									regedClientSender.add(0, c);
								}
								else{
									//error
									System.out.println("error.");
								}
								toLeave.remove(0);
							}
							
							while((result=handleLeaving())!=-1){
								//0, continue handling
								if(result == 0){
									toLeave.remove(0);
									continue;
								}
								//4, no client now, go to status 0 pls
								else if(result == 4){
									toLeave.remove(0);
									status = 0;
									break;
								}
								else{
									//wait for a confirm
									status = 3;
									break;
								}
							}
							
							if(toLeave.isEmpty())
								phase = ADD;
							else
								break;
						}
						
						
						//handle adding
						if(handlePending()){
							status = 2;
							break;
						}
						
						//start
						if(waiting4confirm==0){
							for (Comrade var : regedClientSender) {
								
								var.sender.sendMsg(new RegularNextClockMsg(nextClock));
								waiting4confirm++;
							}
							System.out.println("sending start");
							infoPanel.setCycleNum(frame.automataPanel.getCycle());
							phase = COMPUTE;
						}
						break;
					//new addings (not the first client)
					case -10:
						if(handleNewAddingLeaving(m,4))
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
						System.out.println("sending start!!!!!!!!!");
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
					case -7:
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
	protected boolean handleNewAddingLeaving(MessageWithIp m, int nextStatus) throws IOException{
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
		else if(msg.getMessageCode()==MessageCodeDictionary.REGULAR_BOARD_RETURN){
			//TODO
			RegularBoardReturnMsg r = (RegularBoardReturnMsg)msg;
			if(r.isLeaving){
				toLeave.add(msg.getClientId());
				System.out.println("a client want to leave, pending now");
				return false;
			}
		}
		return false;
	}
	
	protected int handleLeaving() throws IOException{
		if(toLeave.isEmpty())
			return -1;
		
		int cid = toLeave.get(0);
		
		if(newClientSender.size()!=0){
			//ask a new client to replace it immediately
			regedClientSender.get(0).sender.sendMsg(new LeaveReceiverMsg(MessageCodeDictionary.ID_SERVER, 0, ""));
			regedClientSender.get(findClient(cid)).sender.close();
			regedClientSender.set(findClient(cid), new Comrade(regedClientSender.size(), newClientSender.get(0).hostListenningPort, newClientSender.get(0).hostIp, newClientSender.get(0)));
			newClientSender.remove(0);
			
			System.out.println("new adding, replace");
			
			//no confirm
			return 0;
		}
		else if(regedClientSender.size()==1){
			//there is only one client and no adding
			//ask him to leave directly
			regedClientSender.get(0).sender.sendMsg(new LeaveReceiverMsg(MessageCodeDictionary.ID_SERVER, 0, ""));
			regedClientSender.get(findClient(cid)).sender.close();
			regedClientSender.remove(findClient(cid));
			
			System.out.println("only one client, leave directly");
			
			//no confirm, everything done, but you need to wait for a client to start
			return 4;
		}
		else if(isLastPair(cid)!=-1){
			//it is the last node, or the pair of last node
			//ask the last pair merge
			int s = regedClientSender.size();
			int pair_index = (s%2==0)?((s-4)>=0?(s-4):-1):0;
			
			if(isLastPair(pair_index)!=-1)
				pair_index = -1; //you pair can not be your neighbour, occurs when there is 2 clients
			
			int pair_cid = -1;
			String pair_ip = "";
			int pair_port = -1;
			if(pair_index!=-1){
				pair_cid = regedClientSender.get(pair_index).id;
				pair_ip = regedClientSender.get(pair_index).ip;
				pair_port = regedClientSender.get(pair_index).port;
			}
			
			System.out.println("last pair handle pending:"+pair_cid);
			regedClientSender.get(regedClientSender.size()-1-isLastPair(cid)).sender.sendMsg(new MergeLastMsg(pair_cid, pair_ip, pair_port));
			//wait for a confirm, still need a LeaveReceiverMsg
			return isLastPair(cid)+1; //1 if last or 2 if second last
		}
		else{
			//ask the last node merge first,give it a new pair id
			//ask the last node to replace
			int s = regedClientSender.size();
			
			int pair_index = (s%2==0)?((s-4)>=0?(s-4):-1):0;
			
			if(isLastPair(pair_index)!=-1)
				pair_index = -1; //you pair can not be your neighbour, occurs when there is 2 clients
			
			int pair_cid = -1;
			String pair_ip = "";
			int pair_port = -1;
			if(pair_index!=-1){
				pair_cid = regedClientSender.get(pair_index).id;
				pair_ip = regedClientSender.get(pair_index).ip;
				pair_port = regedClientSender.get(pair_index).port;
			}
			
			System.out.println("nomal merge handle pending:"+pair_cid);
			regedClientSender.get(regedClientSender.size()-1).sender.sendMsg(new MergeLastMsg(pair_cid, pair_ip, pair_port));
			//wait for a confirm, still need a LeaveReceiverMsg
			return 3;
		}
	}
	
	private int findClient(int cid){
		for(int i=0; i<regedClientSender.size(); i++){
			if(regedClientSender.get(i).id == cid){
				return i;
			}
		}
		return -1;
	}
	
	private int isLastPair(int cid){
		int s = regedClientSender.size();
		if(regedClientSender.get(s-1).id == cid)
			return 0;
		else if(regedClientSender.get(s-2).id == cid)
			return 1;
		else
			return -1;
	}
	
	//deal with the pending adding request
	//manage the heap
	protected boolean handlePending() throws IOException{
		//you can add at most N new clients in a cycle, N is the number of all clients existing before
		
		if(!newClientSender.isEmpty()){
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
			
			infoPanel.setClientNum(regedClientSender.size());
			
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
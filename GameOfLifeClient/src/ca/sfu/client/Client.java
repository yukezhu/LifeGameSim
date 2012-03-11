package ca.sfu.client;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ca.sfu.cmpt431.facility.Board;
import ca.sfu.cmpt431.facility.BoardOperation;
import ca.sfu.cmpt431.facility.Border;
import ca.sfu.cmpt431.facility.Comrade;
import ca.sfu.cmpt431.facility.Neighbour;
import ca.sfu.cmpt431.facility.Outfits;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;
import ca.sfu.cmpt431.message.join.JoinOutfitsMsg;
import ca.sfu.cmpt431.message.join.JoinRequestMsg;
import ca.sfu.cmpt431.message.join.JoinSplitMsg;
import ca.sfu.cmpt431.message.regular.RegularBoardReturnMsg;
import ca.sfu.cmpt431.message.regular.RegularBorderMsg;
import ca.sfu.cmpt431.message.regular.RegularConfirmMsg;
import ca.sfu.cmpt431.message.regular.RegularUpdateNeighbourMsg;
import ca.sfu.network.MessageReceiver;
import ca.sfu.network.MessageSender;

public class Client {

	private static final int SERVER_PORT = 6560;
	private static final String SERVER_IP = "142.58.35.62";
	private Comrade  server;
	
	private int myPort;
	private String myIp;
	private MessageReceiver Receiver;
	private RegularConfirmMsg myConfirmMessage;
	
	private int status;
	private Outfits outfit;
	
	int neiUpdCount;

	public boolean[] up ;
	public boolean[] down;
	public boolean[] left;
	public boolean[] right;
	public boolean upperLeft;
	public boolean upperRight;
	public boolean lowerLeft;
	public boolean lowerRight;
	
	private int borderCount = 0;
	
	public Client() {
		while(true){
			Random r = new Random(); 
			myPort = r.nextInt(55535)+10000;
			try{
				System.out.println("trying port " + myPort);
				Receiver = new MessageReceiver(myPort);
				System.out.println("port " + myPort + "is ok");
				break;
			}
			catch(Exception e){
				System.out.println("port " + myPort + "is occupied");
			}
		}
	}
	
	public void startClient(String ip) throws IOException, InterruptedException {
		myIp = ip;
		MessageSender svsdr = new MessageSender(SERVER_IP, SERVER_PORT);
		server = new Comrade(MessageCodeDictionary.ID_SERVER, SERVER_PORT, SERVER_IP, svsdr);
		JoinRequestMsg Request = new JoinRequestMsg(myPort);
		server.sender.sendMsg(Request);
		status = 1;
		while(true){
			if(!Receiver.isEmpty()){
				System.out.println("status :" + status);
				Message msg = (Message) Receiver.getNextMessageWithIp().extracMessage();
				switch(status) {
//					case 0:
//						server.sender.sendMsg(new RegularConfirmMsg(-1));
//						status = 1;
//						break;
					case 1:
						repairOutfit((JoinOutfitsMsg) msg);
						if(outfit.neighbour.size() > 0){
							status = 2;
							neiUpdCount = 0;
						}
						else
							status = 3;
						break;
					case 2:
						neiUpdCount++;
						if(neiUpdCount == outfit.neighbour.size()){
							server.sender.sendMsg(myConfirmMessage);
							status = 3;
						}
						break;
					case 3:
						int msgType = msg.getMessageCode();
						if(msgType == MessageCodeDictionary.REGULAR_NEXTCLOCK) {
							sendBorderToNeighbours();
							if(isBorderMessageComplete())
								computeAndReport();
							else
								status = 4;
						}
						else if (msgType == MessageCodeDictionary.REGULAR_UPDATE_NEIGHBOUR)
							handleNeighbourUpdate((RegularUpdateNeighbourMsg)msg);
						else if (msgType == MessageCodeDictionary.JOIN_SPLIT) {
							handleSplit((JoinSplitMsg) msg);
							status = 5;
						}
						else if (msgType == MessageCodeDictionary.REGULAR_BORDER_EXCHANGE)
							handleBorderMessage((RegularBorderMsg) msg);
						break;
					case 4:
						handleBorderMessage((RegularBorderMsg) msg);
						if(isBorderMessageComplete()) {
							computeAndReport();
							status = 3;
						}
						break;
					case 5:
						if(msg.getMessageCode() != MessageCodeDictionary.REGULAR_CONFIRM){
							System.out.println(msg.getMessageCode());
							System.out.println("type error, expect confirm message");
						}
						else
							server.sender.sendMsg(myConfirmMessage);
						break;
					default:
						System.out.println("Received unexpectd message.");
						break;
				}
			}
		}
	}
	
	private void repairOutfit(JoinOutfitsMsg msg) throws IOException {
		System.out.println("received outfit");
		outfit = msg.yourOutfits;
		myConfirmMessage = new RegularConfirmMsg(outfit.myId);
		if(outfit.pair == null){
			System.out.println("aaaaaaa");
			server.sender.sendMsg(myConfirmMessage);
		}
		else {
			System.out.println("bbbbbb");
			outfit.pair.sender = new MessageSender(outfit.pair.ip, outfit.pair.port);
			outfit.pair.sender.sendMsg(myConfirmMessage);
		}
		for(Neighbour nei: outfit.neighbour) {
			if(nei.comrade.id == outfit.pair.id)
				nei.comrade.sender = outfit.pair.sender;
			else {
				nei.comrade.sender = new MessageSender(nei.comrade.ip, nei.comrade.port);
				ArrayList<Integer> mypos  = (ArrayList<Integer>) ClientHelper.ClientNeighbor(nei.position);
				nei.comrade.sender.sendMsg(
						new RegularUpdateNeighbourMsg(outfit.myId, mypos, myPort, myIp));
			}
		}
	}
	
	private void sendBorderToNeighbours() throws IOException {
		int neighborCount = outfit.neighbour.size();
		Border sendborder;	
		for(int j = 0; j < neighborCount; j++)
		{
				sendborder = new Border();
				sendborder.bits = getborder(outfit.neighbour.get(j).position);
				RegularBorderMsg sendbordermsg = new RegularBorderMsg(outfit.myId, sendborder);	
				outfit.neighbour.get(j).comrade.sender.sendMsg(sendbordermsg);

		}
	}
	
	private void handleNeighbourUpdate(RegularUpdateNeighbourMsg msg) throws IOException {
		boolean isOldFriend = false;
		for(Neighbour nei: outfit.neighbour){
			if(nei.comrade.id == msg.getClientId()) {
				nei.position.clear();
				for(Integer q: msg.pos) nei.position.add(q);
				isOldFriend = true;
			}
			else {
				for (Integer p: nei.position){
					for(Integer q: msg.pos)
						if(p == q) nei.position.remove(q);
				}
				if(nei.position.size() == 0){
					nei.comrade.sender.close();
					outfit.neighbour.remove(nei);
				}
			}
		}
		if(!isOldFriend) {
			Neighbour newnei = new Neighbour(msg.pos, 
					new Comrade(msg.getClientId(), msg.port, msg.ip, new MessageSender(msg.ip, msg.port)));
			outfit.neighbour.add(newnei);
		}
	}
	
	private void handleSplit(JoinSplitMsg msg) throws UnknownHostException, IOException {
		
		System.out.println("Handle split");
		List<Board> board;
		if (msg.splitMode == MessageCodeDictionary.SPLIT_MODE_VERTICAL)
		{
			board = BoardOperation.VerticalCut(outfit.myBoard);
		}
		else{
			board = BoardOperation.HorizontalCut(outfit.myBoard);
		}	
		outfit.myBoard = board.get(0);
		if(outfit.pair != null)
			outfit.pair.sender.close();
		 
		outfit.pair = new Comrade(msg.newcomerId, msg.newcomerPort, msg.newcomerIp, new MessageSender(msg.newcomerIp, msg.newcomerPort));
		Board pair_board;
		pair_board = board.get(1);
		Outfits pair_outfit = new Outfits(msg.newcomerId, outfit.nextClock, outfit.top, outfit.left, pair_board);
		Neighbour N10 = findNeiWithPos(10);
		Neighbour N11 = findNeiWithPos(11);
		Neighbour N0= findNeiWithPos(0);
		Neighbour N3 = findNeiWithPos(3);
		Neighbour N4 = findNeiWithPos(4);
		Neighbour N5= findNeiWithPos(5);
		Neighbour N1 = findNeiWithPos(1);
		Neighbour N2= findNeiWithPos(2);
		Neighbour N6 = findNeiWithPos(6);
		Neighbour N7= findNeiWithPos(7);
		Neighbour N8 = findNeiWithPos(8);
		Neighbour N9 = findNeiWithPos(9);
		if (msg.splitMode == MessageCodeDictionary.SPLIT_MODE_VERTICAL)
		{
			for(int i = 0; i < 7; i++)
				for( int j = 0; j < outfit.neighbour.size(); j++)
					if(outfit.neighbour.get(j).position.get(0) <= i){
						pair_outfit.neighbour.add(outfit.neighbour.get(j));
						break;
					}
//			MessageSender Sender = new MessageSender(InetAddress.getLocalHost().getHostAddress(), myPort);
			Comrade comerade = new Comrade(outfit.myId, myPort, myIp, null);
			ArrayList<Integer> position = new ArrayList<Integer>();
			position.add(1);
			position.add(2);
			Neighbour pair_neighbor = new Neighbour(position, comerade);
			ArrayList<Integer> mypos = new ArrayList<Integer>();
			mypos.add(7);
			mypos.add(8);
			Neighbour newneighbor = new Neighbour(mypos, comerade);
			pair_outfit.neighbour.add(newneighbor);
			
			for(int i = 9; i < 12; i++)
				for( int j = 0; j < outfit.neighbour.size(); j++)
					if(outfit.neighbour.get(j).position.get(0) <= i){
						pair_outfit.neighbour.add(outfit.neighbour.get(j));
						break;
					}		
			
			if(N11 != null && N10 != null && N10.comrade.id == N11.comrade.id)
			{
				if(N0.comrade.id != N11.comrade.id)
				{
					N0.comrade.sender.close();
					outfit.neighbour.remove(N0);
				}
				N10.position = new ArrayList<Integer>();
				N10.position.add(10);
				N10.position.add(11);
				N10.position.add(0);
				outfit.neighbour.add(N10);
			}
			if(N4 != null && N5 != null && N4.comrade.id == N5.comrade.id && N4 != null)
			{
				if(N3.comrade.id != N5.comrade.id)
				{
					N3.comrade.sender.close();
					outfit.neighbour.remove(N3);
				}
				N4.position = new ArrayList<Integer>();
				N4.position.add(3);
				N4.position.add(4);
				N4.position.add(5);
				outfit.neighbour.add(N4);
			}
			if(N1 != null && N2 != null && N1.comrade.id == N2.comrade.id && N1 != null)
			{
				if(N1.comrade.id != N0.comrade.id && N1.comrade.id != N3.comrade.id)
				{
					N1.comrade.sender.close();
					outfit.neighbour.remove(N1);
				}
//				pair_neighbor.position = new ArrayList<Integer>();
				N1.position.add(1);
				N1.position.add(2);
				outfit.neighbour.add(N1);
			}
			else{
				if(N1 != null && N0 != null && N1.comrade.id != N0.comrade.id && N1 != null)
				{
					N1.comrade.sender.close();
					outfit.neighbour.remove(N1);					
					N1.position.add(1);
					outfit.neighbour.add(N1);
					
					
				}
				if(N2 != null && N3 != null && N2.comrade.id != N3.comrade.id && N2 != null)
				{
					N2.comrade.sender.close();
					outfit.neighbour.remove(N2);
					N2.position.add(2);
					outfit.neighbour.add(N2);
				}
			}
			
		}
		else{
			for(int i = 0; i < 4; i++)
				for( int j = 0; j < outfit.neighbour.size(); j++)
					if(outfit.neighbour.get(j).position.get(0) <= i){
						pair_outfit.neighbour.add(outfit.neighbour.get(j));
						break;
					}
//			MessageSender Sender = new MessageSender(InetAddress.getLocalHost().getHostAddress(), myPort);
			Comrade comerade = new Comrade(outfit.myId, myPort, myIp, null); 
			ArrayList<Integer> mypos = new ArrayList<Integer>();
			mypos.add(4);
			mypos.add(5);
			Neighbour newneighbor = new Neighbour(mypos, comerade);
			pair_outfit.neighbour.add(newneighbor);
			for(int i = 5; i < 12; i++)
				for( int j = 0; j < outfit.neighbour.size(); j++)
					if(outfit.neighbour.get(j).position.get(0) <= i){
						pair_outfit.neighbour.add(outfit.neighbour.get(j));
						break;
					}
			
			if(N1 != null && N2 != null && N1.comrade.id == N2.comrade.id && N1 != null)
			{
				if(N1.comrade.id != N0.comrade.id)
				{
					N0.comrade.sender.close();
					outfit.neighbour.remove(N0);
				}
				N1.position = new ArrayList<Integer>();
				N1.position.add(1);
				N1.position.add(2);
				N1.position.add(0);
				outfit.neighbour.add(N1);
			}
			if(N7 != null && N8 != null && N7.comrade.id == N8.comrade.id && N7 != null)
			{
				if(N9.comrade.id != N7.comrade.id)
				{
					N9.comrade.sender.close();
					outfit.neighbour.remove(N9);
				}
				N4.position = new ArrayList<Integer>();
				N4.position.add(3);
				N4.position.add(4);
				N4.position.add(5);
				outfit.neighbour.add(N4);
			}
			if(N11 != null && N10 != null && N10.comrade.id == N11.comrade.id && N10 != null)
			{
				if(N11.comrade.id != N0.comrade.id && N11.comrade.id != N9.comrade.id)
				{
					N11.comrade.sender.close();
					outfit.neighbour.remove(N11);
				}
//				pair_neighbor.position = new ArrayList<Integer>();
				N11.position.add(11);
				N11.position.add(10);
				outfit.neighbour.add(N11);
			}
			else{
				if(N11 != null && N0 != null && N11.comrade.id != N0.comrade.id && N0 != null)
				{
					N11.comrade.sender.close();
					outfit.neighbour.remove(N11);					
					N11.position.add(11);
					outfit.neighbour.add(N11);
					
				}
				if(N9 != null && N10 != null && N10.comrade.id != N9.comrade.id && N10 != null)
				{
					N10.comrade.sender.close();
					outfit.neighbour.remove(N10);
					N10.position.add(10);
					outfit.neighbour.add(N10);
				}
			}
		}
		pair_outfit.pair = new Comrade(outfit.myId, myPort, myIp, null);
		outfit.pair.sender.sendMsg(new JoinOutfitsMsg(outfit.myId, myPort, pair_outfit));
		System.out.println("split_ID"+pair_outfit.myId);
		
	}
	
	private void handleBorderMessage(RegularBorderMsg msg) {
		int cid = msg.getClientId();
		int nei_id = -1;
		borderCount++;
		
		for(int i=0; i<outfit.neighbour.size(); i++){
			if(outfit.neighbour.get(i).comrade.id == cid){
				nei_id = i;
				break;
			}
		}
		
		//merge and update the global border array/variable
		mergeBorder(msg.boarder.bits, outfit.neighbour.get(nei_id).position);
	}
	
	private boolean isBorderMessageComplete() {
		if(borderCount == outfit.neighbour.size())
			return true;
		return false;
	}
	
	private void computeAndReport() throws IOException {
		BoardOperation.NextMoment(outfit.myBoard, null, null, null, null, false, false, false, false);
		server.sender.sendMsg(new RegularBoardReturnMsg(outfit.myId, 0, 0, outfit.myBoard));
		borderCount = 0;
	}
	
	private Neighbour findNeiWithPos(int pos) {
		for(Neighbour nei: outfit.neighbour)
			for(Integer i: nei.position)
				if(i == pos)
					return nei;
		return null;
	}
	
	private void sendMessageTo(int cid, Message msg) throws IOException {
		for(Neighbour nei: outfit.neighbour) {
			if(nei.comrade.id == cid)
				nei.comrade.sender.sendMsg(msg);
		}
	}
	
	
	
//	public void startClientOld() throws IOException, ClassNotFoundException, InterruptedException
//	{
//		while(true){
//			if(!Receiver.isEmpty()){
//				System.out.println(status);
//				
//				switch(status) {
//					case MessageCodeDictionary.ORIGINAL_STATUS:	
//						Receiver.getNextMessageWithIp().extracMessage();
//						server.sender.sendMsg(confirm);						
//						status = MessageCodeDictionary.GET_CLIENT_ID_STATUS;
//						break;
//					//wait for cid
//					case MessageCodeDictionary.GET_CLIENT_ID_STATUS:
//						
//						JoinOutfitsMsg ob;
//						JoinOutfitsMsg joinmsg;
//						msgIp = Receiver.getNextMessageWithIp();
//						joinmsg = (JoinOutfitsMsg)msgIp.extracMessage();
//						outfit = joinmsg.yourOutfits;
//						joinmsg = (JoinOutfitsMsg)ob;
//						cid = outfit.myId;
//						pair_id = joinmsg.getClientId();
//						int pair_port = joinmsg.myPort;
//						if(pair_port <0){
//							server.sender.sendMsg(confirm);
//							System.out.println(pair_id);
//						}
//						else{
//							String pair_ip = msgIp.getIp().substring(1); 
//							MessageSender Sender2 = new MessageSender(pair_ip, pair_port);
//							comrade[pair_id] = new Comrade(pair_id, pair_port, pair_ip, Sender2);							
//							comrade[pair_id].sender.sendMsg(confirm);
//						}
//						int i = 0;
//						int[] position;
//						int j = 0;
//						int p = 0;
//						for(i=0;i<12;i++)
//						{
//							while(outfit.neighbour.get(j) != null){
//								for (p = 0; p < outfit.neighbour.get(j).position.length; p++ ){
//									if(i == outfit.neighbour.get(j).position[p]){
//										MessageSender Sender = new MessageSender(outfit.neighbour.get(i).comrade.ip, pair_port);
//										comrade[pair_id] = new Comrade(pair_id, pair_port, outfit.neighbour.get(i).comrade.ip, Sender);
//										int[] mypos;
//										mypos = ClientHelper.ClientNeighbor(outfit.neighbour.get(j).position);
//										RegularUpdateNeighbourMsg neighbor = new RegularUpdateNeighbourMsg(cid, mypos, port, InetAddress.getLocalHost().getHostAddress());
//										comrade[pair_id].sender.sendMsg(neighbor);
//									}
//									
//								}
//								j++;
//							}
//																
//						}
//						status = MessageCodeDictionary.RECEIVE_NEIGHBORS_CONFIRM_STATUS;
//						break;
//					receive neighbor's confirm
//					case MessageCodeDictionary.RECEIVE_NEIGHBORS_CONFIRM_STATUS:
//						int neighborupdateconfirmCount = 0;	
//						
//						Receiver.getNextMessageWithIp();
//						neighborupdateconfirmCount++;
//						if(neighborupdateconfirmCount == outfit.neighbour.size())
//						{
//							server.sender.sendMsg(confirm);
//							status = MessageCodeDictionary.WAIT_FOR_COMMAND_STATUS;
//						}
//						else 
//							status = MessageCodeDictionary.RECEIVE_NEIGHBORS_CONFIRM_STATUS;
//						break;
//					//wait for start or other commands
//					case MessageCodeDictionary.WAIT_FOR_COMMAND_STATUS:								
//						msgIp = Receiver.getNextMessageWithIp();
//						Message msg = (Message)msgIp.extracMessage();
//						int msg_type;
//						msg_type = msg.getMessageCode();
//						if(msg_type == MessageCodeDictionary.REGULAR_NEXTCLOCK)
//							status = MessageCodeDictionary.SEND_BORDER_STATUS;
//						else if (msg_type == MessageCodeDictionary.REGULAR_UPDATE_NEIGHBOUR)
//							status = MessageCodeDictionary.UPDATE_NEIGHBOR_STATUS;
//						else if (msg_type == MessageCodeDictionary.JOIN_SPLIT)
//							status = MessageCodeDictionary.SPLIT_STATUS;
//						else
//							status = MessageCodeDictionary.REVEIVE_BORDER_OR_CONFIRM_STATUS;
//						break;
//					//update neighbor
//					case MessageCodeDictionary.UPDATE_NEIGHBOR_STATUS:
//						msgIp = Receiver.getNextMessageWithIp();
//						RegularUpdateNeighbourMsg neighbormsg = (RegularUpdateNeighbourMsg)msgIp.extracMessage();
//						
//						j = 0;
//						for(i=0;i<12;i++)
//						{
//							while(outfit.neighbour.get(j) != null){
//								for (p = 0; p < outfit.neighbour.get(j).position.length; p++ ){
//									if(i == outfit.neighbour.get(j).position[p]){
//										outfit.neighbour.get(j).comrade.id = neighbormsg.getClientId();
//									}									
//								}
//								j++;
//							}																
//						}
//
//						String neighbor_ip = neighbormsg.ip;
//						int neighbor_port = neighbormsg.port;
//						MessageSender Sender = new MessageSender(neighbor_ip, neighbor_port);
//						comrade[neighbormsg.getClientId()] = new Comrade(neighbormsg.getClientId(), neighbor_port, neighbor_ip,  Sender);
//						comrade[neighbormsg.getClientId()].sender.sendMsg(confirm);
//						status = MessageCodeDictionary.WAIT_FOR_COMMAND_STATUS;
//						break;
//					//start and send border
//					case MessageCodeDictionary.SEND_BORDER_STATUS:
//						RegularNextClockMsg clock = (RegularNextClockMsg)msgIp.extracMessage();
//						myboard = new Board(outfit.myBoard.height,outfit.myBoard.width);
//
//						
//						status = MessageCodeDictionary.REVEIVE_BORDER_OR_CONFIRM_STATUS;
//						break;
//					//receive border or confirm
//					case MessageCodeDictionary.REVEIVE_BORDER_OR_CONFIRM_STATUS:
//						msgIp = Receiver.getNextMessageWithIp();
//						Message msgtogetcode = (Message)msgIp.extracMessage();
//						msg_type = msgtogetcode.getMessageCode();
//						int confirmCount = 0;
//						int borderexchangeCount = 0;
//						int[] posRecord = null;
//						if(msg_type == MessageCodeDictionary.REGULAR_BORDER_EXCHANGE)
//						{
//							RegularBorderMsg neighborbordermsg = (RegularBorderMsg)msgIp.extracMessage();
//							borderexchangeCount++;
//							for(j = 0; j<outfit.neighbour.size(); j++)
//							{
//								if(neighborbordermsg.getClientId() == outfit.neighbour.get(j).comrade.id)
//									posRecord = outfit.neighbour.get(j).position;
//									
//							}
//							mergeBorder(neighborbordermsg.boarder.bits, posRecord);
//							
//						}
//						else if (msg_type == MessageCodeDictionary.REGULAR_CONFIRM)
//						{
//							confirmCount++;
//						}
//						if(borderexchangeCount == outfit.neighbour.size() && borderexchangeCount == outfit.neighbour.size())
//								status = MessageCodeDictionary.COMPUTING_STATUS;
//						else 
//							status = MessageCodeDictionary.REVEIVE_BORDER_OR_CONFIRM_STATUS;
//						break;
//										
//					// split
//					case MessageCodeDictionary.SPLIT_STATUS:
//						msg msg = (msg)msgIp.extracMessage();
//						List<Board> board;
//						if (msg.splitMode == MessageCodeDictionary.SPLIT_MODE_VERTICAL)
//						{
//							board = BoardOperation.VerticalCut(myboard);
//						}
//						else{
//							board = BoardOperation.HorizontalCut(myboard);
//						}	
//						myboard = board.get(0);
//						MessageSender Sender3 = new MessageSender(msg.newcomerIp, msg.newcomerPort);
//						comrade[msg.newcomerId] = new Comrade(msg.newcomerId, msg.newcomerPort, msg.newcomerIp, Sender3);
//						Outfits pair_outfit = new Outfits(pair_id, outfit.nextClock, outfit.top, outfit.left, myboard.height, myboard.width);
//						
//						JoinOutfitsMsg JOM = new JoinOutfitsMsg(cid, port, pair_outfit);
//						comrade[msg.newcomerId].sender.sendMsg(JOM);
//						status = MessageCodeDictionary.WAIT_FOR_PAIR_CONFIRM_STATUS;
//						break;
//					//wait for pair's confirm
//					case MessageCodeDictionary.WAIT_FOR_PAIR_CONFIRM_STATUS:
//						msgIp = Receiver.getNextMessageWithIp();
//						server.sender.sendMsg(confirm);
//						status = MessageCodeDictionary.WAIT_FOR_COMMAND_STATUS;
//						break;
//					//computing
//					case MessageCodeDictionary.COMPUTING_STATUS:
//						myboard = BoardOperation.NextMoment(myboard, up, down, left, right, upperLeft, upperRight, lowerLeft, lowerRight);
//						RegularBoardReturnMsg boardreturnmsg = new RegularBoardReturnMsg(cid,myboard);
//						server.sender.sendMsg(boardreturnmsg);
//						status = MessageCodeDictionary.WAIT_FOR_COMMAND_STATUS;
//						break;
//					default:
//						break;
//				}
//			}
//		}
//		
//	}
	
	protected boolean[] getborder(List<Integer> array){
		
		Board b = outfit.myBoard;
		ArrayList<Boolean> al = new ArrayList<Boolean>();
		
		int j;
		for(int i=0; i<array.size(); i++){
			int a = array.get(i);
			switch(a+1){
			case 1:
				if(al.size()!=0)
					break;
				al.add(b.bitmap[0][0]);
				break;
			case 2:
				j = 0;
				//if it is 1 already
				if(al.size() != 0)
					j = 1;
				for(; j<=b.width/2; j++)
					al.add(b.bitmap[0][j]);
				break;
			case 3:
				//if it is 2 already
				j = b.width/2-1;
				if(al.size() != 0)
					j=j+2;
				
				for(; j<b.width; j++)
					al.add(b.bitmap[0][j]);
				break;
			case 4:
				//if it is 3 already
				if(al.size() != 0)
					break;
				al.add(b.bitmap[0][b.width-1]);
				break;
			case 5:
				j = 0;
				//if it is 4 already
				if(al.size()!=0)
					j=1;
				for(; j<=b.height/2; j++)
					al.add(b.bitmap[j][b.width-1]);
				break;
			case 6:
				j = b.height/2-1;
				//if it is 5 already
				if(al.size()!=0)
					j=j+2;
				for(; j<b.height; j++)
					al.add(b.bitmap[j][b.width-1]);
				break;
			case 7:
				//if it is 6 already
				if(al.size()!=0)
					break;
				al.add(b.bitmap[b.height-1][b.width-1]);
				break;
			case 8:
				j = b.width - 1;
				//if it is 7 already
				if(al.size()!=0)
					j--;
				for(; j>=b.width/2-1; j--)
					al.add(b.bitmap[b.height-1][j]);
				break;
			case 9:
				j = b.width/2;
				if(al.size()!=0)
					j=j-2;
				for(; j>=0; j--)
					al.add(b.bitmap[b.height-1][j]);
				break;
			case 10:
				if(al.size()!=0)
					break;
				al.add(b.bitmap[b.height-1][0]);
				break;
			case 11:
				j = b.height-1;
				if(al.size()!=0)
					j--;
				for(; j>=b.height/2-1; j--)
					al.add(b.bitmap[j][0]);
				break;
			case 12:
				j = b.height/2;
				if(al.size()!=0)
					j=j-2;
				for(; j>=0; j--)
					al.add(b.bitmap[j][0]);
				break;
			}
		}
		
		boolean[] a = new boolean[al.size()];
		for(int k=0; k<a.length; k++){
			a[k]=(boolean)al.get(k);
		}
		
		return a;
	}

	//comment
	protected void mergeBorder(boolean[] aa, List<Integer> array1){
		ArrayList<Boolean> tmp = new ArrayList<Boolean>();
		
		Board b = outfit.myBoard;

		for(int k=0; k<aa.length; k++)
			tmp.add(aa[k]);
		
		for(int i=0; i<array1.size(); i++){
			if(tmp.size()==0)
				break;
			
			int num = array1.get(i);
			switch(num+1){
			case 1:
				upperLeft = (boolean) tmp.get(0);
				tmp.remove(0);
				break;
			case 2:
				for(int p=0; p<b.width/2; p++){
					up[p] = (boolean)tmp.get(0);
					tmp.remove(0);
				}
				break;
			case 3:
				for(int p=b.width/2; p<b.width; p++){
					up[p] = (boolean)tmp.get(0);
					tmp.remove(0);
				}
				break;
			case 4:
				upperRight = (boolean)tmp.get(0);
				tmp.remove(0);
				break;
			case 5:
				for(int p=0; p<b.height/2; p++){
					right[p] = (boolean)tmp.get(0);
					tmp.remove(0);
				}
				break;
			case 6:
				for(int p=b.height/2; p<b.height; p++){
					right[p] = (boolean)tmp.get(0);
					tmp.remove(0);
				}
				break;
			case 7:
				lowerRight = (boolean)tmp.get(0);
				tmp.remove(0);
				break;
			case 8:
				for(int p=b.width-1; p>=b.width/2; p--){
					down[p] = (boolean)tmp.get(0);
					tmp.remove(0);
				}
				break;
			case 9:
				for(int p=b.width/2-1; p>=0; p--){
					down[p] = (boolean)tmp.get(0);
					tmp.remove(0);
				}
				break;
			case 10:
				lowerLeft = (boolean)tmp.get(0);
				tmp.remove(0);
				break;
			case 11:
				for(int p=b.height-1; p>=b.height/2; p--){
					left[p] = (boolean)tmp.get(0);
					tmp.remove(0);
				}
				break;
			case 12:
				for(int p=b.height/2-1; p>=0; p--){
					left[p] = (boolean)tmp.get(0);
					tmp.remove(0);
				}
				break;
			}
		}
	}
	
}
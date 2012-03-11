package ca.sfu.client;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
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
	
	public void startClient() throws IOException, InterruptedException {
		MessageSender svsdr = new MessageSender(SERVER_IP, SERVER_PORT);
		server = new Comrade(MessageCodeDictionary.ID_SERVER, SERVER_PORT, SERVER_IP, svsdr);
		JoinRequestMsg Request = new JoinRequestMsg(myPort);
		server.sender.sendMsg(Request);
		status = 0;
		while(true){
			if(!Receiver.isEmpty()){
				Message msg = (Message) Receiver.getNextMessageWithIp().extracMessage();
				System.out.println("Entreing status: " + status);
				switch(status) {
					case 0:
						server.sender.sendMsg(new RegularConfirmMsg(-1));
						status = 1;
						break;
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
						if(msg.getMessageCode() != MessageCodeDictionary.REGULAR_CONFIRM)
							System.out.println("type error, expect confirm message");
						else
							sendMessageTo(msg.getClientId(), myConfirmMessage);
						break;
					default:
						System.out.println("Received unexpectd message.");
						break;
				}
			}
		}
	}
	
	private void repairOutfit(JoinOutfitsMsg msg) throws IOException {
		outfit = msg.yourOutfits;
		myConfirmMessage = new RegularConfirmMsg(outfit.myId);
		if(outfit.pair == null) 
			server.sender.sendMsg(myConfirmMessage);
		else {
			outfit.pair.sender = new MessageSender(outfit.pair.ip, outfit.pair.port);
			outfit.pair.sender.sendMsg(myConfirmMessage);
		}
		for(Neighbour nei: outfit.neighbour) {
			nei.comrade.sender = new MessageSender(nei.comrade.ip, nei.comrade.port);
			ArrayList<Integer> mypos  = ClientHelper.ClientNeighbor(nei.position);
			nei.comrade.sender.sendMsg(
					new RegularUpdateNeighbourMsg(outfit.myId, mypos, myPort, InetAddress.getLocalHost().getHostAddress()));
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
		int oldPos = -1;
		for(int j = 0; j < outfit.neighbour.size(); j++){
			for (int p = 0; p < outfit.neighbour.get(j).position.size(); p++ ){
				for(int i = 0; i < msg.pos.size(); i++){
					if(msg.pos.get(i) == outfit.neighbour.get(j).position.get(p) ){
						outfit.neighbour.get(j).position.remove(p);
					}
				}									
			}
			if(outfit.neighbour.get(j).comrade.id != msg.getClientId())
				outfit.neighbour.remove(j);
			else
				oldPos = j;
		}
		if(oldPos < 0) {
			MessageSender sender = new MessageSender(msg.ip, msg.port);
			Comrade comerade = new Comrade(msg.getClientId(), msg.port, msg.ip, sender); 
			Neighbour newneighbor = new Neighbour(msg.pos, comerade);
			outfit.neighbour.add(newneighbor);
		}
		else {
			for(Integer i: msg.pos)
				outfit.neighbour.get(oldPos).position.add(i);
		}
		
	}
	
	private void handleSplit(JoinSplitMsg msg) {
		
	}
	
	private void handleBorderMessage(RegularBorderMsg msg) {
		
	}
	
	private boolean isBorderMessageComplete() {
		return true;
	}
	
	private void computeAndReport() throws IOException {
		BoardOperation.NextMoment(outfit.myBoard, null, null, null, null, false, false, false, false);
		server.sender.sendMsg(new RegularBoardReturnMsg(outfit.myId, 0, 0, outfit.myBoard));
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
//						JoinSplitMsg joinsplitmsg = (JoinSplitMsg)msgIp.extracMessage();
//						List<Board> board;
//						if (joinsplitmsg.splitMode == MessageCodeDictionary.SPLIT_MODE_VERTICAL)
//						{
//							board = BoardOperation.VerticalCut(myboard);
//						}
//						else{
//							board = BoardOperation.HorizontalCut(myboard);
//						}	
//						myboard = board.get(0);
//						MessageSender Sender3 = new MessageSender(joinsplitmsg.newcomerIp, joinsplitmsg.newcomerPort);
//						comrade[joinsplitmsg.newcomerId] = new Comrade(joinsplitmsg.newcomerId, joinsplitmsg.newcomerPort, joinsplitmsg.newcomerIp, Sender3);
//						Outfits pair_outfit = new Outfits(pair_id, outfit.nextClock, outfit.top, outfit.left, myboard.height, myboard.width);
//						
//						JoinOutfitsMsg JOM = new JoinOutfitsMsg(cid, port, pair_outfit);
//						comrade[joinsplitmsg.newcomerId].sender.sendMsg(JOM);
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
	
	protected boolean[] getborder(int[] array){
		
		Board b = outfit.myBoard;
		ArrayList<Boolean> al = new ArrayList<Boolean>();
		
		int j;
		for(int i=0; i<array.length; i++){
			int a = array[i];
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
	protected void mergeBorder(boolean[] aa, int[] array1){
		ArrayList<Boolean> tmp = new ArrayList<Boolean>();
		
		Board b = outfit.myBoard;

		for(int k=0; k<aa.length; k++)
			tmp.add(aa[k]);
		
		for(int i=0; i<array1.length; i++){
			if(tmp.size()==0)
				break;
			
			int num = array1[i];
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
package ca.sfu.client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import ca.sfu.cmpt431.message.join.JoinRequestMsg;
import ca.sfu.cmpt431.message.join.JoinSplitMsg;
import ca.sfu.cmpt431.message.leave.LeaveReceiverMsg;
import ca.sfu.cmpt431.message.merge.MergeLastMsg;
import ca.sfu.cmpt431.message.merge.MergeOutfit;
import ca.sfu.cmpt431.message.regular.RegularBoardReturnMsg;
import ca.sfu.cmpt431.message.regular.RegularBorderMsg;
import ca.sfu.cmpt431.message.regular.RegularConfirmMsg;
import ca.sfu.cmpt431.message.regular.RegularOutfitMsg;
import ca.sfu.cmpt431.message.regular.RegularUpdateNeighbourMsg;
import ca.sfu.network.MessageReceiver;
import ca.sfu.network.MessageSender;

public class Client {

	private static final int SERVER_PORT = 6560;
	private String SERVER_IP;
	private Comrade  server;
	
	private boolean TEST_MODE = true;
	private boolean DEBUG_MODE = true;
	private boolean RANDOM = true;
	
	private long t_lastend;
	private long t_start;
	private long t_bdfnsh;
	private long t_cmptfnsh;
	
	
	private int myPort;
	private String myIp;
	private MessageReceiver Receiver;
	private RegularConfirmMsg myConfirmMessage;
	
	private int status;
	private Outfits outfit;
	
	private int neiUpdCount;
	private int borderCount = 0;
	private boolean isleaving = false;
	
	private Message tmpmsg;

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
				System.out.println("port " + myPort + " is ok");
				break;
			}
			catch(Exception e){
				System.out.println("port " + myPort + " is occupied");
			}
		}
	}
	
	public void startClient(String sip, String myip) throws IOException, InterruptedException {
		SERVER_IP = sip;
		myIp = myip;
		MessageSender svsdr = new MessageSender(SERVER_IP, SERVER_PORT);
		server = new Comrade(MessageCodeDictionary.ID_SERVER, SERVER_PORT, SERVER_IP, svsdr);
		JoinRequestMsg Request = new JoinRequestMsg(myPort);
		server.sender.sendMsg(Request);
		status = 1;
		while(true){
			if(!Receiver.isEmpty()){
				System.out.println("\nBefore fetching a message");
				calcPower(10000000);
				Message msg = (Message) Receiver.getNextMessageWithIp().extracMessage();
				if(outfit != null)
					System.out.println("status:" + status + "  messgetype:"+ msg.getMessageCode() + "  from: " + msg.getClientId());
				switch(status) {
					case 1:
						repairOutfit((RegularOutfitMsg) msg);
						if(neiUpdCount > 0)
							status = 2;
						else {
							server.sender.sendMsg(myConfirmMessage);
							status = 3;
						}
						break;
					case 2:
						neiUpdCount--;
						if(neiUpdCount <= 0){
							server.sender.sendMsg(myConfirmMessage);
							status = 3;
						}
						break;
					case 3:
						int msgType = msg.getMessageCode();
						if(msgType == MessageCodeDictionary.REGULAR_NEXTCLOCK) {
//							sendBorderToNeighbours();
							
							if(DEBUG_MODE)
								t_start = System.currentTimeMillis();
							
							if(isBorderMessageComplete()) {
								computeAndReport();
								if(isleaving)
									status = 6;
								else
									status = 3;
							}
							else
								status = 4;
						}
						else if (msgType == MessageCodeDictionary.JOIN_SPLIT)
							handleSplit((JoinSplitMsg) msg);
						else if (msgType == MessageCodeDictionary.REGULAR_BORDER_EXCHANGE)
							handleBorderMessage((RegularBorderMsg) msg);
						else 
							handleAbnomorlSituation(msg);
						
						break;
					case 4:
						handleBorderMessage((RegularBorderMsg) msg);
						if(isBorderMessageComplete()) {
							computeAndReport();
							if(isleaving)
								status = 6;
							else
								status = 3;
						}
						break;
					case 6:
						int msgTp = msg.getMessageCode();
						if(msgTp == MessageCodeDictionary.LEAVE_RECEIVER) {
							handleleaveReceiverMsg((LeaveReceiverMsg) msg);
						}
						else 
							handleAbnomorlSituation(msg);
						
						break;
					case 7:
						if(msg.getMessageCode() != MessageCodeDictionary.REGULAR_CONFIRM)
							System.out.println("type error, expect confirm message, received: " + msg.getMessageCode() + "  from " + msg.getClientId());
						else
							server.sender.sendMsg(myConfirmMessage);
						if(isleaving)
							status = 6;
						else
							status = 1;
						break;
					case 8:
						neiUpdCount--;
						if(neiUpdCount <= 0) {
							finishMerge();
							if(isleaving)
								status = 6;
							else
								status = 3;
						}
						break;
					default:
						System.out.println("Received unexpectd message.");
						break;
				}
				
				System.out.println("After handling the message");
				calcPower(10000000);
			}
		}
	}
	
	private void handleAbnomorlSituation(Message msg) throws IOException {
		int msgType = msg.getMessageCode();
		if (msgType == MessageCodeDictionary.REGULAR_BORDER_EXCHANGE)
			handleBorderMessage((RegularBorderMsg) msg);
		else if (msgType == MessageCodeDictionary.REGULAR_UPDATE_NEIGHBOUR) {
			handleNeighbourUpdate((RegularUpdateNeighbourMsg)msg);
		}
		else if (msgType == MessageCodeDictionary.MERGE_LAST) {
			passOutfitsToPair((MergeLastMsg)msg);
			status = 7;
		}
		else if (msgType == MessageCodeDictionary.MERGE_OUTFIT) {
			MergeOutfit mmsg = (MergeOutfit)msg;
			handleMerge(mmsg.lastfit, mmsg.yourPair);
			tmpmsg = mmsg;
			if(neiUpdCount > 0)
				status = 8;
			else
				finishMerge();
		}
		else
			System.out.println("Received unexpectd message.");
	}
	
	private void handleleaveReceiverMsg(LeaveReceiverMsg msg) throws IOException {
		for(Neighbour nei: outfit.neighbour)
			nei.comrade.sender = null;
		if(outfit.pair != null)
			outfit.pair.sender = null;
		if(msg.rcvid >= 0) {
			MessageSender sender = new MessageSender(msg.rcvip, msg.rcvport);
			sender.sendMsg(new RegularOutfitMsg(outfit.myId, outfit));
		}
		System.exit(0);
	}
	
	private void finishMerge() throws IOException{
		outfit.pair.sender.sendMsg(myConfirmMessage);
		MergeOutfit mmsg = (MergeOutfit) tmpmsg;
		if(hasNeighbour(outfit, mmsg.yourPair))
			outfit.pair = findNeiWithId(outfit, mmsg.yourPair).comrade;
		else if(mmsg.yourPair >= 0) {
			if(!hasNeighbour(outfit, outfit.pair.id))
				outfit.pair.sender.close();
			outfit.pair = new Comrade(mmsg.yourPair, mmsg.pairPort, mmsg.pairIp, 
					new MessageSender(mmsg.pairIp, mmsg.pairPort));
		}
		else
			outfit.pair = null;
		
		System.out.println("After merging:");
		outiftInfo(outfit);
	}
	
	private void passOutfitsToPair(MergeLastMsg msg) throws IOException {
		MessageSender sender = outfit.pair.sender;
		outfit.pair.sender = null;
		for(Neighbour nei: outfit.neighbour)
			nei.comrade.sender = null;
		sender.sendMsg(new MergeOutfit(outfit.myId, outfit, msg.newpair, msg.pairIp, msg.pairPort));
	}
	
	private void repairOutfit(RegularOutfitMsg msg) throws IOException {
		outfit = msg.yourOutfits;
		myConfirmMessage = new RegularConfirmMsg(outfit.myId);
		if(outfit.pair == null)
			outfit.pair = new Comrade(MessageCodeDictionary.ID_SERVER, SERVER_PORT, SERVER_IP, server.sender);
		else
			outfit.pair.sender = new MessageSender(outfit.pair.ip, outfit.pair.port);
//		for(Neighbour nei: outfit.neighbour) {
//			nei.comrade.sender = new MessageSender(nei.comrade.ip, nei.comrade.port);
//			ArrayList<Integer> mypos  = (ArrayList<Integer>) ClientHelper.ClientNeighbor(nei.position);
//			nei.comrade.sender.sendMsg(new RegularUpdateNeighbourMsg(outfit.myId, mypos, myPort, myIp, msg.getClientId()));
//			neiUpdCount ++;
//			if(nei.comrade.id == outfit.pair.id)
//				nei.comrade.sender = outfit.pair.sender;
//		}
		
		if(RANDOM && outfit.myBoard.bitmap == null)
			outfit.myBoard.bitmap = new boolean[outfit.myBoard.height][outfit.myBoard.width];
		
		
		up = new boolean[outfit.myBoard.width];
		down = new boolean[outfit.myBoard.width];
		left = new boolean[outfit.myBoard.height];
		right = new boolean[outfit.myBoard.height];
		System.out.println("received outfit:");
		outiftInfo(outfit);
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
	
	private void handleMerge(Outfits pout, int newpairid) throws IOException {
		if(pout.myId < outfit.myId)
			outfit.myId = pout.myId;;
		
		Neighbour [] pn = new Neighbour[12];
		for(int i = 0; i < 12; i++)
			pn[i] = findNeiWithPos(pout, i);
		
		Neighbour [] n = new Neighbour[12];
		for(int i = 0; i < 12; i++)
			n[i] = findNeiWithPos(outfit, i);
		
		Board newboard;
		
		// reverse of vertical split
		if(pout.top == outfit.top) {
			newboard = new Board(outfit.myBoard.height, outfit.myBoard.width + pout.myBoard.width);
			if(outfit.left < pout.left) {
				BoardOperation.Merge(newboard, outfit.myBoard, 0, 0);
				BoardOperation.Merge(newboard, pout.myBoard, 0, outfit.myBoard.width);
				
				if(n[1] != null && n[1].comrade.id != pn[1].comrade.id) {
					deletePos(outfit, n[1], 2);
					addPos(n[3], 2, true);
				}
				if(n[8] != null && n[8].comrade.id != pn[8].comrade.id) {
					deletePos(outfit, n[8], 7);
					addPos(n[6], 7, false);
				}
				for(int i = 3; i <= 6; i++) {
					if(n[i] != null && hasNeighbour(outfit, n[i].comrade.id)) {
						deletePos(outfit, n[i], i);
					}
					if(pn[i] != null) {
						if(hasNeighbour(pout, pn[i].comrade.id)) {
							outfit.neighbour.add(pn[i]);
							pout.neighbour.remove(pn[i]);
							repairNeighbour(pn[i]);
						}
					}
				}
			}
			else {
				outfit.left = pout.left;
				BoardOperation.Merge(newboard, pout.myBoard, 0, 0);
				BoardOperation.Merge(newboard, outfit.myBoard, 0, pout.myBoard.width);
				
				if(n[1] != null && n[1].comrade.id != pn[1].comrade.id) {
					deletePos(outfit, n[1], 1);
					addPos(n[0], 1, true);
				}
				if(n[8] != null && n[8].comrade.id != pn[8].comrade.id) {
					deletePos(outfit, n[8], 8);
					addPos(n[9], 8, false);
				}
				for(int i = 9; i <= 12; i++) {
					if(n[i%12] != null && hasNeighbour(outfit, n[i%12].comrade.id)) {
						deletePos(outfit, n[i%12], i%12);
					}
					if(pn[i%12] != null) {
						if(hasNeighbour(pout, pn[i%12].comrade.id)) {
							outfit.neighbour.add(pn[i%12]);
							pout.neighbour.remove(pn[i%12]);
							repairNeighbour(pn[i%12]);
						}
					}
				}
			}
		}
		// reverse of horizontal split
		else {
			newboard = new Board(outfit.myBoard.height + pout.myBoard.height, outfit.myBoard.width);
			if(outfit.top < pout.top){
				BoardOperation.Merge(newboard, outfit.myBoard, 0, 0);
				BoardOperation.Merge(newboard, pout.myBoard, outfit.myBoard.height, 0);
				
				if(n[4] != null && n[4].comrade.id != pn[4].comrade.id) {
					deletePos(outfit, n[4], 5);
					addPos(n[6], 5, true);
				}
				if(n[11] != null && n[11].comrade.id != pn[11].comrade.id) {
					deletePos(outfit, n[11], 10);
					addPos(n[9], 10, false);
				}
				for(int i = 6; i <= 9; i++) {
					if(n[i] != null && hasNeighbour(outfit, n[i].comrade.id)) {
						deletePos(outfit, n[i], i);
					}
					if(pn[i] != null) {
						if(hasNeighbour(pout, pn[i].comrade.id)) {
							outfit.neighbour.add(pn[i]);
							pout.neighbour.remove(pn[i]);
							repairNeighbour(pn[i]);
						}
					}
				}
			}
			else {
				outfit.top = pout.top;
				BoardOperation.Merge(newboard, pout.myBoard, 0, 0);
				BoardOperation.Merge(newboard, outfit.myBoard, pout.myBoard.height, 0);
				outfit.myBoard = newboard;
				
				if(n[4] != null && n[4].comrade.id != pn[4].comrade.id) {
					deletePos(outfit, n[4], 4);
					addPos(n[3], 4, true);
				}
				if(n[11] != null && n[11].comrade.id != pn[11].comrade.id) {
					deletePos(outfit, n[11], 11);
					addPos(n[0], 11, false);
				}
				for(int i = 0; i <= 3; i++) {
					if(n[i] != null && hasNeighbour(outfit, n[i].comrade.id)) {
						deletePos(outfit, n[i], i);
					}
					if(pn[i] != null) {
						if(hasNeighbour(pout, pn[i].comrade.id)) {
							outfit.neighbour.add(pn[i]);
							pout.neighbour.remove(pn[i]);
							repairNeighbour(pn[i]);
						}
					}
				}
			}
		}
		for(Neighbour nei: outfit.neighbour) {
			nei.comrade.sender = new MessageSender(nei.comrade.ip, nei.comrade.port);
			ArrayList<Integer> mypos  = (ArrayList<Integer>) ClientHelper.ClientNeighbor(nei.position);
			RegularUpdateNeighbourMsg msg = new RegularUpdateNeighbourMsg(outfit.myId, mypos, myPort, myIp, -2);
			if(outfit.pair != null && nei.comrade.id == newpairid)
				msg.mypair = outfit.pair.id;
			nei.comrade.sender.sendMsg(msg);
			neiUpdCount ++;
		}
		outfit.myBoard = newboard;
		up = new boolean[outfit.myBoard.width];
		down = new boolean[outfit.myBoard.width];
		left = new boolean[outfit.myBoard.height];
		right = new boolean[outfit.myBoard.height];
	}
	
	private void handleNeighbourUpdate(RegularUpdateNeighbourMsg msg) throws IOException {
		boolean isOldFriend = false;
		
		for(int i = 0; i < outfit.neighbour.size(); i++) {
			Neighbour nei = outfit.neighbour.get(i);
			if(nei.comrade.id == msg.getClientId()) {
				nei.position.clear();
				for(Integer q: msg.pos) nei.position.add(q);
				isOldFriend = true;
				System.out.println("Old neighbour " + msg.getClientId() + " updated");
				System.out.println("old nei port:" + nei.comrade.port + "  new nei port:" + msg.port);
				if(!nei.comrade.ip.equals(msg.ip) || nei.comrade.port != msg.port) {
					System.out.println("But he is not the origin one");
					nei.comrade.sender.close();
					nei.comrade = new Comrade(msg.getClientId(), msg.port, msg.ip, new MessageSender(msg.ip, msg.port));
				}
			}
			else {
				for(int j = 0; j < nei.position.size(); j++){
					int p = nei.position.get(j);
					for(int k = 0; k < msg.pos.size(); k++)
						if(msg.pos.get(k).equals(p)) {
							nei.position.remove(j);
							j--;
						}
				}
				if(nei.position.size() == 0){
					if(outfit.pair.id != nei.comrade.id)
						nei.comrade.sender.close();
					outfit.neighbour.remove(i);
					i--;
				}
			}
		}
		
		if(!isOldFriend) {
			Neighbour newnei = new Neighbour(msg.pos, 
					new Comrade(msg.getClientId(), msg.port, msg.ip, new MessageSender(msg.ip, msg.port)));
			outfit.neighbour.add(newnei);
		}

		System.out.println("After neighbour update");
		outiftInfo(outfit);
//		System.out.println("Neighbour size: " + outfit.neighbour.size());
//		int cnt = 1;
//		for(Neighbour nei: outfit.neighbour) {
//			System.out.print("Neighbour #" + cnt++ + "  id " + nei.comrade.id + "  position:");
//			for(Integer in: nei.position)
//				System.out.print(" " + in);
//			System.out.println("");
//		}
		
		if(msg.mypair == outfit.pair.id) {
			System.out.println("Pair updated to " + msg.getClientId());
			if(!hasNeighbour(outfit, outfit.pair.id))
				outfit.pair.sender.close();
			outfit.pair = findNeiWithId(outfit, msg.getClientId()).comrade;
		}
		
		sendMsgToId(myConfirmMessage, msg.getClientId());
	}
	
	private void handleSplit(JoinSplitMsg msg) throws IOException {
		List<Board> boards;
		
		Outfits pout = new Outfits(msg.newcomerId, outfit.nextClock, 0, 0, null);
		ArrayList<Neighbour> pnei = new ArrayList<Neighbour>();
		for(Neighbour tn: outfit.neighbour)
			pnei.add(new Neighbour(
					new ArrayList<Integer>(tn.position),
					new Comrade(tn.comrade.id, tn.comrade.port, tn.comrade.ip, null)));
		pout.neighbour = pnei;
		
		Neighbour [] pn = new Neighbour[12];
		for(int i = 0; i < 12; i++)
			pn[i] = findNeiWithPos(pout, i);
		
		Neighbour [] n = new Neighbour[12];
		for(int i = 0; i < 12; i++)
			n[i] = findNeiWithPos(outfit, i);
		
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		
		outfit.pair = new Comrade(msg.newcomerId, msg.newcomerPort, msg.newcomerIp, 
				new MessageSender(msg.newcomerIp, msg.newcomerPort));
		
		if (msg.splitMode == MessageCodeDictionary.SPLIT_MODE_VERTICAL) {
			boards = BoardOperation.VerticalCut(outfit.myBoard);
			Board left, right;
			left = boards.get(0);
			right = boards.get(1);
			
			outfit.myBoard = left;
			
			pout.top = outfit.top;
			pout.left = outfit.left + left.width;
			pout.myBoard = right;
			pout.pair = new Comrade(outfit.myId, myPort, myIp, null);
			
			deletePos(pout, pn[10], 10);
			deletePos(pout, pn[11], 11);
			tmp = new ArrayList<Integer>();
			tmp.add(10);
			tmp.add(11);
			pout.neighbour.add(
					new Neighbour(tmp, new Comrade(outfit.myId, myPort, myIp, null)));
			deletePos(outfit, n[4], 4);
			deletePos(outfit, n[5], 5);
			tmp = new ArrayList<Integer>();
			tmp.add(4);
			tmp.add(5);
			outfit.neighbour.add(
					new Neighbour(tmp, outfit.pair));
			if(n[1] == n[2]) {
				if(n[0] == n[1]) {
					addPos(n[1], 3, false);
					deletePos(outfit, n[3], 3);
				}
				else if(n[2] == n[3]) {
					addPos(pn[1], 0, true);
					deletePos(pout, pn[0], 0);
				}else {
					addPos(n[1], 3, false);
					deletePos(outfit, n[3], 3);
					addPos(pn[1], 0, true);
					deletePos(pout, pn[0], 0);
				}
			}else {
				addPos(n[1], 2, false);
				addPos(n[2], 3, false);
				deletePos(outfit, n[2], 2);
				deletePos(outfit, n[3], 3);
				addPos(pn[2], 1, true);
				addPos(pn[1], 0, true);
				deletePos(pout, pn[1], 1);
				deletePos(pout, pn[0], 0);
			}
			
			if(n[8] == n[7]) {
				if(n[9] == n[8]) {
					addPos(n[8], 6, true);
					deletePos(outfit, n[6], 6);
				}
				else if(n[7] == n[6]) {
					addPos(pn[8], 9, false);
					deletePos(pout, pn[9], 9);
				}else {
					addPos(n[8], 6, true);
					deletePos(outfit, n[6], 6);
					addPos(pn[8], 9, false);
					deletePos(pout, pn[9], 9);
				}
			}else {
				addPos(n[8], 7, true);
				addPos(n[7], 6, true);
				deletePos(outfit, n[7], 7);
				deletePos(outfit, n[6], 6);
				addPos(pn[7], 8, false);
				addPos(pn[8], 9, false);
				deletePos(pout, pn[8], 8);
				deletePos(pout, pn[9], 9);
			}
		}
		else {
			boards = BoardOperation.HorizontalCut(outfit.myBoard);
			Board top, bottom;
			top = boards.get(0);
			bottom = boards.get(1);
			
			outfit.myBoard = top;
			
			pout.top = outfit.top + top.height;
			pout.left = outfit.left;
			pout.myBoard = bottom;
			pout.pair = new Comrade(outfit.myId, myPort, myIp, null);
			
			deletePos(pout, pn[1], 1);
			deletePos(pout, pn[2], 2);
			tmp = new ArrayList<Integer>();
			tmp.add(1);
			tmp.add(2);
			pout.neighbour.add(
					new Neighbour(tmp, new Comrade(outfit.myId, myPort, myIp, null)));
			deletePos(outfit, n[7], 7);
			deletePos(outfit, n[8], 8);
			tmp = new ArrayList<Integer>();
			tmp.add(7);
			tmp.add(8);
			outfit.neighbour.add(
					new Neighbour(tmp, outfit.pair));
			if(n[4] == n[5]) {
				if(n[3] == n[4]) {
					addPos(n[4], 6, false);
					deletePos(outfit, n[6], 6);
				}
				else if(n[5] == n[6]) {
					addPos(pn[4], 3, true);
					deletePos(pout, pn[3], 3);
				}else {
					addPos(n[4], 6, false);
					deletePos(outfit, n[6], 6);
					addPos(pn[4], 3, true);
					deletePos(pout, pn[3], 3);
				}
			}else {
				addPos(n[4], 5, false);
				addPos(n[5], 6, false);
				deletePos(outfit, n[5], 5);
				deletePos(outfit, n[6], 6);
				addPos(pn[5], 4, true);
				addPos(pn[4], 3, true);
				deletePos(pout, pn[4], 4);
				deletePos(pout, pn[3], 3);
			}
			
			if(n[11] == n[10]) {
				if(n[0] == n[11]) {
					addPos(n[11], 9, true);
					deletePos(outfit, n[9], 9);
				}
				else if(n[10] == n[9]) {
					addPos(pn[11], 0, false);
					deletePos(pout, pn[0], 0);
				}else {
					addPos(n[11], 9, true);
					deletePos(outfit, n[9], 9);
					addPos(pn[11], 0, false);
					deletePos(pout, pn[0], 0);
				}
			}else {
				addPos(n[11], 10, true);
				addPos(n[10], 9, true);
				deletePos(outfit, n[10], 10);
				deletePos(outfit, n[9], 9);
				addPos(pn[10], 11, false);
				addPos(pn[11], 0, false);
				deletePos(pout, pn[11], 11);
				deletePos(pout, pn[0], 0);
			}
		}
		up = new boolean[outfit.myBoard.width];
		down = new boolean[outfit.myBoard.width];
		left = new boolean[outfit.myBoard.height];
		right = new boolean[outfit.myBoard.height];
		
		if(RANDOM)
			pout.myBoard.bitmap = null;
		
		System.out.println("My outfit after spliting:");
		outiftInfo(outfit);
		outfit.pair.sender.sendMsg(new RegularOutfitMsg(outfit.myId, myPort, pout));
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
		
		mergeBorder(msg.boarder.bits, outfit.neighbour.get(nei_id).position);
	}
	
	private boolean isBorderMessageComplete() {
		if(borderCount == outfit.neighbour.size())
			return true;
		return false;
	}
	
	private void computeAndReport() throws IOException {
		if(DEBUG_MODE)
			t_bdfnsh = System.currentTimeMillis();
		
		BoardOperation.NextMoment(outfit.myBoard, up, down, left, right, upperLeft, upperRight, lowerLeft, lowerRight);
		
		if(DEBUG_MODE) {
			t_cmptfnsh = System.currentTimeMillis();
			System.out.println("T_RcvStart: " + (t_start - t_lastend)/1000.0 + "  T_BdrCPLT: " + (t_bdfnsh - t_lastend)/1000.0 + "  T_CMPT: " + (t_cmptfnsh - t_bdfnsh)/1000.0);
		}
		
		if(!TEST_MODE) {
//			whether to leave
			System.out.println("Do you want to leave?\n0: no    1: yes");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String res = br.readLine();
			if(Integer.parseInt(res) == 1)
				isleaving = true;
			else
				isleaving = false;
			server.sender.sendMsg(new RegularBoardReturnMsg(isleaving, outfit.myId, outfit.top, outfit.left, outfit.myBoard));
		}
		else {
			server.sender.sendMsg(new RegularBoardReturnMsg(isleaving, outfit.myId, outfit.top, outfit.left, null));
//			server.sender.sendMsg(myConfirmMessage);
		}
		outfit.nextClock ++;
		borderCount = 0;
		
		if(DEBUG_MODE)
			t_lastend = System.currentTimeMillis();
	}
	
	private void sendMsgToId(Message msg, int id) throws IOException {
		for(Neighbour nei: outfit.neighbour)
			if(nei.comrade.id == id)
				nei.comrade.sender.sendMsg(msg);
	}
	
	private void repairNeighbour(Neighbour nei) throws IOException {
		nei.comrade.sender = new MessageSender(nei.comrade.ip, nei.comrade.port);
	}
	
	private void deletePos(Outfits out, Neighbour nei, Integer pos) {
		if(nei == null)
			return ;
		nei.position.remove(pos);
		if(nei.position.size() == 0) {
			if(nei.comrade.sender != null && nei.comrade.id != out.pair.id){
				nei.comrade.sender.close();
			}
			out.neighbour.remove(nei);
		}
	}
	
	private void addPos(Neighbour nei, Integer pos, boolean front) {
		if(nei == null)
			return;
		if(front)
			nei.position.add(0, pos);
		else
			nei.position.add(pos);
	}
	
	private boolean hasNeighbour(Outfits out, int id) {
		for(Neighbour nei: out.neighbour)
			if(nei.comrade.id == id)
				return true;
		return false;
	}
	
	private Neighbour findNeiWithId(Outfits out, int id) {
		for(Neighbour nei: out.neighbour)
			if(nei.comrade.id == id)
				return nei;
		return null;
	}
	
	private void outiftInfo(Outfits out) {
		System.out.println("Id:   " + out.myId);
		System.out.println("Clk:  " + out.nextClock);
		System.out.println("Top:  " + out.top);
		System.out.println("Left: " + out.left);
		System.out.println("Width:" + out.myBoard.width);
		System.out.println("Heig: " + out.myBoard.height);
		System.out.println("Neighbour size: " + out.neighbour.size());
		int cnt = 1;
		for(Neighbour nei: out.neighbour){
			System.out.print("Nei #" + cnt++ + "  Id: " + nei.comrade.id +"  Pos:");
			for(Integer in: nei.position)
				System.out.print(" " + in);
			System.out.println("");
		}
		if(out.pair != null)
			System.out.println("Pair Id: " + out.pair.id);
		
	}
	
	private Neighbour findNeiWithPos(Outfits out, int pos) {
		for(Neighbour nei: out.neighbour)
			for(Integer i: nei.position)
				if(i == pos)
					return nei;
		return null;
	}
	
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
				al.add(0,b.bitmap[0][0]);
				break;
			case 2:
				j = 0;
				//if it is 1 already
				if(al.size() != 0)
					j = 1;
				for(; j<=b.width/2; j++)
					al.add(0,b.bitmap[0][j]);
				break;
			case 3:
				//if it is 2 already
				j = b.width/2-1;
				if(al.size() != 0)
					j=j+2;
				
				for(; j<b.width; j++)
					al.add(0,b.bitmap[0][j]);
				break;
			case 4:
				//if it is 3 already
				if(al.size() != 0)
					break;
				al.add(0,b.bitmap[0][b.width-1]);
				break;
			case 5:
				j = 0;
				//if it is 4 already
				if(al.size()!=0)
					j=1;
				for(; j<=b.height/2; j++)
					al.add(0,b.bitmap[j][b.width-1]);
				break;
			case 6:
				j = b.height/2-1;
				//if it is 5 already
				if(al.size()!=0)
					j=j+2;
				for(; j<b.height; j++)
					al.add(0,b.bitmap[j][b.width-1]);
				break;
			case 7:
				//if it is 6 already
				if(al.size()!=0)
					break;
				al.add(0,b.bitmap[b.height-1][b.width-1]);
				break;
			case 8:
				j = b.width - 1;
				//if it is 7 already
				if(al.size()!=0)
					j--;
				for(; j>=b.width/2-1; j--)
					al.add(0,b.bitmap[b.height-1][j]);
				break;
			case 9:
				j = b.width/2;
				if(al.size()!=0)
					j=j-2;
				for(; j>=0; j--)
					al.add(0,b.bitmap[b.height-1][j]);
				break;
			case 10:
				if(al.size()!=0)
					break;
				al.add(0,b.bitmap[b.height-1][0]);
				break;
			case 11:
				j = b.height-1;
				if(al.size()!=0)
					j--;
				for(; j>=b.height/2-1; j--)
					al.add(0,b.bitmap[j][0]);
				break;
			case 12:
				j = b.height/2;
				if(al.size()!=0)
					j=j-2;
				for(; j>=0; j--)
					al.add(0,b.bitmap[j][0]);
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
		
		for(int i=0; i<aa.length; i++){
			tmp.add(aa[i]);
		}
		
		Board b = outfit.myBoard;
		
		
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
	
	public void calcPower(int n)
	{
		int cnt = 1;
		long st = System.currentTimeMillis();
		for(int i=0; i<n; i++)
		{
			if (cnt % 7 == 3) cnt += 2;
			else cnt += 9;
		}
		long ed = System.currentTimeMillis();
		System.out.println("calculate power:" + 1000.0/(ed-st));
	}
	
}
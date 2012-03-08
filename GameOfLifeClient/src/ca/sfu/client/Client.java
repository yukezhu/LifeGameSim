package ca.sfu.client;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import ca.sfu.cmpt431.facility.Board;
import ca.sfu.cmpt431.facility.BoardOperation;
import ca.sfu.cmpt431.facility.Border;
import ca.sfu.cmpt431.facility.Comrade;
import ca.sfu.cmpt431.facility.Outfits;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;
import ca.sfu.cmpt431.message.join.JoinOutfitsMsg;
import ca.sfu.cmpt431.message.join.JoinRequestMsg;
import ca.sfu.cmpt431.message.join.JoinSplitMsg;
import ca.sfu.cmpt431.message.regular.RegularBoardReturnMsg;
import ca.sfu.cmpt431.message.regular.RegularBorderMsg;
import ca.sfu.cmpt431.message.regular.RegularConfirmMsg;
import ca.sfu.cmpt431.message.regular.RegularNextClockMsg;
import ca.sfu.cmpt431.message.regular.RegularUpdateNeighbourMsg;
import ca.sfu.network.MessageReceiver;
import ca.sfu.network.MessageSender;
import ca.sfu.network.SynchronizedMsgQueue.MessageWithIp;

public class Client {

	protected static final int SERVER_PORT = 6560;
//	private final static int 
	
	private int status;
	private int cid;
//	private AutomataMsg auto;
	private String direction;
	private int[] border;
	private RegularConfirmMsg confirm = new RegularConfirmMsg(-10);
	private Outfits outfit;
//	private MessageSender[] Sender;
	private Comrade[]  comrade;
//	private MessageSender ServerSender;
	private Comrade  server;
//	private int comrade_id;
//	private int Sender_id;

	public MessageReceiver Receiver; 
//	public MessageSender Sender1;
//	public MessageSender Sender2;
//	public Comrade comrade1 = new Comrade(-1, Sender1);
	public int port;
	public MessageWithIp msgIp;
	public Board myboard;
	public int pair_id;
	public boolean[] up ;
	public boolean[] down;
	public boolean[] left;
	public boolean[] right;
	public boolean upperLeft;
	public boolean upperRight;
	public boolean lowerLeft;
	public boolean lowerRight;
	
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
		MessageSender Sender1;
		Sender1 = new MessageSender("142.58.35.71", SERVER_PORT);
		server = new Comrade(-1, SERVER_PORT, "142.58.35.71",Sender1);
		JoinRequestMsg Request = new JoinRequestMsg(port);
		server.sender.sendMsg(Request);
//		regedClientSender.get(0).sender.sendMsg(Request);
		status = 0;
	}
	
	public void startClient() throws IOException, ClassNotFoundException, InterruptedException
	{
		while(true){
			if(!Receiver.isEmpty()){
				System.out.println(status);
				
				switch(status) {
					case MessageCodeDictionary.ORIGINAL_STATUS:	
						Receiver.getNextMessageWithIp().extracMessage();
						server.sender.sendMsg(confirm);						
						status = MessageCodeDictionary.GET_CLIENT_ID_STATUS;
						break;
					//wait for cid
					case MessageCodeDictionary.GET_CLIENT_ID_STATUS:
						
//						JoinOutfitsMsg ob;
						JoinOutfitsMsg joinmsg;
						msgIp = Receiver.getNextMessageWithIp();
						joinmsg = (JoinOutfitsMsg)msgIp.extracMessage();
						outfit = joinmsg.yourOutfits;
//						joinmsg = (JoinOutfitsMsg)ob;
						cid = outfit.myId;
						pair_id = joinmsg.getClientId();
						int pair_port = joinmsg.myPort;
						if(pair_port <0){
							server.sender.sendMsg(confirm);
							System.out.println(pair_id);
						}
						else{
							String pair_ip = msgIp.getIp().substring(1); 
							MessageSender Sender2 = new MessageSender(pair_ip, pair_port);
							comrade[pair_id] = new Comrade(pair_id, pair_port, pair_ip, Sender2);							
							comrade[pair_id].sender.sendMsg(confirm);
						}
						int i = 0;
						int[] position;
						int j = 0;
						int p = 0;
						for(i=0;i<12;i++)
						{
							while(outfit.neighbour.get(j) != null){
								for (p = 0; p < outfit.neighbour.get(j).position.length; p++ ){
									if(i == outfit.neighbour.get(j).position[p]){
										MessageSender Sender = new MessageSender(outfit.neighbour.get(i).comrade.ip, pair_port);
										comrade[pair_id] = new Comrade(pair_id, pair_port, outfit.neighbour.get(i).comrade.ip, Sender);
										int[] mypos;
										mypos = ClientHelper.ClientNeighbor(outfit.neighbour.get(j).position);
										RegularUpdateNeighbourMsg neighbor = new RegularUpdateNeighbourMsg(cid, mypos, port, InetAddress.getLocalHost().getHostAddress());
										comrade[pair_id].sender.sendMsg(neighbor);
									}
									
								}
								j++;
							}
																
						}
						status = MessageCodeDictionary.RECEIVE_NEIGHBORS_CONFIRM_STATUS;
						break;
					//receive neighbor's confirm
					case MessageCodeDictionary.RECEIVE_NEIGHBORS_CONFIRM_STATUS:
						int neighborupdateconfirmCount = 0;	
						
						Receiver.getNextMessageWithIp();
						neighborupdateconfirmCount++;
						if(neighborupdateconfirmCount == outfit.neighbour.size())
						{
							server.sender.sendMsg(confirm);
							status = MessageCodeDictionary.WAIT_FOR_COMMAND_STATUS;
						}
						else 
							status = MessageCodeDictionary.RECEIVE_NEIGHBORS_CONFIRM_STATUS;
						break;
					//wait for start or other commands
					case MessageCodeDictionary.WAIT_FOR_COMMAND_STATUS:								
						msgIp = Receiver.getNextMessageWithIp();
						Message msg = (Message)msgIp.extracMessage();
						int msg_type;
						msg_type = msg.getMessageCode();
						if(msg_type == MessageCodeDictionary.REGULAR_NEXTCLOCK)
							status = MessageCodeDictionary.SEND_BORDER_STATUS;
						else if (msg_type == MessageCodeDictionary.REGULAR_UPDATE_NEIGHBOUR)
							status = MessageCodeDictionary.UPDATE_NEIGHBOR_STATUS;
						else if (msg_type == MessageCodeDictionary.JOIN_SPLIT)
							status = MessageCodeDictionary.SPLIT_STATUS;
						else
							status = MessageCodeDictionary.REVEIVE_BORDER_OR_CONFIRM_STATUS;
						break;
					//update neighbor
					case MessageCodeDictionary.UPDATE_NEIGHBOR_STATUS:
						msgIp = Receiver.getNextMessageWithIp();
						RegularUpdateNeighbourMsg neighbormsg = (RegularUpdateNeighbourMsg)msgIp.extracMessage();

						for(i=0;i<12;i++)
						{
							while(outfit.neighbour.get(j) != null){
								for (p = 0; p < outfit.neighbour.get(j).position.length; p++ ){
									if(i == outfit.neighbour.get(j).position[p]){
										outfit.neighbour.get(j).comrade.id = neighbormsg.getClientId();
									}									
								}
								j++;
							}																
						}

						String neighbor_ip = neighbormsg.ip;
						int neighbor_port = neighbormsg.port;
						MessageSender Sender = new MessageSender(neighbor_ip, neighbor_port);
						comrade[neighbormsg.getClientId()] = new Comrade(neighbormsg.getClientId(), neighbor_port, neighbor_ip,  Sender);
						comrade[neighbormsg.getClientId()].sender.sendMsg(confirm);
						status = MessageCodeDictionary.WAIT_FOR_COMMAND_STATUS;
						break;
					//start and send border
					case MessageCodeDictionary.SEND_BORDER_STATUS:
						RegularNextClockMsg clock = (RegularNextClockMsg)msgIp.extracMessage();
						myboard = new Board(outfit.myBoard.height,outfit.myBoard.width);

						int neighborCount = outfit.neighbour.size();
						Border sendborder;						
						for(j = 0; j < neighborCount; j++)
						{
							
								sendborder = getborder(outfit.neighbour.get(j).position);
								RegularBorderMsg sendbordermsg = new RegularBorderMsg(cid, sendborder);	
								outfit.neighbour.get(j).comrade.sender.sendMsg(myboard);

						}
						status = MessageCodeDictionary.REVEIVE_BORDER_OR_CONFIRM_STATUS;
						break;
					//receive border or confirm
					case MessageCodeDictionary.REVEIVE_BORDER_OR_CONFIRM_STATUS:
						msgIp = Receiver.getNextMessageWithIp();
						msg_type = msg.getMessageCode();
						int confirmCount = 0;
						int borderexchangeCount = 0;
						int[] posRecord;
						if(msg_type == MessageCodeDictionary.REGULAR_BORDER_EXCHANGE)
						{
							RegularBorderMsg neighborbordermsg = (RegularBorderMsg)msgIp.extracMessage();
							borderexchangeCount++;
							for(j = 0; j<outfit.neighbour.size(); j++)
							{
								if(neighborbordermsg.getClientId() == outfit.neighbour.get(j).comrade.id)
									posRecord = outfit.neighbour.get(i).position;
									
							}
							mergeBorder(neighborbordermsg.boarder, posRecord);
							
						}
						else if (msg_type == MessageCodeDictionary.REGULAR_CONFIRM)
						{
							confirmCount++;
						}
						if(borderexchangeCount == outfit.neighbour.size() && borderexchangeCount == outfit.neighbour.size())
								status = MessageCodeDictionary.COMPUTING_STATUS;
						else 
							status = MessageCodeDictionary.REVEIVE_BORDER_OR_CONFIRM_STATUS;
						break;
										
					// split
					case MessageCodeDictionary.SPLIT_STATUS:
						JoinSplitMsg joinsplitmsg = (JoinSplitMsg)msgIp.extracMessage();
						List<Board> board;
						if (joinsplitmsg.splitMode == MessageCodeDictionary.SPLIT_MODE_VERTICAL)
						{
							board = BoardOperation.VerticalCut(myboard);
						}
						else{
							board = BoardOperation.HorizontalCut(myboard);
						}	
						myboard = board.get(0);
						MessageSender Sender3 = new MessageSender(joinsplitmsg.newcomerIp, joinsplitmsg.newcomerPort);
						comrade[joinsplitmsg.newcomerId] = new Comrade(joinsplitmsg.newcomerId, joinsplitmsg.newcomerPort, joinsplitmsg.newcomerIp, Sender3);
						Outfits pair_outfit = new Outfits(pair_id, outfit.nextClock, outfit.top, outfit.left, myboard.height, myboard.width);
						
						JoinOutfitsMsg JOM = new JoinOutfitsMsg(cid, port, pair_outfit);
						comrade[joinsplitmsg.newcomerId].sender.sendMsg(JOM);
						status = MessageCodeDictionary.WAIT_FOR_PAIR_CONFIRM_STATUS;
						break;
					//wait for pair's confirm
					case MessageCodeDictionary.WAIT_FOR_PAIR_CONFIRM_STATUS:
						msgIp = Receiver.getNextMessageWithIp();
						server.sender.sendMsg(confirm);
						status = MessageCodeDictionary.WAIT_FOR_COMMAND_STATUS;
						break;
					//computing
					case MessageCodeDictionary.COMPUTING_STATUS:
						myboard = BoardOperation.NextMoment(myboard, up, down, left, right, upperLeft, upperRight, lowerLeft, lowerRight);
						RegularBoardReturnMsg boardreturnmsg = new RegularBoardReturnMsg(cid,myboard);
						server.sender.sendMsg(boardreturnmsg);
						status = MessageCodeDictionary.WAIT_FOR_COMMAND_STATUS;
						break;
					default:
						break;
				}
			}
		}
		
	}

	
}



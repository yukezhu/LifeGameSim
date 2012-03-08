package ca.sfu.client;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import ca.sfu.cmpt431.facility.Board;
import ca.sfu.cmpt431.facility.BoardOperation;
import ca.sfu.cmpt431.facility.Comrade;
import ca.sfu.cmpt431.facility.Outfits;
import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;
import ca.sfu.cmpt431.message.join.JoinOutfitsMsg;
import ca.sfu.cmpt431.message.join.JoinRequestMsg;
import ca.sfu.cmpt431.message.join.JoinSplitMsg;
import ca.sfu.cmpt431.message.regular.RegularConfirmMsg;
import ca.sfu.cmpt431.message.regular.RegularNextClockMsg;
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
		server = new Comrade(-1,Sender1);
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
					case 0:	
						Receiver.getNextMessageWithIp().extracMessage();
						server.sender.sendMsg(confirm);						
						status = 1;
						break;
						//wait for cid
					case 1:
						
						JoinOutfitsMsg ob;
						JoinOutfitsMsg joinmsg;
						msgIp = Receiver.getNextMessageWithIp();
						ob = (JoinOutfitsMsg)msgIp.extracMessage();
						outfit = ob.yourOutfits;
						joinmsg = (JoinOutfitsMsg)ob;
						cid = outfit.myId;
						pair_id = ob.getClientId();
						int pair_port = joinmsg.myPort;
						if(pair_port <0){
							server.sender.sendMsg(confirm);
							System.out.println(pair_id);
						}
						else{
							String pair_ip = msgIp.getIp().substring(1); 
//							Comrade comrade2 = new Comrade(pair_id, Sender2);
							MessageSender Sender2 = new MessageSender(pair_ip, pair_port);
							comrade[pair_id] = new Comrade(pair_id, Sender2);
							
//							comrade[pair_id] = Comrade(pair_id, Sender2);
//							comrade2.sender = new MessageSender(pair_ip, pair_port);							
							comrade[pair_id].sender.sendMsg(confirm);
						}
						status = 2;
						break;
						//wait for start or other commands
					case 2:			
//						MessageWithIp msgIp2;						
						msgIp = Receiver.getNextMessageWithIp();
						Message msg = (Message)msgIp.extracMessage();
						int msg_type;
						msg_type = msg.getMessageCode();
						if(msg_type == 1)
							status = 3;
						else 
							status = 4;
						break;
						//start
					case 3:
						RegularNextClockMsg clock = (RegularNextClockMsg)msgIp.extracMessage();
						myboard = new Board(outfit.myBoard.height,outfit.myBoard.width);
//						int down = outfit.top+outfit.myBoard.height;
//						int right = outfit.left+outfit.myBoard.width;
						boolean[] up = new boolean[] {false,false,false,false,false,false,false,false,false,false};
						boolean[] down= new boolean[] {false,false,false,false,false,false,false,false,false,false};
						boolean[] left= new boolean[] {false,false,false,false,false,false,false,false,false,false};
						boolean[] right= new boolean[] {false,false,false,false,false,false,false,false,false,false};
						boolean upperLeft = false;
						boolean upperRight = false;
						boolean lowerLeft = false;
						boolean lowerRight = false;
						myboard = BoardOperation.NextMoment(outfit.myBoard, up, down, left, right, upperLeft, upperRight, lowerLeft, lowerRight); 
						server.sender.sendMsg(myboard);
//						status = 3;
						break;
						// split
					case 4:
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
						comrade[joinsplitmsg.newcomerId] = new Comrade(joinsplitmsg.newcomerId, Sender3);
						Outfits pair_outfit = new Outfits(pair_id, outfit.nextClock, outfit.top, outfit.left, myboard.height, myboard.width);
						
						JoinOutfitsMsg JOM = new JoinOutfitsMsg(cid, port, pair_outfit);
						comrade[joinsplitmsg.newcomerId].sender.sendMsg(JOM);
						status = 5;
						break;
					//wait for pair's confirm
					case 5:
						msgIp = Receiver.getNextMessageWithIp();
						server.sender.sendMsg(confirm);
						status = 2;
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



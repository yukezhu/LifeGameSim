package ca.sfu.network;

import java.io.IOException;

public class MessageTester {

	public static void main(String[] argv) throws IOException, InterruptedException
	{
		String clientFlag = argv[0];
		if(clientFlag.equals("client"))
		{
			MessageSender sender = new MessageSender("127.0.0.1", 5354);
			sender.sendMsg("hello world");			
		} else {
			MessageReceiver receiver = new MessageReceiver(5354);
			while(!receiver.isEmpty())
			{
				String s = (String) receiver.getNextMessageWithIp().extracMessage();
				System.out.println(s);
			}
		}
	}
	
}

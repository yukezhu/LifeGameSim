package ca.sfu.client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.net.UnknownHostException;

import ca.sfu.message.Automata;


public class Client extends ServerSocket{
	
	Socket socket; 
	
	protected static final int PORT = 8765;
	protected static final int SERVER_PORT = 6560;
	
	public Client() throws UnknownHostException, IOException, ClassNotFoundException
	{
		super(SERVER_PORT); 
		socket = new Socket("142.58.35.159", PORT);
	}
	
	public void startClient() throws IOException, ClassNotFoundException
	{
		final ObjectOutputStream clientOutputStream = new ObjectOutputStream(
			     socket.getOutputStream());
		ObjectInputStream clientInputStream = new ObjectInputStream(
			     socket.getInputStream());
		
		Automata auto = new Automata();
		String client_ip;
		String direction;
		
		auto = (Automata)clientInputStream.readObject();
		client_ip = (String)clientInputStream.readObject();
		direction = (String)clientInputStream.readObject();
		int[] border=new int[auto.getHeight()];
		
		Socket socket1;
		if(client_ip == null){
			socket1 = accept(); 
			clientOutputStream.writeUnshared("OK");
		}
		else{
			socket1 = new Socket(client_ip, SERVER_PORT);
		}
		final ObjectOutputStream pairOutputStream = new ObjectOutputStream(
			     socket1.getOutputStream());
		ObjectInputStream pairInputStream = new ObjectInputStream(
			     socket1.getInputStream());
			
		
		try
		{
			while(true)
			{
				//System.out.println();
				//System.out.println(direction);
				for(int i=0;i<border.length; i++)
					if(direction.equals("left"))
						border[i] = auto.bitmap[i][auto.width-1];
					else
						border[i] = auto.bitmap[i][0];
				
				String start = (String)clientInputStream.readObject();
				if(!start.equals("start"))
					continue;
				
				pairOutputStream.writeUnshared(border);
				border = (int[])pairInputStream.readObject();
				
				System.out.println("computing");
				
				auto.nextMoment(direction.equals("left"), border);
				
				auto.printout();
				
				Thread.sleep(200);
				clientOutputStream.writeUnshared(auto); // Notice: Use Unshared!!!
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			socket.close();
			clientOutputStream.close();
			clientInputStream.close();
		}
	}
	
}

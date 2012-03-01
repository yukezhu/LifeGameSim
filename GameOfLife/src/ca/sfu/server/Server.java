package ca.sfu.server;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;

import ca.sfu.message.Automata;


public class Server extends ServerSocket{
	
	protected static final int SERVER_PORT = 8765;
	
	Automata auto;
	
	public Server() throws IOException 
	{ 
		super(SERVER_PORT); 
	}
	
	protected void startServer() throws IOException, ClassNotFoundException
	{
		ObjectInputStream serverInputStream1;
		ObjectOutputStream serverOutputStream1;
		
		ObjectInputStream serverInputStream2;
		ObjectOutputStream serverOutputStream2;
		
		auto = new Automata();

		//auto.randomBitmap();
		//System.out.println(auto.width);
		//System.out.println(auto.height);
		
		Socket socket1 = accept(); 
		Socket socket2 = accept();
		
		//get ip
		String client_ip1 = socket1.getInetAddress().toString().substring(1);
		String client_ip2 = socket2.getInetAddress().toString().substring(1);
		
		serverInputStream1 = new ObjectInputStream(socket1.getInputStream());
		serverOutputStream1 = new ObjectOutputStream(socket1.getOutputStream());
		
		serverInputStream2 = new ObjectInputStream(socket2.getInputStream());
		serverOutputStream2 = new ObjectOutputStream(socket2.getOutputStream());
		
		
		serverOutputStream1.writeUnshared(auto.left());
		serverOutputStream1.writeUnshared(null);
		serverOutputStream1.writeUnshared("left"); //"left" or "right"
		
		serverOutputStream2.writeUnshared(auto.right());
		serverOutputStream2.writeUnshared(client_ip1);
		serverOutputStream2.writeUnshared("right"); //"left" or "right"
		
		//serverOutputStream2.writeUnshared(auto);
		//serverOutputStream2.writeUnshared(client_ip2);
		
		if(!((String)serverInputStream1.readObject()).equals("OK")){
			return; //error
		}
		
		// UI
		JFrame frame = new JFrame();
		frame.setSize(480, 480);
		Automata auto = new Automata();
		AutomataPanel panel = new AutomataPanel();
		panel.setAutomata(auto);
		frame.setContentPane(panel);
		frame.setVisible(true);

		auto = new Automata();
		try
		{
			while(true)
			{
				//read user name, using java.util.Formatter syntax :
//				BufferedReader reader;
//				reader = new BufferedReader(new InputStreamReader(System.in));
//				String name = reader.readLine();
				
				Thread.sleep(20);
				
				//start
				serverOutputStream2.writeUnshared("start");
				serverOutputStream1.writeUnshared("start");
				
				System.out.println("Loading data...");
				//System.out.println(client_ip);
				//auto = (Automata)serverInputStream1.readObject();
				auto.mergeLeft((Automata)serverInputStream1.readObject());
				auto.mergeRight((Automata)serverInputStream2.readObject());
				
				System.out.println("received");
				panel.setAutomata(auto);
				panel.repaint();
				
				
				
				
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		finally {
			serverInputStream1.close();
			serverOutputStream1.close();
			close();
		}
	}
	
	public Automata getAutomata()
	{
		return auto;
	}
}

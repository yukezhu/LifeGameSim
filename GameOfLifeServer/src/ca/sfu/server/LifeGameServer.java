package ca.sfu.server;
import java.io.IOException;

import javax.swing.JFrame;

public class LifeGameServer extends JFrame {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException
	{
		
		Server s1 = new Server();
		s1.startServer();
	}

}

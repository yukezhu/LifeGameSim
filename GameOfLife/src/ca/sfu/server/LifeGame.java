package ca.sfu.server;
import java.io.IOException;

import javax.swing.JFrame;

public class LifeGame extends JFrame {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		
		Server s1 = new Server();
		s1.startServer();
	}

}

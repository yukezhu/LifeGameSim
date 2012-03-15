package ca.sfu.server;
import java.io.IOException;

public class LifeGameServer {

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException
	{
		Server s1 = new Server();
		s1.startServer();
	}

}

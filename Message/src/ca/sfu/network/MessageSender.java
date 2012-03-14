package ca.sfu.network;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class MessageSender{

	private static final int BufferSize = 1048576;
	private static byte [] tmpbuf = new byte[BufferSize];
	
	private Selector selector = null;

	SocketChannel socketChannel;

	public String hostIp;

	public int hostListenningPort;

	public static void main(String[] args) throws IOException{
		MessageSender client = new MessageSender("142.58.35.130",1990);
		String msg = "hello";	
		client.sendMsg(msg);
	}

	public MessageSender(String HostIp, int HostListenningPort) throws IOException{
		this.hostIp=HostIp;
		this.hostListenningPort = HostListenningPort;   

		socketChannel=SocketChannel.open(new InetSocketAddress(hostIp, hostListenningPort));
		socketChannel.configureBlocking(false);

		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_READ);
	}

	public void sendMsg (Object msg) throws IOException{
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bOut);
		out.writeObject(msg);
		out.flush();
		byte [] arr = bOut.toByteArray();
		
		int len = arr.length;
		for(int i = 0; i < 4; i++)
			tmpbuf[i] = (byte) (len >> ((3 - i) * 8));
		for(int i = 0; i < arr.length; i++)
			tmpbuf[i + 4] = arr[i];
		
		System.out.println("sending message of size " + arr.length);
		
		ByteBuffer bb = ByteBuffer.wrap(tmpbuf, 0, len + 4);
		int num = socketChannel.write(bb);
		System.out.println("actual data written" + num + "\n");
		
		out.close();
	}
	
	public boolean isOpen() {
		if(socketChannel == null) return false;
		return socketChannel.isOpen();
	}
	
	public void close() {
		try {
			selector.close();
		} catch (IOException e) {
		}
		try {
			socketChannel.close();
		} catch (Exception e) {
		}
	}
}
package ca.sfu.network;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import ca.sfu.network.SynchronizedMsgQueue.MessageWithIp;

public class MessageReceiver {
	
	private static final int BufferSize = 165536;
	private static final int QueueSize  = 1024;
	private static final int TimeOut    = 3000;
	
	private Selector selector;
	private ServerSocketChannel listenerChannel;
	
	private SynchronizedMsgQueue msgQueue;
	
//	private ByteBuffer tmpbuf;
	private byte [] tmpbuf;
  
	public static void main(String[] args) throws IOException, InterruptedException{
		MessageReceiver ms = new MessageReceiver(1978);
		while(true){
			if(!ms.isEmpty()) {
				String msg = (String)ms.getNextMessageWithIp().extracMessage();
				System.out.println(msg);
			}
		}
	}
	
	public MessageReceiver(int ListenPort) throws IOException {
		selector = Selector.open();
		
		listenerChannel = ServerSocketChannel.open();
		listenerChannel.socket().bind(new InetSocketAddress(ListenPort));
		listenerChannel.configureBlocking(false);
		listenerChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		msgQueue = new SynchronizedMsgQueue(QueueSize);
		
//		tmpbuf = ByteBuffer.allocate(BufferSize);
		tmpbuf = new byte[BufferSize];
		
		new ListeningThread();
		
	}
	
	public boolean isEmpty() {
		return msgQueue.isEmpty();
	}
	
	public MessageWithIp getNextMessageWithIp() throws InterruptedException {
		return (MessageWithIp)msgQueue.pop();
	}
	
	private class ListeningThread implements Runnable {
		ListeningThread() {
			new Thread(this).start();
		}

		@Override
		public void run() {
			while(true) {
				try {
					if(selector.select(TimeOut) == 0){
//						System.out.println("Listening.");
						continue;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
				while(keyIter.hasNext()){
					System.out.println("\nIn 2nd Thread, before handling message");
					calcPower(10000000);
					
					SelectionKey key=keyIter.next();
					keyIter.remove();
					try{
						if(key.isAcceptable()){
							handleAccept(key);
						}
	          
						if(key.isReadable()){
							handleRead(key);
						}
					} catch(IOException e){
						e.printStackTrace();
					}
					
					System.out.println("After handling message");
					calcPower(10000000);
				}
			}
		}
	}
	
	public boolean isOpen() {
		if(listenerChannel == null) return false;
		return listenerChannel.isOpen();
	}
	
	public void close() {
		try {
			selector.close();
		} catch (IOException e) {
		}
		try {
			listenerChannel.close();
		} catch (Exception e) {
		}
	}
	
	private void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clientChannel=((ServerSocketChannel)key.channel()).accept();
		clientChannel.configureBlocking(false);
		clientChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(BufferSize));
	}
  
	private void handleRead(SelectionKey key) throws IOException {
		SocketChannel clientChannel = (SocketChannel)key.channel();
		ByteBuffer buffer = (ByteBuffer)key.attachment();
		
		int cursor = 0;
		int length = 0;
		int bytesRead = 0;
		
//		long ts, te;
		
		buffer.clear();
		bytesRead = clientChannel.read(buffer);
		if(bytesRead > 0) {
			
//			ts = System.currentTimeMillis();
//			System.out.println("start receiving,");
			
			
			buffer.flip();
			length = buffer.getInt();
			cursor = bytesRead - 4;
			for(int i = 0; i < cursor; i++)
				tmpbuf[i] = buffer.array()[i + 4];
		}
		else {
			return ;
		}
		
		while(key.isReadable() && cursor < length) {
			buffer.clear();
			bytesRead = clientChannel.read(buffer);
			if(bytesRead > 0) {
				buffer.flip();
				byte [] data = buffer.array();
				for(int i = 0; i < bytesRead; i++)
					tmpbuf[cursor + i] = data[i];
				cursor += bytesRead;
			}
		}
		
		try {
			
//			te = System.currentTimeMillis();
//			System.out.println("receiving finished. used time:" + (te - ts) / 1000.0 + "  message size is:" + length);
//			ts = te;
//			System.out.println("start receiving,");
			
			byte[] arr = MessageCompressor.decompress(tmpbuf);
			
//			te = System.currentTimeMillis();
//			System.out.println("receiving finished. used time:" + (te - ts) / 1000.0 + "  message size is:" + length);
			
			
			ByteArrayInputStream bi = new ByteArrayInputStream(arr);
			ObjectInputStream oi = new ObjectInputStream(bi);
			Object msg = oi.readObject();
			msgQueue.push(msg, clientChannel.socket().getInetAddress().toString());
			bi.close();
			oi.close();
		} catch (Exception e) {
			e.printStackTrace();
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
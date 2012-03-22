package ca.sfu.network;

public class SynchronizedMsgQueue {
	private Object[] elements;
	private int head;
	private int tail;
	private int size;

	public SynchronizedMsgQueue(int capacity) {
		elements = new Object[capacity];
		head = 0;
		tail = 0;
	    size = 0;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public int getSize() {
		return size;
	}

	public Object pop() throws InterruptedException {
		if (size > 0) {
			Object msg = (Object) elements[head];
			head++;
			size--;
			if (head == elements.length)
				head = 0;
			notifyAll();
			return msg;
		}
		return null;
	}

	public void push(Object msg, String ipAddress) throws InterruptedException {
		while (size == elements.length)
			wait();
		elements[tail] = new MessageWithIp(msg, ipAddress);
		tail ++;
	    size ++;
	    if (tail == elements.length)
	    	tail = 0;
	}
	
	public class MessageWithIp {
		private Object message;
		private String ip;
		
		public MessageWithIp(Object msg, String ip) {
			this.message = msg;
			this.ip = ip;
		}
		
		public Object extracMessage() {
			return message;
		}
		
		public String getIp() {
			return ip.substring(1);
		}
	}
}

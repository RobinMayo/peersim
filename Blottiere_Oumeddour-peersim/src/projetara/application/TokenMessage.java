package projetara.application;

import java.util.Queue;

import projetara.util.Message;


public class TokenMessage extends Message {
	
	private final int counter;
	private final Queue<Long> next;
	
	public TokenMessage(long idsrc, long iddest, Queue<Long> next, int counter, int pid) {
		super(idsrc, iddest, Application.TOKEN_TAG, null, pid);
		this.counter=counter;
		this.next=next;
	}

	public int getCounter() {
		return counter;
	}

	public Queue<Long> getNext() {
		return next;
	}

}

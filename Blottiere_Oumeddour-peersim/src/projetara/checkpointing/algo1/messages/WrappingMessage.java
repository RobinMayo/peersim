package projetara.checkpointing.algo1.messages;

import projetara.util.Message;

public class WrappingMessage extends Message{

	public Message getMessage(){
		return (Message)this.getContent();
	}
	
	public WrappingMessage(long idsrc, long iddest, Message mess, int pid) {
		super(idsrc, iddest, "WrappingMessage", mess, pid);
	}
	
}
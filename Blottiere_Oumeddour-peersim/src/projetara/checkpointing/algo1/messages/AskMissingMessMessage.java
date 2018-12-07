package projetara.checkpointing.algo1.messages;

import projetara.util.Message;

public class AskMissingMessMessage extends Message{

	public int getNbRcv(){
		return (Integer)this.getContent();
	}
	
	public AskMissingMessMessage(long idsrc, long iddest, int nbrcv, int pid) {
		super(idsrc, iddest, "AskMissingMessMessage", nbrcv, pid);
	}
	
}
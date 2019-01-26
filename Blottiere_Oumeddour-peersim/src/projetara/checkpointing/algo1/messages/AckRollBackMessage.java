package projetara.checkpointing.algo1.messages;

import projetara.util.Message;

public class AckRollBackMessage extends Message{
	public AckRollBackMessage(long idsrc, long iddest, boolean nack, int pid) {
		super(idsrc, iddest, " AckRollBackMessage", nack, pid);
	}

	public boolean should_contine_rollback() {
		return (Boolean) getContent();
	}
	
	
}
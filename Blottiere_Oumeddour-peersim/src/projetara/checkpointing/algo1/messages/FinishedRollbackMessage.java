package projetara.checkpointing.algo1.messages;

import projetara.util.Message;

public class FinishedRollbackMessage extends Message{

	public FinishedRollbackMessage(long idsrc, long iddest,  int pid) {
		super(idsrc, iddest, "FinishedRollbackMessage", null, pid);
	}
	
}
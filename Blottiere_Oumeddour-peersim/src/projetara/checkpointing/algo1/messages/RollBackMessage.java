package projetara.checkpointing.algo1.messages;

import projetara.util.Message;

public class RollBackMessage extends Message {

	public int getNbSent(){
		return (Integer)this.getContent();
	}
	
	public RollBackMessage(long idsrc, long iddest, int nb_sent, int pid) {
		super(idsrc, iddest, "rollback", nb_sent, pid);
	}

}

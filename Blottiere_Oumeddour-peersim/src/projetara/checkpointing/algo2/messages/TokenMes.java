package projetara.checkpointing.algo2.messages;

import projetara.util.Message;


public class TokenMes extends Message {
	
	public TokenMes(long idsrc, long iddest, int pid) {
		super(idsrc, iddest, "", null, pid);
	}
	
	@Override
	public String toString() {
		return "TokenMessage( from="+getIdSrc()+", to = "+getIdDest()+")";
	}
}

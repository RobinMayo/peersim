package projetara.checkpointing.algo1.messages;

import java.util.ArrayList;
import java.util.List;

import projetara.util.Message;

public class ReplyAskMissingMessMessage extends Message{
	
	
	public ReplyAskMissingMessMessage(long idsrc, long iddest, List<WrappingMessage> messages, int pid) {
		super(idsrc, iddest, "ReplyAskMissingMessMessage", new ArrayList<WrappingMessage>(messages), pid);
		
	}

	@SuppressWarnings("unchecked")
	public List<WrappingMessage> getMissingMessages(){
		return (List<WrappingMessage>) getContent();
	}
	
	
}
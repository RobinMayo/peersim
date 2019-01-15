package projetara.util;

public class Message {
	private final Object content;
	private final String tag;
	private final long idsrc;
	private final long iddest;
	private final int pid;
	public Object getContent() {
		return content;
	}
	public String getTag() {
		return tag;
	}
	public long getIdSrc() {
		return idsrc;
	}
	public long getIdDest() {
		return iddest;
	}
	public int getPid(){
		return pid;
	}
	
	
	public Message(long idsrc, long iddest, String tag, Object content, int pid){
		this.content=content;
		this.tag=tag;
		this.iddest=iddest;
		this.idsrc=idsrc;
		this.pid=pid;
		
	}
		
}

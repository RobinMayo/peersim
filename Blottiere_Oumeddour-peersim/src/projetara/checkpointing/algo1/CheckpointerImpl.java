package projetara.checkpointing.algo1;

import static projetara.util.Constantes.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import projetara.checkpointing.Checkpointable;
import projetara.checkpointing.Checkpointer;
import projetara.checkpointing.NodeState;
import projetara.checkpointing.algo1.messages.AskMissingMessMessage;
import projetara.checkpointing.algo1.messages.FinishedRollbackMessage;
import projetara.checkpointing.algo1.messages.ReplyAskMissingMessMessage;
import projetara.checkpointing.algo1.messages.RollBackMessage;
import projetara.checkpointing.algo1.messages.WrappingMessage;
import projetara.util.Message;


public class CheckpointerImpl implements Checkpointer, EDProtocol, Transport {


	

	private static final String PAR_TRANSPORT = "transport";
	private static final String PAR_CHECKPOINTABLE = "checkpointable";
	private static final String PAR_TIMECHECKPOINTING = "timecheckpointing";
	
	
	private final int checkpointable_id;
	private final int transport;
	private final int protocol_id;
	private final long timecheckpointing;
	
	//attributs pour compter le nombre de messages entrants et sortants
	private Map<Long,Integer> sent;
	private Map<Long,Integer> rcvd;
	//attribut pour sauvegarder les messages envoyés depuis le dernier checkpoint
	private Map<Long,List<WrappingMessage>> sent_messages;
	
	
	
	//ATtributs de sauvegarde
	private Stack<NodeState> states;
	private Stack<Map<Long,Integer>> saved_sent;
	private Stack<Map<Long,Integer>> saved_rcvd;
	private Stack<Map<Long,List<WrappingMessage>>> saved_sent_messages;
	
	//attributs pour l'algo de recovery
	private boolean is_recovery=false;
	private int nb_remaining_broadcast_rollback;
	private int nb_remaining_received_rollback;
	private int nb_remaining_finished_rollback;
	private List<WrappingMessage> message_to_replay_after_recovery;
	private int nb_remaining_replyrecovery;

	
	
	public CheckpointerImpl(String prefix) {
		String tmp[]=prefix.split("\\.");
		protocol_id=Configuration.lookupPid(tmp[tmp.length-1]);
		transport=Configuration.getPid(prefix+"."+PAR_TRANSPORT);
		
		checkpointable_id=Configuration.getPid(prefix+"."+PAR_CHECKPOINTABLE);
		timecheckpointing=Configuration.getLong(prefix+"."+PAR_TIMECHECKPOINTING);
		
	}
	
	public Object clone(){
		CheckpointerImpl res= null;
		try { res = (CheckpointerImpl) super.clone();}
		catch( CloneNotSupportedException e ) {} // never happens
		res.states= new Stack<>(); 		
		res.sent=new HashMap<>();
		res.rcvd=new HashMap<>();
		for(int i=0;i<Network.size();i++){
			res.sent.put(new Long(i), 0);
			res.rcvd.put(new Long(i), 0);
		}
		
		
		res.saved_rcvd=new Stack<>();
		res.saved_sent=new Stack<>();
		res.sent_messages=new HashMap<>();
		res.saved_sent_messages=new Stack<>();
		
		res.message_to_replay_after_recovery=new ArrayList<>();
		
		next_turn(CommonState.getNode());
		return res;
	}
	
	@Override
	public long getLatency(Node src, Node dest) {
		return ((Transport) src.getProtocol(transport)).getLatency(src, dest);
	}
	

	@Override
	public void processEvent(Node node, int pid, Object event) {
		if(protocol_id != pid){
			throw new RuntimeException("Receive an event for wrong protocol");
		}
		if(event instanceof String){
			String ev= (String) event;
			if(ev.equals("loop")){
				loop(node);
			}else{
				throw new RuntimeException("Receive unknown type event");
			}
		}else if(event instanceof WrappingMessage){
			receiveWrappingMessage(node, (WrappingMessage) event);
		}else if(event instanceof RollBackMessage){
			receiveRollBackMessage(node, (RollBackMessage)event);
		}else if(event instanceof FinishedRollbackMessage){
			receiveFinishedRollbackMessage(node,(FinishedRollbackMessage)event);
		}else if(event instanceof AskMissingMessMessage){
			receiveAskMissingMessMessage(node, (AskMissingMessMessage) event);
		}else if(event instanceof ReplyAskMissingMessMessage){
			receiveReplyAskMissingMessMessage(node, (ReplyAskMissingMessMessage)event);
		}else{
			throw new RuntimeException("Receive unknown type event");
		}
	}

	
	
	
	

	private void receiveWrappingMessage(Node host, WrappingMessage wm){
		Message m = wm.getMessage(); 
		long sender = m.getIdSrc();
		if(!is_recovery){
			rcvd.put(sender, rcvd.get(sender)+1);
			((EDProtocol)host.getProtocol(m.getPid())).processEvent(host, m.getPid(), m);
		}
	}
	

	

	@Override
	public void send(Node src, Node dest, Object msg, int pid) {
		Transport t = (Transport) src.getProtocol(transport);
		if(!is_recovery){
			sent.put(dest.getID(), sent.get(dest.getID())+1);
			WrappingMessage mess = new WrappingMessage(src.getID(), dest.getID(), (Message)msg, protocol_id);
			if(!sent_messages.containsKey(dest.getID())){
				sent_messages.put(dest.getID(), new ArrayList<>());
			}
			sent_messages.get(dest.getID()).add(mess);
			t.send(src, dest, mess, protocol_id);
		}
		
	}

	
	@Override
	public void createCheckpoint(Node host){
		log.info("Node "+host.getID()+" BEGIN");

		Checkpointable chk = (Checkpointable) host.getProtocol(checkpointable_id);
		NodeState ns = chk.getCurrentState();
		states.push(ns);
		saved_sent.push(new HashMap<>(sent));
		saved_rcvd.push(new HashMap<>(rcvd));
		saved_sent_messages.push(new HashMap<>(sent_messages));
		sent_messages.clear();
			
		log.fine("Node "+host.getID()+" : saved  state ("+(states.size())+") "+states.peek()+" sent = "
				+saved_sent.peek()+" rcvd = "+saved_rcvd.peek());
	}
	
	
	///////////////////////////////////////// MEthodes pour le Recouvrement
	
	
	@Override
	public void recover(Node host){
		log.fine("Node "+host.getID()+" : start recovering");
		Checkpointable chk = (Checkpointable) host.getProtocol(checkpointable_id);
		chk.suspend();
		is_recovery=true;
		nb_remaining_broadcast_rollback=Network.size()-1;
		nb_remaining_finished_rollback=Network.size()-1;
		if(host.isUp()){
			createCheckpoint(host);
		}else{
			host.setFailState(Fallible.OK);
		}
		log.info("Node "+host.getID()+" : start recovering ("+states.size()+" checkpoints) last state = "+states.peek());
		send_rollback_messages(host);
		
	}
	
	private void send_rollback_messages(Node host) {
		Transport t = (Transport) host.getProtocol(transport);
		for(int j=0;j<Network.size();j++){
			if(j != host.getIndex()){
				long id_dest=Network.get(j).getID();
				int nb_sent = saved_sent.peek().get(id_dest);
				t.send(host, Network.get(j), new RollBackMessage(host.getID(),id_dest, nb_sent,
						protocol_id), protocol_id);
			}
		}
		nb_remaining_broadcast_rollback--;
		nb_remaining_received_rollback = Network.size()-1;
	}
	
	
	
	private void receiveRollBackMessage(Node host, RollBackMessage rbmess){
		log.fine("Node "+host.getID()+" receive RollBackMessage from "+rbmess.getIdSrc());
		nb_remaining_received_rollback--;
		int nb_recv=saved_rcvd.peek().get(rbmess.getIdSrc());
		while(nb_recv > rbmess.getNbSent()){
			delete_checkpoint();
			nb_recv=saved_rcvd.peek().get(rbmess.getIdSrc());
		}		
		
		if(nb_remaining_received_rollback == 0){
			if(nb_remaining_broadcast_rollback != 0){
				send_rollback_messages(host);
			}else{	
				// on  a trouvé la ligne de recouvremen et on le notifie à tout le monde
				for(int i = 0;i< Network.size();i++){
					Node dest = Network.get(i);
					Transport t = (Transport) host.getProtocol(transport);
					if(dest.getID()!=host.getID()){
						t.send(host, dest, new FinishedRollbackMessage(host.getID(), dest.getID(),
								protocol_id), protocol_id);
					}
				}
				
				if(nb_remaining_finished_rollback == 0){//De mon point de vue tout le monde a fini son rollback
					findMessagesToReplay(host);
				}
			}
		}
	}
	
	
	
	private void delete_checkpoint() {
		states.pop();
		saved_sent.pop();
		saved_rcvd.pop();
		saved_sent_messages.pop();
		
	}	
	
	private void receiveFinishedRollbackMessage(Node host, FinishedRollbackMessage m) {
		nb_remaining_finished_rollback--;
		if(nb_remaining_finished_rollback == 0){//De mon point de vue tout le monde a fini son rollback
			findMessagesToReplay(host);
		}
	}
	
	
	
	
	private void findMessagesToReplay(Node host){		
		Transport t = (Transport) host.getProtocol(transport);
		nb_remaining_replyrecovery=Network.size()-1;
		message_to_replay_after_recovery.clear();
		for(int i=0; i< Network.size();i++){
			Node dest = Network.get(i);
			if(dest.getID() != host.getID()){
				t.send(host, dest, new AskMissingMessMessage(host.getID(), dest.getID(),
						saved_rcvd.peek().get(dest.getID()) , protocol_id), protocol_id);
			}
			
		}
	}
	
	private void receiveAskMissingMessMessage(Node host, AskMissingMessMessage amess){
		log.fine("Node "+host.getID()+" receive AskMissingMessMessage from "+amess.getIdSrc());
		Transport t = (Transport) host.getProtocol(transport);
		int nb_sent =  this.saved_sent.peek().get(amess.getIdSrc());
		int nb_rcv = amess.getNbRcv();
		if( nb_rcv > nb_sent){
			throw new RuntimeException("Error : inconcistency in cover line");
		}
		List<WrappingMessage> missing_mess = new ArrayList<>();
		if(nb_rcv < nb_sent){
			int nb_missing = nb_sent-nb_rcv;
			Stack<Map<Long,List<WrappingMessage>>> tmp = new Stack<>();;
			do{
				List<WrappingMessage> l = this.saved_sent_messages.peek().get(amess.getIdSrc());
				while(l==null){
					tmp.push(saved_sent_messages.pop());
					l = this.saved_sent_messages.peek().get(amess.getIdSrc());
				}
				int debut = Math.max(0, l.size() - nb_missing);
				for(int i = debut  ; i< l.size();i++){
					missing_mess.add(l.get(i));
					nb_missing--;
				}
			}while(nb_missing>0);
			
			while(!tmp.isEmpty()){
				saved_sent_messages.push(tmp.pop());
			}
		}
		
		for(int i=0;i< Network.size();i++){
			Node dest=Network.get(i);
			if( dest.getID()== amess.getIdSrc()){
				t.send(host, dest, new ReplyAskMissingMessMessage(host.getID(), amess.getIdSrc(),
						missing_mess, protocol_id), protocol_id);
				break;
			}
		}
		
	}
	
	
	private void receiveReplyAskMissingMessMessage(Node host, ReplyAskMissingMessMessage reply){
		nb_remaining_replyrecovery--;
		this.message_to_replay_after_recovery.addAll(reply.getMissingMessages());
		if(nb_remaining_replyrecovery == 0){
			stop_recover(host);
		}
	}
	
	
	
	private void stop_recover(Node host) {
		Checkpointable chk = (Checkpointable) host.getProtocol(checkpointable_id);
		chk.resume();
		is_recovery=false;
		this.sent=new HashMap<>(saved_sent.peek());
		this.rcvd=new HashMap<>(saved_rcvd.peek());
		this.sent_messages.clear();
		log.info("Node "+host.getID()+" : end recovering (recover from checkpoint "+states.size()+")"+
				"  state = "+states.peek()+" nb reply messages = " +message_to_replay_after_recovery.size());
		chk.restoreState(states.peek());		
		for( WrappingMessage wm : message_to_replay_after_recovery){
			receiveWrappingMessage(host, wm);
		}
		
		
	}
	
	
	
	
	////////////////////////////////////////// Fin des methodes de recouvrement
	
	
	
	
	public void loop(Node host) {
		if(CommonState.r.nextInt()%2 == 0 && ! is_recovery){
			createCheckpoint(host);
		}
		next_turn(host);
		return;
	}
	
	private void next_turn(Node host){
		long min = (long )(timecheckpointing * 0.8);
		long max = (long )(timecheckpointing * 1.2);
		long res = CommonState.r.nextLong(max+min)+min;
		EDSimulator.add(res,"loop", host,protocol_id);
	}

	
}

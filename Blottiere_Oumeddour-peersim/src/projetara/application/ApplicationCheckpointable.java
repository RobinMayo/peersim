package projetara.application;

import java.util.ArrayDeque;
import java.util.Queue;

import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import projetara.checkpointing.Checkpointable;
import projetara.checkpointing.NodeState;


public class ApplicationCheckpointable extends Application implements EDProtocol, Checkpointable {
	
	//variable permettant de savoir si l'application est suspendue ou pas pour cause de recovery
	private boolean is_suspended=false;
	
	public ApplicationCheckpointable(String prefix) {
		super(prefix);
	}
	
	public Object clone(){
		ApplicationCheckpointable res= null;
		res = (ApplicationCheckpointable) super.clone();
		return res;
	}
	
	
	@Override
	public void suspend() {
		is_suspended=true;		
	}


	
	@Override
	public void resume() {
		is_suspended=false;
		id_execution++;
	}
	

	
	@Override
	public NodeState getCurrentState(){
		NodeState res = new NodeState();
		res.saveVariable("state",state.name());
		res.saveVariable("next", new ArrayDeque<Long>(next) );
		res.saveVariable("last", new Long(last));
		res.saveVariable("nb_cs",nb_cs);
		res.saveVariable("global_counter", global_counter );
		return res;
	}
	
	
	@Override
	public void processEvent(Node node, int pid, Object event) {
		//si on est suspendu alors on ne traite plus d'event au niveau applicatif car on est en recovery
		if(!is_suspended){
			super.processEvent(node, pid, event);;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void restoreState(NodeState restored_state){
		
		this.next = new ArrayDeque<>( (Queue<Long> )restored_state.loadVariable("next" ))  ;
		this.last = (Long) restored_state.loadVariable("last");	
		this.nb_cs=(Integer) restored_state.loadVariable("nb_cs");
		this.global_counter=(Integer)restored_state.loadVariable("global_counter");
		changestate(CommonState.getNode(), State.valueOf(((String)restored_state.loadVariable("state"))));
		
		
	}
	
	

}

package projetara.application;

import static projetara.util.Constantes.log;

import java.util.ArrayDeque;
import java.util.Queue;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import projetara.application.InternalEvent.TypeEvent;
import projetara.util.Message;


public class Application implements EDProtocol {
		
		//Nom des arguments du fichiers de configuration
		private static final String PAR_TRANSPORT = "transport";
		private static final String PAR_TIME_CS = "timeCS";
		private static final String PAR_TIME_BETWEEN_CS = "timeBetweenCS";
		
		//constantes de l'algorithme
		public static final long initial_owner=0L;
		public static final long nil=-2L;
		
		//tag des messages
		public static final String REQUEST_TAG = "request";
		public static final String TOKEN_TAG = "token";
		
		//etats possibles du noeud dans l'application
		public static enum State{tranquil, requesting, inCS}
		
		//paramètres de l'algorithme lus depuis le fichier de configuration
		protected final long timeCS;
		protected final long timeBetweenCS;
		protected final int transport_id;
		protected final int protocol_id;
		
		//variables d'état de l'application
		protected State state;
		protected Queue<Long> next;
		protected long last;
		protected int nb_cs=0;//permet de compter le nombre de section critiques exécutées par le noeud

		// compteur qui sera inclu dans le message jeton, sa valeur est égale à la dernière valeur connue 
		// (i.e. depuis la dernière fois où le noeud a vu passer le jeton) 
		// ATTENTION, cette variable n'est pas globale, elle est propre à chaque noeud mais ils ne peuvent
		// la modifier uniquement lorsqu'ils possèdent le jeton
		protected int global_counter=0;
		
		// ********** Exercice 1 question 4 :
		// Métriques :
		protected int nbMessages;
		protected int nbRequest;
		protected long nodeRequestingTime;
		private long beginRequestTime;
		private static long beginningTime = System.currentTimeMillis();
		protected static long tokenBeginStateTime = beginningTime;
		protected static long tokenUnusedTime = 0;
		protected static long tokenUsageTime = 0;
		protected static long tokenTransmissionTime = 0;
		
		protected int id_execution; // permet d'identifier l'id d'exécution, incrémenter si
		// l'application est suspendue (toujours constant dans cette classe)
		
		
		public Application(String prefix) {
			String tmp[]=prefix.split("\\.");
			protocol_id=Configuration.lookupPid(tmp[tmp.length-1]);
			
			transport_id=Configuration.getPid(prefix+"."+PAR_TRANSPORT);
			timeCS=Configuration.getLong(prefix+"."+PAR_TIME_CS);
			timeBetweenCS=Configuration.getLong(prefix+"."+PAR_TIME_BETWEEN_CS);
			
			beginRequestTime = 0;
			nodeRequestingTime = 0;
		}
		
		public Object clone(){
			Application res= null;
			try { res = (Application) super.clone(); }
			catch( CloneNotSupportedException e ) {} // never happens
			res.initialisation(CommonState.getNode());
			
			return res;
		}
		
		@Override
		public void processEvent(Node node, int pid, Object event) {
			log.finer("Node "+node.getID()+" BEGIN");
			log.finest("Node "+node.getID()+" last : "+last);
			
			if(protocol_id != pid){
				throw new RuntimeException("Receive an event for wrong protocol");
			}
			
			if(event instanceof InternalEvent){
				InternalEvent ev= (InternalEvent) event;
				if(ev.getDate() == id_execution){
					log.finer("Node "+node.getID()+" ev.getType() : " + ev.getType());
					switch(ev.getType()) {
					case release_cs:
						nb_cs++;
						this.releaseCS(node);
						break;
					case request_cs:
						this.requestCS(node);
						break;
					default:
						throw new RuntimeException("Receive unknown type event");
					}
				}else{
					log.warning(node.getID()+" : ignoring obsolete event "+ev);
				}
			}else if(event instanceof Message) {
				Message m = (Message) event;
				if(m.getTag().equals(REQUEST_TAG)){
					this.receive_request(node, m.getIdSrc(), (Long)m.getContent());
				}else if(m.getTag().equals(TOKEN_TAG)){
					if(! (m instanceof TokenMessage))
						throw new RuntimeException("Receive a message with tag token but it is not"
								+ "an instance of TokenMessage");
					TokenMessage tm= (TokenMessage)m;
					this.receive_token(node, tm.getIdSrc(), tm.getNext(),tm.getCounter());
				}else{
					throw new RuntimeException("Receive unknown type Message");
				}
			}else {
				throw new RuntimeException("Receive unknown type event");
			}
		}
		
		
		/////////////////////////////////////////// METHODES DE L'ALGORITHME ///////////////////////
		private void executeCS(Node host){
			log.finer("Node "+host.getID()+" BEGIN");
			log.info("Node "+host.getID()+" executing its CS num "+nb_cs+" : next= "+next.toString());
			global_counter++;
			log.info("Node "+host.getID()+" global counter = "+global_counter);
		}
		
		private void initialisation(Node host) {
			log.finer("Node "+host.getID()+" BEGIN");
			changestate(host,State.tranquil);
			next=new ArrayDeque<Long>();
			if(host.getID() == initial_owner){
				last=nil;
				log.fine("Node "+host.getID()+" last=nil");
			}else{
				last=initial_owner;
				log.fine("Node "+host.getID()+" last=initial_owner");
			}
		}
		
		private void requestCS(Node host){
			log.finer("Node "+host.getID()+" BEGIN");
			changestate(host,State.requesting);
			if(last != nil){
				log.finer("Node "+host.getID()+" last : "+last);
				Transport tr= (Transport) host.getProtocol(transport_id);
				Node dest = Network.get((int)last);
				tr.send(host,dest, new Message(host.getID(), dest.getID(),  REQUEST_TAG,
						host.getID(), protocol_id), protocol_id);
				nbRequest++;
				last=nil;
				return;//on simule un wait ici
			}
			changestate(host,State.inCS);
			//DEBUT CS
			log.fine("Node "+host.getID()+" ***** BEGIN CS ! *****");
			tokenUnusedTime += System.currentTimeMillis() - tokenBeginStateTime;
			log.warning("tokenUnusedTime : "+tokenUnusedTime+", tokenBeginStateTime : "+tokenBeginStateTime);
			tokenBeginStateTime = System.currentTimeMillis();
			log.finest("Node "+host.getID()+" END");
		}
		
		private void releaseCS(Node host){
			log.finer("Node "+host.getID()+" BEGIN");
			log.fine("Node "+host.getID()+" next="+next);
			changestate(host,State.tranquil);
			
			tokenUsageTime += System.currentTimeMillis() - tokenBeginStateTime;
			tokenBeginStateTime = System.currentTimeMillis();
			
			if(!next.isEmpty()){
				last=getLast(next);
				long next_holder = next.poll();//dequeue
				Transport tr= (Transport) host.getProtocol(transport_id);
				Node dest = Network.get((int)next_holder);
				log.finer("Node "+host.getID()+" last : "+last+", next_holder : "+next_holder);
				tr.send(host, dest,new TokenMessage(host.getID(), dest.getID(),
						new ArrayDeque<Long>(next), global_counter, protocol_id)   , protocol_id);
				nbMessages++;
				log.finer("Node "+host.getID()+" send token("+next+") to "+dest.getID());
				next.clear();
				log.fine("Node "+host.getID()+" next="+next);
			}
			log.fine("Node "+host.getID()+" ***** END CS ! *****");
			shareToEndControler();
			log.finest("Node "+host.getID()+" END");
		}
		
		
		private void  receive_request(Node host, long from, long requester){
			log.finer("Node "+host.getID()+" BEGIN");
			log.fine("Node "+host.getID()+" receive request message from Node "+from+" for Node "+requester);
			Transport tr= (Transport) host.getProtocol(transport_id);
			
			if(last == nil){
				if(state != State.tranquil){	
					next.add(requester);
					
				}else{
					Node dest = Network.get((int)requester);
					tr.send(host, dest,
							new TokenMessage(host.getID(), dest.getID(), new ArrayDeque<Long>(),
									global_counter, protocol_id), protocol_id);
					log.fine("Node "+host.getID()+" send token("+next+") to "+dest.getID()
							+" (no need)");
					last=requester;
				}
			}else{
				Node dest = Network.get((int)last);
				tr.send(host, dest,
						new Message(host.getID(), dest.getID(),  REQUEST_TAG, requester, protocol_id),
						protocol_id);
				nbRequest++;
				log.fine("Node "+host.getID()+" send Message("+REQUEST_TAG+") to "+dest.getID());
				last=requester;
			}
			log.finest("Node "+host.getID()+" last : "+last);
			log.finest("Node "+host.getID()+" END");
		}
		
		private void receive_token(Node host, long from, Queue<Long> remote_queue, int counter){
			log.finer("Node "+host.getID()+" BEGIN");
			log.fine("Node "+host.getID()+" receive token message ("+remote_queue.toString()+
					", counter = "+counter+") from Node "+from+" next ="+next.toString());
			tokenTransmissionTime += System.currentTimeMillis() - tokenBeginStateTime;
			tokenBeginStateTime = System.currentTimeMillis();
			
			global_counter=counter;
			remote_queue.addAll(next);
			next=remote_queue;
			log.finer("Node "+host.getID()+" next="+next);
			changestate(host, State.inCS);
			log.fine("Node "+host.getID()+" ***** BEGIN CS ! *****");
		}
		
		
        /////////////////////////////////////////// METHODES UTILITAIRES ///////////////////////////
		protected void changestate(Node host, State s) {
			log.finer("Node "+host.getID()+" BEGIN");
			log.fine("Node "+host.getID()+" state : "+s.toString());
			this.state = s;
			switch(this.state){
			case inCS:
				nodeRequestingTime += System.currentTimeMillis() - beginRequestTime;
				executeCS(host);
				schedule_release(host);
				break;
			case tranquil:
				schedule_request(host);
				break;
			default:
				beginRequestTime = System.currentTimeMillis();
			}
		}

		private static long getLast(Queue<Long> q) {
			Object tmp[] = q.toArray();
			return (Long)tmp[tmp.length-1];
		}
		
		private void schedule_release(Node host) {
			log.finer("Node "+host.getID()+" BEGIN");
			long min = (long )(timeCS * 0.8);
			long max = (long )(timeCS * 1.2);
			long res = CommonState.r.nextLong(max-min)+min;
			
			EDSimulator.add(res, new InternalEvent(TypeEvent.release_cs, id_execution), host, protocol_id);
		}
		
		private void schedule_request(Node host) {
			log.finer("Node "+host.getID()+" BEGIN");
			long min = (long )(timeBetweenCS * 0.8);
			long max = (long )(timeBetweenCS * 1.2);
			long res = CommonState.r.nextLong(max-min)+min;
			
			EDSimulator.add(res, new InternalEvent(TypeEvent.request_cs, id_execution), host, protocol_id);
		}
		
		// WARNING : To call only once in Critical Section !
		private void shareToEndControler() {
			
			log.info("Exectution time = "+(System.currentTimeMillis() - beginningTime));
			
			// Nombre de messages applicatifs :
			EndControler.nbCs = global_counter;
			EndControler.nbRequest += nbRequest;
			EndControler.nbMessages += nbMessages;
			
			// Temps passé dans l'état requesting :
			EndControler.nodeRequestingTime += nodeRequestingTime;
			
			// Temps que le jeton passe dans chacun de ses états :
			EndControler.tokenUnusedTime = tokenUnusedTime;
			EndControler.tokenUsageTime = tokenUsageTime;
			EndControler.tokenTransmissionTime = tokenTransmissionTime;
		}
}

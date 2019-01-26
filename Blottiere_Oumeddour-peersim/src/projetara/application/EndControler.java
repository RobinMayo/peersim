package projetara.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


// ********** Exercice 1 question 4 :
public class EndControler implements Control {
	
	//Nom des arguments du fichiers de configuration
	private static final String APPLICATION = "application";
	
	protected final int protocol_id;
	
	// Métriques :
	// Nombre de messages applicatifs :
	//protected static int timeCS = 0;
	//protected static int timeBetweenCS = 0;
	protected static int nbCs = 0;
	protected static float nbTokenByNode = 0;
	protected static float nbTokenByCs = 0;
	protected static int nbToken = 0;
	protected static float nbRequestByNode = 0;
	protected static float nbRequestByCs = 0;
	
	// Temps passé dans l'état requesting :
	protected static long nodeRequestingTime = 0;
	protected static float nodeAverageRequestingTime = 0;
	
	// Temps que le jeton passe dans chacun de ses états :
	protected static long tokenUnusedTime = 0;
	protected static long tokenUsageTime = 0;
	protected static long tokenTransmissionTime = 0;
	protected static float totalTime = 0;

	public EndControler(String prefix) {
		protocol_id = Configuration.getPid(prefix+"."+APPLICATION);
	}
	
	@Override
	public boolean execute() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < Network.size(); i++) {
			Node node = Network.get(i);
			Application application = (Application)node.getProtocol(protocol_id);
			
			nbCs += application.nb_cs;
			nbToken += application.nbToken;
			
			if(application.nb_cs != 0) {
				nbTokenByNode += application.nbToken / application.nb_cs; 
				nbRequestByNode += application.nbRequest / application.nb_cs;
			} else {
				nbTokenByNode += application.nbToken; 
				nbRequestByNode += application.nbRequest;
			}
			nodeRequestingTime += application.nodeRequestingTime;
		}

		// Temps que le jeton passe dans chacun de ses états :
		tokenUnusedTime = Application.tokenUnusedTime;
		tokenUsageTime = Application.tokenUsageTime;
		tokenTransmissionTime = Application.tokenTransmissionTime;
		
		nbTokenByCs = nbTokenByNode / Network.size();
		nbRequestByCs = nbRequestByNode / Network.size();
		nodeAverageRequestingTime = nodeRequestingTime / nbCs;
		System.out.println(this.toString());
		writeFile();
		return false;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("\n\n********** FIN de l'Application **********\n\n");
		str.append("\t\tMétriques :\n\n");
		
		str.append("Nombre de messages applicatifs :\n");
		str.append("nbCs = "+nbCs+", nbToken = "+nbToken+", nbTokenByCs = "+nbTokenByCs+
				", nbRequestByCs = "+nbRequestByCs+", ");
		str.append("nbTokenByNode = "+nbTokenByNode+", nbRequestByNode = "+nbRequestByNode+".\n\n");
		
		str.append("Temps passé dans l'état requesting :\n");
		str.append("nodeRequestingTime = "+nodeRequestingTime+
				" nodeAverageRequestingTime = "+nodeAverageRequestingTime+".\n\n");
		
		str.append("Temps que le jeton passe dans chacun de ses états :\n");
		str.append("tokenUnusedTime = "+tokenUnusedTime+", tokenUsageTime = "+tokenUsageTime+
				", tokenTransmissionTime = "+tokenTransmissionTime+".\n\n");
		return str.toString();
	}
	
	private String toFile() {
		StringBuilder str = new StringBuilder();
		str.append(nbTokenByCs+" "+nbRequestByCs+"\n");
		str.append(nodeAverageRequestingTime+"\n");
		
		totalTime = tokenUnusedTime + tokenUsageTime + tokenTransmissionTime;
		str.append(((tokenUnusedTime / totalTime) * 100)+" "+((tokenUsageTime / totalTime) * 100)+
				" "+((tokenTransmissionTime / totalTime) * 100)+"\n\n");
		
		str.append("********** Format : **********\n\n");
		str.append("nbTokenByCs nbRequestByCs\n");
		str.append("nodeAverageRequestingTime\n");
		str.append("tokenUnusedTime tokenUsageTime tokenTransmissionTime\n");
		
		return str.toString();
	}
	
	private void writeFile() {
		String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
		String fileName = "Log_exercice_1_"+date;
		FileWriter fw;
		BufferedWriter output;
		
		try {
			fw = new FileWriter("Logs/"+fileName, true);
			output = new BufferedWriter(fw);
			output.write(toFile());
			output.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

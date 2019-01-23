package projetara.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import peersim.core.Control;
import peersim.core.Network;

// ********** Exercice 1 question 4 :
public class EndControler implements Control {
	
	// Métriques :
	// Nombre de messages applicatifs :
	protected static int nbCs;
	protected static int nbMessages;
	protected static int nbMessagesByCs;
	protected static int nbRequest;
	protected static int nbRequestByCs;
	
	// Temps passé dans l'état requesting :
	protected static long nodeRequestingTime;
	protected static long nodeAverageRequestingTime;
	
	// Temps que le jeton passe dans chacun de ses états :
	protected static long tokenUnusedTime;
	protected static long tokenUsageTime;
	protected static long tokenTransmissionTime;
	protected static float totalTime;

	public EndControler(String string) {}
	
	@Override
	public boolean execute() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		nbMessagesByCs = nbMessages / Network.size();
		nbRequestByCs = nbRequest / Network.size();
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
		str.append("nbCs = "+nbCs+", nbMessagesByCs = "+nbMessagesByCs+
				", nbRequestByCs = "+nbRequestByCs+", ");
		str.append("nbMessages = "+nbMessages+", nbRequest = "+nbRequest+".\n\n");
		
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
		str.append(nbMessagesByCs+" "+nbRequestByCs+"\n");
		str.append(nodeAverageRequestingTime+"\n");
		
		totalTime = tokenUnusedTime + tokenUsageTime + tokenTransmissionTime;
		str.append(((tokenUnusedTime / totalTime) * 100)+" "+((tokenUsageTime / totalTime) * 100)+
				" "+((tokenTransmissionTime / totalTime) * 100)+"\n\n");
		
		str.append("********** Format : **********\n\n");
		str.append("nbMessagesByCs nbRequestByCs\n");
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

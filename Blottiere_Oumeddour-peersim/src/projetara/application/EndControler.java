package projetara.application;

import peersim.core.Control;

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

	public EndControler(String string) {}
	
	@Override
	public boolean execute() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		nbMessagesByCs = nbMessages / nbCs;
		nbRequestByCs = nbRequest / nbCs;
		nodeAverageRequestingTime = nodeRequestingTime / nbCs;
		System.out.println(this.toString());
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
	
	public String toFile() {
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
}

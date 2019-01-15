package projetara.util;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import peersim.config.Configuration;
import peersim.core.Control;

public final class Constantes implements Control {
	
	/*Ne peut être ni instanciée ni étendue*/
	public Constantes(String prefix) {
		log.setLevel(Level.parse(Configuration.getString(prefix + "." + PAR_LOG_LEVEL)));
	}
	
	//private Constantes() {}
	
	@Override
	public boolean execute() {
		return false;
	}

	//Logger pour le debug : niveau INFO = affiche les sections critiques, niveau FINE = affiche les évenements
	public static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("log");
	private static final String PAR_LOG_LEVEL = "loglevel";//niveau INFO par defaut

	static {
		
		Handler handlerObj = new ConsoleHandler();
		log.setUseParentHandlers(false);
		handlerObj.setLevel(Level.ALL);
		handlerObj.setFormatter(new Formatter() {

			@Override
			public String format(LogRecord record) {
				//initial was "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n" 
				//"%4$s : %5$s  (%2$s)%n"
				String format="%2$-30s %7$4s %4$-6s : %5$s%n";
				String source;
				String lineNumber = 
						String.valueOf(Thread.currentThread().getStackTrace()[8].getLineNumber());
				if (record.getSourceClassName() != null) {
					source = record.getSourceClassName();
					String tmp[] = source.split("\\.");
					source=tmp[tmp.length-1];
					if (record.getSourceMethodName() != null) {
						source += " " + record.getSourceMethodName();
					}
				} else {
					source = record.getLoggerName();
				}
				source = source.length() > 30 ? source.substring(0, 30) : source;
				String message = formatMessage(record);
				String throwable = "";
				return String.format(format,
						new Date(record.getMillis()),
						source,
						record.getLoggerName(),
						record.getLevel(),
						message,
						throwable,
						lineNumber);
			}
		});

		log.addHandler(handlerObj);
	}
}

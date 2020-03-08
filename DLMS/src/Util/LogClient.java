package Util;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogClient {
public Logger logger= Logger.getLogger("LogManager");;
	
	public LogClient(String clientPath,String fileName) {
		try {
			String fullpath=Constants.LOG_DIR+clientPath+fileName+".log";
		FileHandler	fileHandler = new FileHandler(fullpath);
			SimpleFormatter formatter = new SimpleFormatter();
	        fileHandler.setFormatter(formatter);
	        logger.setUseParentHandlers(false);
			logger.addHandler(fileHandler);		
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Exception in logger :: "+e.getMessage());
		}
	}
}

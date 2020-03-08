package Util;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogManager {
	public Logger logger= Logger.getLogger("LogManager");;
	
	public LogManager(String serverName) {
		try {
			String fullpath=Constants.LOG_DIR+serverName+"\\"+serverName+".log";
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

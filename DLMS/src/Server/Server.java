package Server;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.logging.Level;

import Util.Constants;
import Util.LogManager;
import Util.Servers;

public class Server {
	
	static IServerInterface stubCON, stubMCG, stubMON;
	static ServerImpl serverCON,serverMCG,serverMON;
	static HashMap<String,ServerImpl> serverData;
	public static void main(String[] args) throws Exception
	{
		serverSetup();
		
		registerServers();
		LogManager logManager = new LogManager("MainServer");
		System.out.println("Servers Started");
		logManager.logger.log(Level.INFO, "Server Started");
		AddData();
	}
	
	public static void serverSetup() throws RemoteException, IOException{
		new File(Constants.LOG_DIR).mkdirs();
		new File(Constants.LOG_DIR+Servers.CON.getserverName().toString()).mkdirs();
		new File(Constants.LOG_DIR+Servers.MCG.getserverName().toString()).mkdir();
		new File(Constants.LOG_DIR+Servers.MON.getserverName().toString()).mkdir();			
		new File(Constants.LOG_DIR+"MainServer").mkdir();
		
		serverCON=new ServerImpl(Servers.CON);
		serverMCG=new ServerImpl(Servers.MCG);
		serverMON=new ServerImpl(Servers.MON);
		serverData = new HashMap<>();
		serverData.put("CON",serverCON);
		serverData.put("MCG",serverMCG);
		serverData.put("MON",serverMON);

	}
	
	public static void AddData() throws IOException {
		serverCON.addItem("CONM1234", "CON7865", "Distributed Systems", 9);
		serverMCG.addItem("MCGM1234", "MCG7865", "Distributed Systems", 8);
		serverMON.addItem("MONM1234", "MON7865", "Distributed Systems", 7);
	}
	
	public static void registerServers() {
		Registry registryCON,registryMCG,registryMON;
		try {

			registryCON = LocateRegistry.createRegistry(Constants.CON_PORT_NUM);
			registryMCG = LocateRegistry.createRegistry(Constants.MCG_PORT_NUM);
			registryMON = LocateRegistry.createRegistry(Constants.MON_PORT_NUM);
			registryCON.bind("CON",serverCON);
			registryMCG.bind("MCG",serverMCG);
			registryMON.bind("MON",serverMON);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}

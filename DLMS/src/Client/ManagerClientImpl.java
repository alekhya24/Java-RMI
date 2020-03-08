package Client;

import java.io.File;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.logging.Level;

import Server.IServerInterface;
import Util.Constants;
import Util.LogClient;
import Util.Servers;

public class ManagerClientImpl {
	static Registry registryCON;
	static Registry registryMCG;
	static Registry registryMON;

	Util.LogClient logClient = null;
	IServerInterface iServer = null;
	
	static {
		try {
			registryCON = LocateRegistry.getRegistry("localhost", Constants.CON_PORT_NUM);
			registryMCG = LocateRegistry.getRegistry("localhost", Constants.MCG_PORT_NUM);
			registryMON = LocateRegistry.getRegistry("localhost", Constants.MON_PORT_NUM);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	ManagerClientImpl(Servers server, String managerId)

			throws AccessException, RemoteException, NotBoundException {
		String folder="";
		if (server == Servers.CON) {
			folder=Servers.CON.getserverName().toString();
			iServer = (IServerInterface) registryCON.lookup(server.toString());
		} else if (server == Servers.MCG) {
			folder=Servers.MCG.getserverName().toString();
			iServer = (IServerInterface) registryMCG.lookup(server.toString());
		} else if (server == Servers.MON) {
			folder=Servers.MON.getserverName().toString();
			iServer = (IServerInterface) registryMON.lookup(server.toString());
		}
		boolean mgrID = new File(Constants.LOG_DIR+folder +"\\"+managerId).mkdir();
		logClient = new LogClient(folder+"\\"+managerId+"\\",managerId);
	}
	
	
	public String addItem(String managerId,String itemName,int quantity) throws RemoteException
	{
		logClient.logger.info("ManagerClient: Initiating Add Item");
		String result = "";
		int itemNumber=(int)(Math.random()*9000)+1000;
		String itemId = managerId.substring(0, 3) + (itemNumber);
		result = iServer.addItem(managerId, itemId, itemName, quantity);
		logClient.logger.log(Level.INFO, result);
		return result;
	}
	
	public String listItemAvailability(String managerId) throws RemoteException
	{
		logClient.logger.info("ManagerClient: Initiating listItemAvailability");	
		String output=iServer.listItemAvailability(managerId);
			logClient.logger.log(Level.INFO, output.toString());
		return output;
	}
	
	public String removeItem(String managerId,String itemId,int quantity) throws RemoteException
	{
		logClient.logger.info("ManagerClient: Initiating Remove Item");
		String result=iServer.removeItem(managerId, itemId, quantity);
logClient.logger.log(Level.INFO, result);
		return result;
	}
}

package Client;

import java.io.File;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import Server.IServerInterface;
import Util.Constants;
import Util.LogClient;
import Util.Servers;

public class UserClientImpl {
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


	UserClientImpl(Servers server, String UserId)
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

		boolean usrID = new File(Constants.LOG_DIR+folder+"\\"+UserId).mkdir();
		logClient = new LogClient(folder+"\\"+UserId+"\\",UserId);
	}
	
	public String borrowItem(String userId, String itemId,boolean isWaitlisted) throws RemoteException
	{
		logClient.logger.info("UserClient: Initiating Borrow Item");
		String result = iServer.borrowItem(userId, itemId,isWaitlisted);
		logClient.logger.info("Success");
		return result;
	}
	
	public String findItem(String userId,String itemName) throws RemoteException
	{
		logClient.logger.info("UserClient: Initiating Find Item");
		String result=iServer.findItem(userId, itemName);
		logClient.logger.info(result);
		return result;
	}
	
	public String returnItem(String userId,String itemId) throws RemoteException
	{
		logClient.logger.info("UserClient: Initiating Return Item");
		String result=iServer.returnItem(userId, itemId);
		logClient.logger.info(result);
		return result;
	}
}

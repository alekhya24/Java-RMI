package Server;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface IServerInterface extends Remote {
	//Operations performed by Manager
	public String addItem(String managerID,String itemID,String itemName,int quantity) throws RemoteException;
	public String removeItem (String managerID,String itemID,int quantity) throws RemoteException;
	public String listItemAvailability (String managerID) throws RemoteException;
	
	//Operations performed by user
	public String borrowItem (String userID,String itemID,boolean isWaitlisted) throws RemoteException;
	public String findItem (String userID,String itemName) throws RemoteException;
	public String returnItem (String userID,String itemID) throws RemoteException;

}

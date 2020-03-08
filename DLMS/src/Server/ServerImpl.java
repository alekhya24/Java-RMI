package Server;
import java.io.IOException;
import java.rmi.*;
import java.rmi.server.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.logging.Level;
import java.util.stream.Collectors;
import Model.Item;
import UDP.ServerUDP;
import UDP.UDPRequestProvider;
import Util.LogManager;
import Util.Servers;

public class ServerImpl extends UnicastRemoteObject implements IServerInterface {
	public LogManager logManager;
	public ServerUDP serverUDP;
	public String IPaddress;
	public HashMap<String, Item> dataMap=new HashMap<>();
	public HashMap<String,List<String>> borrowMap=new HashMap<>();
	public HashMap<String,Queue<String>> waitingListMap=new HashMap<>();
	public String location;

	public ServerImpl(Servers libraryLocation) throws RemoteException,IOException {
		super();
		location=libraryLocation.toString();
		logManager=new LogManager(libraryLocation.getserverName().toString().toUpperCase());
		serverUDP = new ServerUDP(libraryLocation, logManager.logger, this);
		serverUDP.start();
	}
	
	public String addItem(String managerID, String itemId, String itemName, int quantity) throws RemoteException {
		logManager.logger.info("Date & Time: " + LocalDateTime.now() +"; REQUEST TYPE:Add Item; REQUEST PARAMETERS: managerId:"+managerID+",itemId:"+itemId+",itemName:"+itemName+",quantity:"+quantity);
		String result="";
			Item item=new Item();
			Item existingItem=null;
			List<String> data=	dataMap.entrySet()
		              .stream()
		              .filter(entry -> Objects.equals(entry.getValue().ItemName, itemName))
		              .map(Map.Entry::getKey)
		              .collect(Collectors.toList());
			String originalItemId=data.stream()
					  .filter(s -> location.equals(s.substring(0, 3)))
					  .findAny()
					  .orElse(null);
			existingItem=originalItemId!=null?dataMap.get(originalItemId):null;
			if(existingItem!=null)
			{
				itemId=existingItem.ItemId;
			existingItem.setInStock(existingItem.InStock+quantity);
	        assignWaitlistingMembers(itemId);
			result="Item "+ existingItem.ItemName+" already exists.Updated existing item quantity.Updated quantity = "+existingItem.InStock;
			logManager.logger.info(result);
			logManager.logger.info("Request Successfully completed");
			}
			else
			{
			String key =itemId;
			item.ItemId=itemId;
			item.ItemName=itemName;
			item.InStock=quantity;
			dataMap.put(key,item);
			result="Item is added " + item.ItemName + " with key: " + key;
			logManager.logger.info("Server Response: "+result);
			logManager.logger.info("Request Successfully completed");
			}
			return result;
	}

	public String removeItem (String managerID,String itemID,int quantity) throws RemoteException
	{
		logManager.logger.info("Date & Time: " + LocalDateTime.now() +"; REQUEST TYPE:Remove Item; REQUEST PARAMETERS: managerId:"+managerID+",itemId:"+itemID+",quantity:"+quantity);
		String result="";
		Item existingItem=dataMap.get(itemID);
		if(existingItem==null)
		{
			logManager.logger.info("Request Failed");
			result= "Item doesn't exist";
		}
		else
		{
		int existingItemQty=existingItem.getInStock();
		 if(quantity<0)
			{
				RemoveFromBorrowList(itemID);
				dataMap.remove(itemID);
				logManager.logger.info("Request Successfully completed");
				result= "Success";
			}
		 else if(quantity<=existingItemQty)
		{
		if(existingItemQty>0)
		{
			int qty=existingItemQty-quantity;
			existingItem.setInStock(qty);
			logManager.logger.info("Request Successfully completed");
			result= "Success";
		}
		else if(existingItemQty==0)
		{
			dataMap.remove(itemID);
			logManager.logger.info("Request Successfully completed");
			result= "Success";
		}
		}
		else
		{
			logManager.logger.info("Request Failed");
			result= "Quantity entered is incorrect";
		}
		

	}	
		return result;
	}
	
	public void RemoveFromBorrowList(String itemID) throws RemoteException
	{
		ArrayList<String> userIds = new ArrayList<>();
		for (Entry<String, List<String>> entry : borrowMap.entrySet()) {
	        if (entry.getValue().contains(itemID)) {
	            userIds.add(entry.getKey());
	        }
	    }
		for (String id : userIds) {
			returnItem(id, itemID);
		}

	}
	
	public String listItemAvailability(String managerID) throws RemoteException {
		logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:List Item Availability; REQUEST PARAMETERS: managerId:"+managerID);
		ArrayList<String> output=new ArrayList<>();
		Collection<Item> data=dataMap.values();
		for (Item item : data) {
			if(item.ItemId.substring(0,3).toString().equals(managerID.substring(0, 3).toString()))
			{
			output.add(item.ItemId+" "+item.ItemName+" "+ item.InStock);
			}
		}
        String finalData= (String)output.stream().collect(Collectors.joining(","));
        String result=finalData.equals(",")?"No records found":finalData.replaceAll(",*$", "");
		logManager.logger.info("ServerResponse : "+output.toString());
		logManager.logger.info("Request Successfully Completed");
		return result;
	}
	
	private String getCurrentServerItemDetails(String itemName){
		String output = "";
		for (Map.Entry<String, Item> entry : this.dataMap.entrySet()) {
			if(entry.getValue().ItemName.equals(itemName))
			{
			output=entry.getKey()+" " +entry.getValue().InStock;
			}
		}
		return output;
	}
	
	public String findItem (String userID,String itemName)
	{
		logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:find Item; REQUEST PARAMETERS: userId:"+userID+",itemName:"+itemName);
        ArrayList<String> recordCount =new ArrayList<>();
        UDPRequestProvider[] req = new UDPRequestProvider[2];
        int counter = 0;
        ArrayList<String> locList = new ArrayList<>();
        locList.add("CON");
        locList.add("MCG");
        locList.add("MON");
        for (String loc : locList) {
            if (loc== this.location) {
            	if(getCurrentServerItemDetails(itemName)!="")
            	{
                recordCount.add(getCurrentServerItemDetails(itemName).trim());
            	}
            } else {
                try {
                	String data="findItem:"+userID+":"+itemName;
                	logManager.logger.info("Request redirecting through UDP to: "+ loc);
                	req[counter] = new UDPRequestProvider(Server.serverData.get(loc),data);
                } catch (IOException e) {
                    logManager.logger.log(Level.SEVERE, e.getMessage());
                }
                req[counter].start();
                
                counter++;
            }
        }
        for (UDPRequestProvider request : req) {
            try {
                request.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(request.getReturnValue()!=null)
            {
            recordCount.add(request.getReturnValue().trim());
            }
        }
        String finalData= (String)recordCount.stream().collect(Collectors.joining(","));
        String result=finalData.equals(",")?"No records found":finalData.replaceAll(",*$", "");
        result=result.startsWith(",")?result.substring(1):result;
        logManager.logger.info("Server Response :"+ result);
        logManager.logger.info("Request Successfully Completed");
        
        return result;
	}
	public String returnItem (String userID,String itemID) throws RemoteException
	{
		logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:return Item; REQUEST PARAMETERS: userId:"+userID+",itemID:"+itemID);
		String result="";
		boolean isReturned=false;
        UDPRequestProvider[] req = new UDPRequestProvider[1];
        int counter = 0;
        ArrayList<String> locList = new ArrayList<>();
        locList.add("CON");
        locList.add("MCG");
        locList.add("MON");
        for (String loc : locList) {
            if (loc== this.location && !isReturned) {
        		isReturned=true;        			
            	if(borrowMap.containsKey(userID))
        		{
            		List<String> keyData=borrowMap.get(userID);
        			if(keyData.contains(itemID) && keyData.size()>1)
        			{
        				borrowMap.get(userID).remove(itemID);
        			}
        			else
        			{
        		borrowMap.remove(userID);
        			}
        		Item originalData=dataMap.get(itemID);
        		originalData.setInStock(++originalData.InStock);
        		result="Item returned successfully";
        		
        		}
        		else
        		{
        			result="No such item borrowed.Please try again";
        		}
            	logManager.logger.info(result);
            }
            else
            {  
            if(!isReturned)
    		{
            	try {

            		if(loc.equals(itemID.substring(0, 3)))
            		{
            			isReturned=true;
            			String data="returnItem:"+userID+":"+itemID;
            			logManager.logger.info("Request redirecting through UDP to: "+ loc);
                	req[counter] = new UDPRequestProvider(Server.serverData.get(loc),data);
                    req[counter].start(); 
                    counter++;
            		}

                } catch (IOException e) {
                	
                    logManager.logger.log(Level.SEVERE, e.getMessage());
                    logManager.logger.info("Failed");
                }
            }
    		}
        }
            if(counter>0)
            {
            for (UDPRequestProvider request : req) {
                try {
                    request.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                result=request.getReturnValue();
            }
            }
        logManager.logger.info("Server Response:"+ result);
        logManager.logger.info("Request Successfully Completed");

        assignWaitlistingMembers(itemID);
		return result;
	}
	
	private void assignWaitlistingMembers(String itemID) throws RemoteException
	{
		logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:Assign Waitlisting members; REQUEST PARAMETERS: itemID:"+itemID);
        UDPRequestProvider[] req = new UDPRequestProvider[1];
        String result="";
        int counter=0;
	      ArrayList<String> locList = new ArrayList<>();
	        locList.add("CON");
	        locList.add("MCG");
	        locList.add("MON");
	        
	        for (String loc : locList) {
			if(loc==this.location)
        	{
			Queue<String> waitingListUsers= waitingListMap.get(itemID);
			if(waitingListUsers!=null)
			{
				String userId=waitingListUsers.poll();
				if(userId!=null)
				{
				Item existingItem=dataMap.get(itemID);
				int existingItemCount=existingItem.getInStock();
				System.out.println("count:"+existingItemCount);
				if(existingItemCount>0)
				{
					existingItem.setInStock(--existingItemCount);
				List<String> existingItemList=borrowMap.get(userId);
				if(existingItemList==null)
				{
					List<String> itemList=new ArrayList<>();
					itemList.add(itemID);
					existingItemList=itemList;
				}
				else
				{
				existingItemList.add(itemID);
				}
				borrowMap.put(userId, existingItemList);
				result=userId+ " borrowed book "+itemID+" successfully";
			logManager.logger.info("Request Successfully Completed");
				}
			}
			}
		}
		else
		{
			 if(loc.equals(itemID.substring(0, 3)))
			 {
			 try {
				 Queue<String> waitingItemsList= Server.serverData.get(loc).waitingListMap.get(itemID);
				 if(waitingItemsList!=null)
				 {
             	String data="assignWaitListItem:"+itemID;
             	logManager.logger.info("Request redirecting through UDP to: "+ loc);
             	req[counter] = new UDPRequestProvider(Server.serverData.get(loc),data);
                req[counter].start();
                counter++;
				 }

             } catch (IOException e) {
                 logManager.logger.severe(e.getMessage());
             }
         }
		 }
	        }
	     	if(counter>0)
        	{
	        for (UDPRequestProvider request : req) {
                try {
                    request.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                result=request.getReturnValue();

            }
        	}
            System.out.println(result);
	}
	
	public String borrowItem(String userID, String itemID,boolean isWaitlisted) throws RemoteException {
		String result="";
        UDPRequestProvider[] req = new UDPRequestProvider[1];
		logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:borrow Item; REQUEST PARAMETERS: userId:"+userID+",itemID:"+itemID);
        int counter = 0;
        ArrayList<String> locList = new ArrayList<>();
        locList.add("CON");
        locList.add("MCG");
        locList.add("MON");
		if(!isWaitlisted)
		{
		boolean isBorrowed=false;
        for (String loc : locList) {
        	if(loc==this.location)
        	{
		if(userID.substring(0, 3).equals(itemID.substring(0, 3)))
		{
			Item existingItem=dataMap.get(itemID);
			if(existingItem!=null)
			{
			int existingItemCount=existingItem.getInStock();

			if(existingItemCount>0)
			{
				isBorrowed=true;
				existingItem.InStock=--existingItemCount;
				List<String> existingItemList=borrowMap.get(userID);
				if(existingItemList==null)
				{
					List<String> itemList=new ArrayList<>();
					itemList.add(itemID);
					existingItemList=itemList;
				}
				else
				{
				existingItemList.add(itemID);
				}
				borrowMap.put(userID, existingItemList);
				result=userID + " borrowed "+ itemID+ " successfully";
			}
			else
			{
				result="Failed";
			}}
			else
			{
				result="No Item exists with item Id: "+itemID;
			}
			
		}
        	}
		else
		{
			 if(!isBorrowed)
			 {
					if(loc.equals(itemID.substring(0, 3)))
            		{
			 try {

							isBorrowed=true;
             	String data="borrowItem:"+userID+":"+itemID;
             	logManager.logger.info("Request redirecting through UDP to: "+ loc);
             	req[counter] = new UDPRequestProvider(Server.serverData.get(loc),data);
				 }

              catch (IOException e) {
                 logManager.logger.log(Level.SEVERE, e.getMessage());
             }
             req[counter].start();
             counter++;
         }
			 }
		}
		}
        	if(counter>0)
        	{
        	   for (UDPRequestProvider request : req) {
                   try {
                       request.join();
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
                  result=request.getReturnValue();//request.getReturnValue();
               }
        	}
        	logManager.logger.info("Request Successfully Completed");
		}
		else
		{
		result=AddToWaitingList(userID,itemID);
		}
		return result;
	}

	public String AddToWaitingList(String userID,String itemID)
	{
		logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:borrow Item; REQUEST PARAMETERS: userId:"+userID+",itemID:"+itemID);
		String result="";
        UDPRequestProvider[] req = new UDPRequestProvider[1];
        int counter=0;
		  ArrayList<String> locList = new ArrayList<>();
	        locList.add("CON");
	        locList.add("MCG");
	        locList.add("MON");
		
		for (String loc : locList) {
        	if(loc==this.location)
        	{
		if(userID.substring(0, 3).equals(itemID.substring(0, 3)))
		{
			Queue<String> waitingListQueue=waitingListMap.get(itemID);
		if(waitingListQueue==null)
		{
			Queue<String> itemQueue=new LinkedList<>();
			itemQueue.add(userID);
			waitingListQueue=itemQueue;
		}
		else
		{
		waitingListQueue.add(userID);
		}	
		waitingListMap.put(itemID, waitingListQueue);
		result="user "+userID+ " is added to the waiting list";
		}
    		}
        	else
        	{
				if(loc.equals(itemID.substring(0, 3)))
        		{
        		 try {
        				logManager.logger.info("Request redirecting through UDP to: "+ loc);
          	String data="addToWaitingList:"+userID+":"+itemID;
          	
          	req[counter] = new UDPRequestProvider(Server.serverData.get(loc),data);
				 }

           catch (IOException e) {
              logManager.logger.log(Level.SEVERE, e.getMessage());
          }
          req[counter].start();
          counter++;
        	}
            }
		 }
		if(counter>0)
    	{
    	   for (UDPRequestProvider request : req) {
               try {
                   request.join();
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
              result=request.getReturnValue();//request.getReturnValue();
           }
    	}
    	logManager.logger.info("Request Successfully Completed");
		return result;
	}

}

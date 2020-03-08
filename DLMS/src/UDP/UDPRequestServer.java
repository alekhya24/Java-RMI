package UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import Model.Item;
import Server.ServerImpl;
import Util.Servers;
public class UDPRequestServer extends Thread {
	DatagramSocket serverSocket;
	Servers location;
	private DatagramPacket receivePacket;
	private ServerImpl server;


	public UDPRequestServer(DatagramPacket pkt, ServerImpl serverImp) {
		receivePacket = pkt;
		server = serverImp;
		try {
			serverSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		byte[] responseData;
		try {	
			String inputPkt = new String(receivePacket.getData()).trim();
			System.out.println("UDP Request:"+inputPkt);
			if (inputPkt.split(":")[0].equals("findItem")) {
				responseData = getItemCount(inputPkt).getBytes();
				serverSocket.send(new DatagramPacket(responseData, responseData.length, receivePacket.getAddress(),
						receivePacket.getPort()));
			}
			else if(inputPkt.split(":")[0].equals("returnItem"))
			{
				responseData=returnItem(inputPkt).getBytes();
				serverSocket.send(new DatagramPacket(responseData, responseData.length, receivePacket.getAddress(),
						receivePacket.getPort()));
			}
			else if(inputPkt.split(":")[0].equals("borrowItem"))
			{
				responseData=borrowItem(inputPkt).getBytes();
				serverSocket.send(new DatagramPacket(responseData, responseData.length, receivePacket.getAddress(),
						receivePacket.getPort()));
			}
			else if(inputPkt.split(":")[0].equals("assignWaitListItem"))
			{
				responseData=assignWaitListItem(inputPkt).getBytes();
				serverSocket.send(new DatagramPacket(responseData, responseData.length, receivePacket.getAddress(),
						receivePacket.getPort()));
			}
			else if(inputPkt.split(":")[0].equals("addToWaitingList"))
			{
				responseData=addToWaitingList(inputPkt).getBytes();
				serverSocket.send(new DatagramPacket(responseData, responseData.length, receivePacket.getAddress(),
						receivePacket.getPort()));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private String addToWaitingList(String input)
	{
		String userId=input.split(":")[1];
		String itemId=input.split(":")[2];
		server.logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:borrow Item; REQUEST PARAMETERS: userId:"+userId+",itemID:"+itemId);
		String result="";
		Queue<String> waitingListQueue=server.waitingListMap.get(itemId);
		if(waitingListQueue==null)
		{
			Queue<String> itemQueue=new LinkedList<>();
			itemQueue.add(userId);
			waitingListQueue=itemQueue;
		}
		else
		{
		waitingListQueue.add(userId);
		}	
		server.waitingListMap.put(itemId, waitingListQueue);
		result="user "+userId+ " is added to the waiting list for "+ itemId;
		server.logManager.logger.info("Server Response: "+ result);
			return result;
	}

	private String assignWaitListItem(String input)
	{	
		String output="";
		String itemId=input.split(":")[1];
		server.logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:Assign Waitlisting members; REQUEST PARAMETERS: itemID:"+itemId);
		Queue<String> waitingListUsers= server.waitingListMap.get(itemId);
		if(waitingListUsers!=null)
		{
		String userId=waitingListUsers.poll();
		if(userId!=null)
		{
		Item existingItem=server.dataMap.get(itemId);
		int existingItemCount=existingItem.getInStock();
		if(existingItemCount>0)
		{
			existingItemCount=existingItemCount-1;
			existingItem.setInStock(existingItemCount);
		List<String> existingItemList=server.borrowMap.get(userId);
		if(existingItemList==null)
		{
			List<String> itemList=new ArrayList();
			itemList.add(itemId);
			existingItemList=itemList;
		}
		else
		{
		existingItemList.add(itemId);
		}
		server.borrowMap.put(userId, existingItemList);
		output=userId+ " borrowed book "+itemId+" successfully";
		}
		}
		}
		server.logManager.logger.info("Server Response: "+ output);
		return output;
	}
	
	private String getItemCount(String input){
		String output = "";
		String itemName=input.split(":")[2];
		server.logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:GetItemCount; REQUEST PARAMETERS: itemName:"+itemName);
		for (Map.Entry<String, Item> entry : server.dataMap.entrySet()) {
			if(entry.getValue().ItemName.equals(itemName))
			{
			output=entry.getKey()+" " +entry.getValue().InStock;
			}
		}
		server.logManager.logger.info("Server Response: "+ output);
		return output;
	}
	
	private String borrowItem(String input)
	{
		String result="";
		String userID=input.split(":")[1];
		String itemID=input.split(":")[2];
		server.logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:BorrowItem; REQUEST PARAMETERS: itemID: "+itemID+ ",userID: "+userID);
		if(!checkIfUserAlreadyBorrowed(userID,itemID))
		{
			Item existingItem=server.dataMap.get(itemID);
			if(existingItem!=null)
			{
			int existingItemCount=existingItem.getInStock();
			System.out.println("count:"+existingItemCount);
			if(existingItemCount>0)
			{
				existingItemCount=existingItemCount-1;
				existingItem.setInStock(existingItemCount);
				List<String> existingItemList=server.borrowMap.get(userID);
				if(existingItemList==null)
				{
					List<String> itemList=new ArrayList();
					itemList.add(itemID);
					existingItemList=itemList;
				}
				else
				{
				existingItemList.add(itemID);
				}
				server.borrowMap.put(userID, existingItemList);
				result=userID+ "borrowed book "+itemID+" successfully";
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
		else
		{
			result="User can borrow only once from other libraries";
			
		}
		server.logManager.logger.info("Server Response: "+ result);
		return result;
	}
	
	private boolean checkIfUserAlreadyBorrowed(String userId,String itemId)
	{
		server.logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:CheckIfUserAlreadyBorrowed; REQUEST PARAMETERS: itemID:"+itemId+", userId: "+userId);
		boolean isBorrowed=false;
		List<String> existingItemList=server.borrowMap.get(userId);
		if(existingItemList!=null)
		{
			existingItemList.stream().map(s->s.substring(0,3)).forEach(System.out::println);
		List<String> shortenedExistingItemList=existingItemList.stream().map(s->s.substring(0,3)).collect(Collectors.toList());
		if(shortenedExistingItemList.contains(server.location.toString()))
		{
			isBorrowed=true;
		}
		}
		server.logManager.logger.info("Server Response: "+ isBorrowed);
		return isBorrowed;
	}
	private String returnItem(String input)
	{
		String result="";
		String itemId=input.split(":")[2];
		String userId=input.split(":")[1];
		server.logManager.logger.info("Date & Time: " + LocalDateTime.now() +"REQUEST TYPE:ReturnItem(); REQUEST PARAMETERS: itemID:"+itemId+", userId:"+userId);
		HashMap<String, List<String>> data=server.borrowMap;
		if(data.containsKey(userId))
		{
			List<String> keyData=server.borrowMap.get(userId);
			if(keyData.contains(itemId) && keyData.size()>1)
			{
				server.borrowMap.get(userId).remove(itemId);
			}
			else
			{
		server.borrowMap.remove(userId);
			}
		Item originalData=server.dataMap.get(itemId);
		originalData.setInStock(++originalData.InStock);
		result="Item returned successfully";
		}
		else
		{
			result="No such item borrowed.Please try again";
		}
		server.logManager.logger.info("Server Response: "+ result);
		return result;
	}
}

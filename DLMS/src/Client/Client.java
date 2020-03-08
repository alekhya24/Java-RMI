package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Util.LogManager;
import Util.Servers;

public class Client {
static LogManager logManager;
	
	public static void main(String[] args) throws IOException, NotBoundException {
		while (true) {
			ManagerClientImpl mngrClient=null;
			UserClientImpl usrClient = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter the ID:");
			String id = br.readLine();			
			
			if(id.length()!=8)
			{				
				System.out.println("Too many/less characters in the ID. Please enter in (LIBRXXXX) format, where LIB={CON,MCG,MON} and R={M,U}");
				continue;	
			}
			else
			{
				String entered_id=id.substring(4, 7);
				Pattern numberPattern=Pattern.compile("([0-9]*)");
				Matcher matchID=numberPattern.matcher(entered_id);

				if(!matchID.matches())
				{
					System.out.println("Invalid character in ID.please enter in (LIBRXXXX) format,where XXXX can only be numbers");
					continue;
				}
			}
			if (id.contains("CON") && (id.substring(3,4).equals("M")||id.substring(3, 4).equals("U"))) {
				if(id.contains("CONM"))
				{
				mngrClient = new ManagerClientImpl(Servers.CON, id);
				logManager = new LogManager(Servers.CON.getserverName());
				}
				else
				{
					usrClient = new UserClientImpl(Servers.CON, id);
					logManager = new LogManager(Servers.CON.getserverName());
				}
			} else if (id.contains("MCG") && (id.substring(3,4).equals("M")||id.substring(3, 4).equals("U"))) {
				if(id.contains("MCGM"))
				{
				mngrClient = new ManagerClientImpl(Servers.MCG, id);
				logManager = new LogManager(Servers.MCG.getserverName());
				}
				else
				{
					usrClient = new UserClientImpl(Servers.MCG, id);
					logManager = new LogManager(Servers.MCG.getserverName());
				}
			} else if (id.contains("MON") && (id.substring(3,4).equals("M")||id.substring(3, 4).equals("U"))) {
				if(id.contains("MONM"))
				{
				mngrClient =new ManagerClientImpl(Servers.MON, id);
				logManager = new LogManager(Servers.MON.getserverName());
				}
				else
				{
					usrClient = new UserClientImpl(Servers.MON, id);
					logManager = new LogManager(Servers.MON.getserverName());	
				}
			} else {
				System.out.println("Wrong ID.Please enter again");
				continue;
			}
			int i = 1;
			while (i != 0) {
				if(id.contains("CONM") || id.contains("MCGM")|| id.contains("MONM"))
				{
				System.out.println("Please select");
				System.out.println("1) Add Item");
				System.out.println("2) Remove Item");
				System.out.println("3) List Item Availability");
				String choiceString=br.readLine();
				Pattern choicePattern=Pattern.compile("([0-9]*)");
				Matcher givenChoice=choicePattern.matcher(choiceString);
				Integer choice = givenChoice.matches()? Integer.parseInt(choiceString):4;
				switch (choice) {
				case 1:
					System.out.println("Enter Item Name");
					String itemName = br.readLine();
					System.out.println("Enter the quantity");
					int itemQty = Integer.parseInt(br.readLine());

					System.out.println(
							mngrClient.addItem(id, itemName, itemQty));
					break;
				case 2:
					System.out.println("Enter ItemId");
					String rItemId = br.readLine();
					if(rItemId.contains("CON")||rItemId.contains("MON")||rItemId.contains("MCG"))
					{
					System.out.println("Enter the quantity");
					String enteredValue= br.readLine();
					int rItemQty = Integer.parseInt(enteredValue);
					System.out.println(
							mngrClient.removeItem(id, rItemId,rItemQty));
					}
					else
					{
						System.out.println("Invalid ItemId.Please try again");
						continue;
					}
					break;
				case 3:
					String op=mngrClient.listItemAvailability(id);

						System.out.println(op);
						break;
				default:
					System.out.println("Invalid choice! Please try again");
					break;
				}
				}
				else if(id.contains("CONU") || id.contains("MCGU")|| id.contains("MONU"))
				{
				System.out.println("Please select");
				System.out.println("1) Borrow Item");
				System.out.println("2) Find Item");
				System.out.println("3) Return Item ");
				String choiceString=br.readLine();
				Pattern choicePattern=Pattern.compile("([0-9]*)");
				Matcher givenChoice=choicePattern.matcher(choiceString);
				Integer choice = givenChoice.matches()? Integer.parseInt(choiceString):4;
				switch (choice) {
				case 1:
					System.out.println("Enter Item Id");
					String itemId = br.readLine();
					if(itemId.contains("CON")||itemId.contains("MON")||itemId.contains("MCG"))
					{
							String op=usrClient.borrowItem(id, itemId,false);
							if(op.contains("Fail"))
							{
								System.out.println("Currently the book is not available.Would you like to add yourself to waiting list?");
								System.out.println("1) Yes");
								System.out.println("2) No");
								Integer selection=Integer.parseInt(br.readLine());
								switch(selection) {
								case 1:
									op=usrClient.borrowItem(id, itemId,true);
									System.out.println(op);
									break;
								case 2:
									break;
								}
							}
							else
							{
								System.out.println(op);
							}
					}
					else
					{
						System.out.println("Invalid ItemId.Please try again");
						continue;
					}
					break;
				case 2:
					System.out.println("Enter Item Name");
					String fItemName = br.readLine();
					
					System.out.println(
							usrClient.findItem(id, fItemName));
					break;
				case 3:
					System.out.println("Enter Item Id");
					String ritemId = br.readLine();
					if(ritemId.contains("CON")||ritemId.contains("MON")||ritemId.contains("MCG"))
					{
					System.out.println(usrClient.returnItem(id, ritemId));
					}
					else
					{
						System.out.println("Invalid ItemId.Please try again");
						continue;
					}
					break;
				default:
					System.out.println("Invalid choice! Please try again");
					break;
				}
			}
		}
	}
	}
}

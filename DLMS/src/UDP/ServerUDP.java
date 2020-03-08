package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import Server.ServerImpl;
import Util.Servers;;


public class ServerUDP extends Thread {
	DatagramSocket serverSocket;
	DatagramPacket receivePacket;
	DatagramPacket sendPacket;
	int udpPortNum;
	Servers location;
	Logger loggerInstance;
	ServerImpl server;
	
	public ServerUDP(Servers loc, Logger logger, ServerImpl serverImp) {
		location = loc;
		loggerInstance = logger;
		this.server = serverImp;
		try {
			switch (loc) {
			case CON:
				serverSocket = new DatagramSocket(Util.Constants.UDP_PORT_NUM_CON);
				udpPortNum = Util.Constants.UDP_PORT_NUM_CON;
				logger.info("CON UDP Server Started");
				break;
			case MCG:
				serverSocket = new DatagramSocket(Util.Constants.UDP_PORT_NUM_MCG);
				udpPortNum = Util.Constants.UDP_PORT_NUM_MCG;
				logger.info("MCG UDP Server Started");
				break;
			case MON:
				serverSocket = new DatagramSocket(Util.Constants.UDP_PORT_NUM_MON);
				udpPortNum = Util.Constants.UDP_PORT_NUM_MON;
				logger.info("MON UDP Server Started");
				break;
			}

		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	@Override
	public void run() {
		byte[] receiveData;
		//System.out.println(c+" "+location);
		while(true) {
			try {
				receiveData = new byte[1024];
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				System.out.println("Received pkt :: "+new String(receivePacket.getData()));
				String inputPkt = new String(receivePacket.getData()).trim();	
				new UDPRequestServer(receivePacket, server).start();		
				loggerInstance.log(Level.INFO, "Received " + inputPkt + " from " + location);
			} catch (Exception e) {
			}
		}
	}
}

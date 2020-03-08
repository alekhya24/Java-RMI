package UDP;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import Server.ServerImpl;


public class UDPRequestProvider extends Thread {
    private Logger logger;
    private ServerImpl server;
    private String reqData;
    private String returnValue;

    
    public UDPRequestProvider(ServerImpl server,String requiredData) throws IOException {
        this.server = server;
        this.reqData=requiredData;
    }

   
    public String getReturnValue() {
        return returnValue;
    }
   
    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] data = reqData.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(server.IPaddress), server.serverUDP.udpPortNum);
            socket.send(packet);
            data = new byte[100];
            socket.receive(new DatagramPacket(data, data.length));
            returnValue = new String(data);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
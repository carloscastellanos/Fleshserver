/**
 * 
 */
package cc.fleshserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacket;
import de.sciss.net.OSCReceiver;
import de.sciss.net.OSCTransmitter;

/**
 * @author carlos
 *
 */
public class OSCHandler extends Thread
{
	
	//   ---------------- OSC/TEMP stuff -----------------   //
    // Note: we need to have two OSC ports since TEMP may not like
    // sending and receiving on the same port (beacuse multiple clients
    // will be doing it and maybe it doesn't know who to issue the
    // /pingreturn too?)
    // plus with OSC, it's standard procedure to send on a different port
    // than you receive on
    OSCTransmitter oscTransmitter = null; // for sending messages to TEMP server (on port 12000)
    OSCReceiver oscReceiver = null; // for receiving messages from TEMP
    InetSocketAddress sendAddr;
    InetSocketAddress rcvAddr;
    DatagramChannel dchSend = null;
    DatagramChannel dchRcv = null;
    static final int sendPort = 44000;
    static final int receivePort = 44001;
    //static final String oscAddress = "/fleshserver";
    //static final String tempConnect = "/temp/connect/" + Integer.toString(receivePort) + oscAddress;
    static final String respond = "/sound"; // responding only to address space "/sound"
    //static final String tempDisconnect = "/temp/disconnect/" + Integer.toString(receivePort);
    //static final String ping = "/temp/ping";
    //static final String pingReturn = "/temp/pingreturn/" + Integer.toString(receivePort);
    boolean isConnected = false;
    String host;
    
	public OSCHandler(String host) {
		this.host = host;
	}
    
	public void run()
	{
		System.out.println("*** OSCHandler started... ***");
		// Get socket to TEMP server via OSC/UDP
		while(!Thread.interrupted()) {
			if(!isConnected) {
				if(oscConnect() == false) {
					isConnected = false;
					System.out.println("Error making an OSC/UDP socket to " + host + ":" + sendPort);
					System.out.println("Will try again in 10 seconds");
					try {
						Thread.sleep(10000);  // retry in 10 secs
					} catch (InterruptedException iex) {
						System.out.println("Thread " + this + " was interrupted: " + iex);
					}
				} else {
					isConnected = true;
				}
			}
		} // end while
		if(isConnected)
			oscDisconnect();
		
		System.out.println("*** OSCHandler stopped. ***");
	}
	
	void sendOSC(OSCPacket oscPacket)
	{
		if(oscTransmitter != null && sendAddr != null) {
			try {
				oscTransmitter.send(oscPacket, sendAddr);
				//System.out.println("*** OSC Message sent... ***");
			} catch(IOException ioe) {
				System.out.println("*** Error sending OSC/UDP message! *** " + ioe);
			}
		}
	}
	
	boolean oscConnect()
	{
		if(isConnected)
			return true;
		
		boolean success;
		try {
			//InetAddress localhost = InetAddress.getLocalHost();
			InetAddress addr = InetAddress.getByName(host);
			dchRcv = DatagramChannel.open();
			dchSend = DatagramChannel.open();
			// assign an automatic local socket address
			rcvAddr = new InetSocketAddress(addr, receivePort);
			sendAddr = new InetSocketAddress(addr, sendPort);
			dchRcv.socket().bind(rcvAddr);
			oscReceiver = new OSCReceiver(dchRcv);
			oscReceiver.addOSCListener(new OSCListener() {
				// listen for and accept incoming OSC messages
				public void messageReceived(OSCMessage msg, SocketAddress sender, long time)
				{
					// get the address pattern of the msg
					String oscMsg = msg.getName();
					InetSocketAddress saddr = (InetSocketAddress) sender;
					System.out.println("=== OSC message received - " + oscMsg + 
							" received from: " + saddr.getAddress() + ":" + saddr.getPort() + " ===");
					if(oscMsg.indexOf(respond) != -1) {
						String arg = (String)msg.getArg(0);
						if(arg != null && arg.length() > 1)
							System.out.println(arg);
					}

				}
			});
			oscReceiver.startListening();
			oscTransmitter = new OSCTransmitter(dchSend);
			//OSCMessage connect = new OSCMessage(tempConnect, new Object[] { respond });
			//sendOSC(connect);

			System.out.println("*** OSC connection successful ***");
			success = true;
		} catch(IOException ioe) {
			System.out.println("*** OSC connection error! ***");
			System.out.println(ioe);
			success = false;
		}
		return success;
	}
	
	void oscDisconnect()
	{
		// stop/close the OSC
		//OSCMessage disconnect = new OSCMessage(tempDisconnect, OSCMessage.NO_ARGS);
		//sendOSC(disconnect);
		
		if(oscReceiver != null) {
			try {
				oscReceiver.stopListening();
            } catch(IOException e0) {
            }
        }
        if(dchRcv != null) {
        		try {
        			dchRcv.close();
        		} catch(IOException e1) {
        		}
        }
        if(dchSend != null) {
    		try {
    			dchSend.close();
    		} catch(IOException e2) {
    		}
    }
        System.out.println("*** OSC disconnection successful ***");
	}

}

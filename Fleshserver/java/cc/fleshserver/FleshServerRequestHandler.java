/**
 * 
 */
package cc.fleshserver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;
import cc.infoart.comm.rxtx.BufferedSerialHandler;
import cc.infoart.comm.rxtx.SerialDataEventListener;
import de.sciss.net.OSCMessage;

/**
 * @author carlos
 *
 */
public class FleshServerRequestHandler implements SerialDataEventListener
{
    private static BufferedSerialHandler serial = new BufferedSerialHandler();
    private boolean serialOpen;
    // store a list of all the current BodyD objects/client sockets
    private static ArrayList<FleshServerEventListener> listeners = new ArrayList<FleshServerEventListener>();
    private StringBuffer sb = null;
    private  SimpleDateFormat logTime;
    private static OSCHandler oscHandler; // OSCHandler thread
    private static final String osc = "/fleshdata";
    
    // constructor
    public FleshServerRequestHandler(String host)
    {
    		logTime =  new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss Z");
    		// start the OSCHandler thread
    		oscHandler = new OSCHandler(host);
    		oscHandler.setDaemon(true);
    		oscHandler.start();
    }
    
    public void serialDataEvent(int[] data) {
    		//System.out.println("serial data received...");
    		byte[] dataBytes = new byte[data.length];
    		for(int i=0; i<data.length; i++)
    			dataBytes[i] = (byte)data[i];
    		String str = new String(dataBytes);
    		if(sb == null)
    			sb = new StringBuffer();
    		sb.append(str);
    		// beginning of message
    		if(sb.indexOf("<message>") != -1 || sb.indexOf("<MESSAGE>") != -1) {
    			if(sb.indexOf("</message>") != -1 || sb.indexOf("</MESSAGE>") != -1) {
    				String temp = sb.toString();
    				String[] result = temp.split("\\|");
    				String status = result[1];
    				String ip = result[2];
    				String msg = ip + " - - [" + logTime.format(new Date()) + "] status=" + status + " HTTP/1.0 200";
    				// this will trigger the callback
    				if(!listeners.isEmpty())
    					notifySerialListeners(msg);
    				System.out.println(msg);
    				// OSC
    				if(oscHandler.isConnected) {
    					// create the OSCMesage
    					Object[] vals = new Object[] {msg};
    					OSCMessage oscMsg = new OSCMessage(osc, vals);
    					// send it!
    					oscHandler.sendOSC(oscMsg);
    				}
    				sb = null;
    			}
    		}
    }
    
    /**
     * @return a list of the available serial ports
     */
    public ArrayList getSerialPorts()
    {
        return serial.getPorts();
    }
    
    public boolean openSerial(String sPort, int baudRate)
    {
        if(!serialOpen) {
            try {
                // open the serial port
                serialOpen = serial.openStream(sPort, baudRate, this);
            } catch(Exception ex) {
            }
        }
        return serialOpen;
    }
    
    public boolean closeSerial()
    {
        try {
            if(serial.closeStream()) {
                serialOpen = false;
            }
        } catch (Exception ex) {
            System.out.println("Error while trying to close serial port!");
            System.out.println(ex);
        }
        return !serialOpen;
    }
    
    public boolean isSerialOpen()
    {
        return serial.isPortOpen();
    }
    
    public boolean close()
    {
        if(closeSerial()) {
            return true;
        } else {
            return false;
        }
    }
    
    public synchronized void addSerialOutputListener(FleshServerEventListener sel) {
        if(sel != null && listeners.indexOf(sel) == -1) {
            listeners.add(sel);
            System.out.println("[+] " + sel + " # of listeners=" + listeners.size());
        }
    }

    public synchronized void removeSerialOutputListener(FleshServerEventListener sel) {
        if(listeners.contains(sel)) {
            listeners.remove(listeners.indexOf(sel));
            System.out.println("[-] " + sel + " # of listeners=" + listeners.size());
        }
    }

    // let everyone know message was received
    private synchronized void notifySerialListeners(String msg) {
        if(listeners == null) {
            //System.out.println("CarnivoreListener: no listeners");
            return;
        } else {
            ListIterator<FleshServerEventListener> iter = listeners.listIterator();
            //System.out.println("Notifying all " + listeners.size() + " listeners...\n");
            while(iter.hasNext()) {
            		iter.next().messageReceived(msg);
            	}
        }
    	} 
}

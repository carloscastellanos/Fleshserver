/**
 * 
 */
package cc.fleshserver;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author carlos
 *
 */
public class FleshServerApp extends Window
{
	private FleshServerDisplay flesh;
	private Frame parentFrame;
	
	public FleshServerApp(Frame f) {
		super(f);
		parentFrame = f;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("\nFleshserver 0.1a\n");
		
		if (args.length < 1)
			throw new IllegalArgumentException("Syntax: FleshServer <osc host>");
		 
		String host = args[0];
		
		// Setup custom event trapping (to trap escape key globally)
		// NOTE: this won't work with MS Jview (comment out if running in that JRE)
		// NOTE: this interferes with Alt-F4 key (OS key to close window)
		EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
		eq.push(new CustomEventQueue());

		// Make the full screen frame
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		Frame frame = new Frame();
		frame.setBounds(0,0,(int)screenDim.width,(int)screenDim.height);
		frame.setVisible(true);

		// Make the Fleshserver Window from frame
		FleshServerApp fleshApp = new FleshServerApp(frame);
		fleshApp.setVisible(true);
		fleshApp.init();
		fleshApp.start();
		fleshApp.serialSetup(host);
	}
	
    public void init()
    {
      // MS JView requires .x .y .width .height (doesn't know getWidth() etc) (thanks to Mark Napier)
      setBounds(parentFrame.getBounds().x, parentFrame.getBounds().y, parentFrame.getBounds().width, parentFrame.getBounds().height);
      setVisible(true);
      setBackground(Color.black);
      setLayout(null);

      flesh = new FleshServerDisplay();
      flesh.setLocation(0,0);
      add(flesh);
    }


    public void start()
    {
      if (flesh != null) {
  		flesh.start();
      }
    }

    public void stop()
    {
      if (flesh != null) {
        flesh.stop();
      }
    }

	private void serialSetup(String oscHost) {
	    int baud = 9600;			// our baud rate
	    int serialPortNum = 0;	// the serial port we'll open
	    ArrayList portList;		// the list of available serial ports;
	    BufferedReader inStream;
	    
		FleshServerRequestHandler serialHandler = new FleshServerRequestHandler(oscHost);
		serialHandler.addSerialOutputListener((FleshServerEventListener)flesh);
		portList = serialHandler.getSerialPorts();
		
		System.out.println("\nPick the number of a serial port to open.");
		
		try {
            // open input from keyboard
            inStream = new BufferedReader(new InputStreamReader(System.in));
            String inputText;
            
            // read as long as we have data at stdin
            while((inputText = inStream.readLine()) != null) {
                // if port is not open, assume user is typing a number
                // and open the corresponding port:
                if (!serialHandler.isSerialOpen()) {
                    serialPortNum = getNumber(inputText);
                    // if serialPortNum is in the right range, open it:
                    if (serialPortNum >= 0) {
                        if (serialPortNum < portList.size()) {
                            String whichPort = (String)portList.get(serialPortNum);
                            if (serialHandler.openSerial(whichPort, baud)) {
                                break;
                            }
                        } else {
                            // You didn't ge a valid port:
                            System.out.println(serialPortNum + " is not a valid serial port number. Please choose again");
                        }
                    }
                } /*else {
                    // port is open:
                    // if user types +++, close the port and repeat the port selection dialog:
                    if (inputText.equals("+++")) {
                        bodyDSerial.closeSerial();
                        System.out.println("Serial port closed.");
                        portList = bodyDSerial.getSerialPorts();                  
                        System.out.println("\nPick the number of a serial port to open.");
                    }            
                }*/
            }
            // if stdin closes, close port and quit:
            //inStream.close();
            //bodyDSerial.closeSerial();
            //System.out.println("Serial port closed; thank you, have a nice day.");
            System.out.println(" ");
        } catch(IOException e) {
            System.out.println(e);
        }
	}
	
    /**
     * @return an int from a string that's a valid number.
     **/   
    private int getNumber(String inString) {
        int value = -1;
        try {
            value = Integer.parseInt(inString);
        } catch (NumberFormatException ne) {
            System.out.println("not a valid number");
        }
        return value;
    }
}


////////////////////////////////////////////////////////////
//class CustomEventQueue
//
//Custom event queue traps Escape key at highest level
//no matter what object has focus.  Will exit from app
//when Escape is hit.

class CustomEventQueue extends EventQueue
{
	public CustomEventQueue() {
	}

	// Override this function to change behavior
	public void handleKey(String keydesc) {
		System.out.println("System Event: got key " + keydesc);
		if (keydesc.equals("esc")){
			System.exit(0);
		}
	}

	// Override this function to change behavior
	public void handleMouseMove(int x, int y) {
	}

	public void dispatchEvent(AWTEvent event)
	{
		int id = event.getID();
		// Trap specified keys
		if (id == KeyEvent.KEY_PRESSED) {
			KeyEvent ke = (KeyEvent)event;
			if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
				handleKey("esc");
			}
			else if (ke.getKeyCode() == KeyEvent.VK_F1 && ke.isControlDown()) {
				handleKey("ctrlF1");
			}
			else if (ke.getKeyCode() == KeyEvent.VK_F1) {
				handleKey("F1");
			}
			return;
		}
		else if (id == MouseEvent.MOUSE_MOVED) {
			MouseEvent me = (MouseEvent)event;
			if (me.getX() < 5 && me.getY() < 5) {
				handleMouseMove(me.getX(),me.getY());
			}
		}
		super.dispatchEvent(event);
	}
}

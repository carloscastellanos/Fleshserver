/**
 * 
 */
package cc.fleshserver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Toolkit;


/**
 * @author carlos
 *
 */

public class FleshServerDisplay extends Panel implements FleshServerEventListener
{
	private String currentLine = "";
    private int currLinePos = 0;
    private int panelWidth, panelHeight;

    public FleshServerDisplay() {
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		panelWidth = (int)screenDim.width;
		panelHeight = (int)screenDim.height;
    }
  
    public FleshServerDisplay(int width, int height) {
    		panelWidth = width;
    		panelHeight = height;
    }
    
    public void start() {
    		setBackground(Color.white);
    		setFont(new Font("Monaco", Font.PLAIN, 12));
    }
    
	public void messageReceived(String line) {
		currentLine = line;
		repaint();
	}
	
	// draw to screen
    public void paint(Graphics g) {
        g.drawString(currentLine,20,currLinePos*20);
        currLinePos++;
        if (currLinePos*20 > panelHeight) {
            g.clearRect(0,0,panelWidth,panelHeight);
            currLinePos = 0;
        }
    }

    public void update(Graphics g) {
        paint(g);
    }

	public void stop() {
		currentLine = "";
		currLinePos = 0;
	}

/*	public static void main(String args[]) {
		PApplet.main(new String[] { "--display=1", "--present", "FleshServerDisplay" });
	}*/
	
}

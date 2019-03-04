/**
 * 
 */
package cc.fleshserver;

import java.util.EventListener;

/**
 * @author carlos
 *
 */
public interface FleshServerEventListener extends EventListener
{
	public abstract void messageReceived(String msg);
}

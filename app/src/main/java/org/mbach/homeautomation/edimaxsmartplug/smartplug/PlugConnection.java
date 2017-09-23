package org.mbach.homeautomation.edimaxsmartplug.smartplug;

public interface PlugConnection {
	
	public void connect();
	
	public boolean isConnected();
	
	public String sendCommand(String xmlCommand) throws Exception;
	
	public void disconnect();
}
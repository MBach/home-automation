package org.mbach.homeautomation.edimaxsmartplug.smartplug;

public interface PlugConnection {
	
	void connect();
	
	boolean isConnected();
	
	String sendCommand(String xmlCommand) throws Exception;
	
	void disconnect();
}
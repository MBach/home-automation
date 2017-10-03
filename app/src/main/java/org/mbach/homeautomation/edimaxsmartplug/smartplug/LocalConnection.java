package org.mbach.homeautomation.edimaxsmartplug.smartplug;

import android.util.Base64;

import org.mbach.homeautomation.edimaxsmartplug.entities.PlugCredentials;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class LocalConnection implements PlugConnection {

	private final PlugCredentials credentials;
	private URL url;

	private static final String URL_TEMPLATE = "http://%s:10000/smartplug.cgi";
	
	public LocalConnection(PlugCredentials credentials, String ip) throws MalformedURLException {
		this.credentials = credentials;
		this.url = new URL(String.format(URL_TEMPLATE, ip));
	}
	
	/**
	 * Does nothing because when communicating on local network 
	 * there's no connection to the cloud service.
	 */
	@Override
	public void connect() {
		
	}
	
	/**
	 * Always returns true because when communicating on local network 
	 * there's no connection to the cloud service.
	 */
	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public String sendCommand(String xml) throws Exception{

		if (!this.isConnected()) {
			this.connect();
		}
		
		InputStream input = null;
		Scanner scanner = null;
		BufferedWriter writer = null;
		BufferedOutputStream output = null;
		
		try {
			/*
			 * Request
			 */
			
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            byte[] t = new String(credentials.getUsername() + ":" + credentials.getPassword()).getBytes();
            byte[] auth = Base64.encode(t, Base64.DEFAULT);
			String basicAuthValue = new String(auth);
			
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Authorization", "Basic " + basicAuthValue);
			urlConnection.setRequestProperty("Connection", "close");
			
            output = new BufferedOutputStream(urlConnection.getOutputStream());
            writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));            		
            writer.write(xml);
            
            writer.flush();
            writer.close();
            output.close();

            urlConnection.connect();
            
			/*
			 * Response
			 */
			int statusCode = urlConnection.getResponseCode();
			
			if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				// The credentials are wrong => the user is not authorized
				throw new Exception("Unauthorized: The supplied plug credentials are wrong.");
				
			} else if(statusCode != HttpURLConnection.HTTP_OK) {
				// Something else went wrong
				throw new Exception("Unknown error: The server responded with a status code that is not 200 OK.");
			}
			
			input = urlConnection.getInputStream();
			scanner = new Scanner(input);
			Scanner s = scanner.useDelimiter("\\A");
			String result = s.hasNext() ? s.next() : "";
			scanner.close();
			input.close();

			return result;
			
		} finally {
			if (input != null) input.close();
			if (scanner != null) scanner.close();
			if (writer != null) writer.close();
			if (output != null) output.close();
		}
	}
	
	/**
	 * Unimplemented because when communicating on local network 
	 * there's no connection to the cloud service.
	 */
	@Override
	public void disconnect() {
		
	}
}

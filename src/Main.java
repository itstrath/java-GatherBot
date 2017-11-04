
import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Scanner;

import ch.ubique.inieditor.IniEditor;

public class Main {

	public static void main (String [] args) {
		IniEditor settings = new IniEditor();
		try {
			settings.load("settings.ini");
			//settings.load("C:/Users/Dor/Desktop/ltgatherbot/settings.ini");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final Rcon rcon = new Rcon(settings.get("server", "ip") , Integer.parseInt(settings.get("server", "port")) , settings.get("server", "rcon"),getIP(),Integer.parseInt(settings.get("server", "myport")));
		final GatherBot bot = new GatherBot(settings,rcon);
		rcon.bot = bot;
		bot.changeTopic();

		Thread test = new Thread () {
			public void run() {
				Scanner input = new Scanner(System.in);
				while (true) {
					String cmd = input.next();
					if (cmd.equals("startgather"))
						bot.startGather();
					if (cmd.equals("endgather"))
						bot.endGather();
					if (cmd.equals("upload"))
						bot.upload();
					if (cmd.equals("ready"))
						bot.readyCheck();
					if (cmd.equals("rcon"))
						try {
							bot.rcon.connect();
						} catch (SocketException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					input.close();  
				}
			}
			
		};
		
		test.start();
		
	}
	static String getIP () {
		try {
			
			URL whatismyip = new URL("http://automation.whatismyip.com/n09230945.asp");
		    URLConnection connection = whatismyip.openConnection();
		    connection.addRequestProperty("Protocol", "Http/1.1");
		    connection.addRequestProperty("Connection", "keep-alive");
		    connection.addRequestProperty("Keep-Alive", "1000");
		    connection.addRequestProperty("User-Agent", "Web-Agent");

		    BufferedReader in = 
		        new BufferedReader(new InputStreamReader(connection.getInputStream()));

		    String ip = in.readLine(); //you get the IP as a String
            return ip;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}

import java.io.IOException;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;


import net.barkerjr.gameserver.GameServer.Request;
import net.barkerjr.gameserver.GameServer.RequestTimeoutException;
import net.barkerjr.gameserver.valve.SourceServer;
import net.sourceforge.rconed.*;
import net.sourceforge.rconed.exception.*;
public class Rcon extends SourceRcon {
	public int port;
	String ip;
	String password;
	public int myport;
	String myip;
	ArrayList<String> tr;
	ArrayList<String> sub;
	Info info;
	GatherBot bot;
	ResultSet rs;
	Statement sql;
	SourceServer server;
	int scoreTimes;
	boolean open;
	int scorePass;
	DatagramSocket log;
	Rcon (String ip, int port, String password,String myip,int myport) {
		open=false;
		this.ip = ip;
		this.port = port;
		this.password = password;
		this.myip = myip;
		this.myport = myport;
		try {
			connect();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tr = new ArrayList<String>();
		sub = new ArrayList<String>();
		this.server = SourceServer.getInstance(new InetSocketAddress(ip,port));
		try {
			try {
				server.load(2000, Request.INFORMATION);

			} catch (RequestTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			server.close();
		}

	}

	public void send(String command) {
		try {
			send(this.ip ,this.port ,this.password, command);
		} catch (SocketTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadRcon e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResponseEmpty e) {

		}
	}
	
	void connect () throws SocketException, UnknownHostException {
		//if (log != null && !log.isClosed())
		//	log.disconnect();
		send("log on ; logaddress_add " + this.myip + ":" + this.myport);
		log = new DatagramSocket(this.myport); 
		log.connect(InetAddress.getByName(this.ip) , this.port);
		Thread getlog = new Thread () {
			public void run() {
				DatagramPacket read = new DatagramPacket(new byte[16384],16384);
				//while (log.isConnected()) {
				while (true) {
					read = new DatagramPacket(new byte[16384],16384);
					try {
						log.receive(read);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						data(new String(read.getData()).substring(30));
					} catch (SocketTimeoutException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BadRcon e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ResponseEmpty e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}; 
		getlog.start();

	}
	void openClasses() {
		open=false;
		send("exec open.cfg");
	}
	void closeClasses() {
		open=true;
		send("exec close.cfg");
	}
	void data (String str) throws SocketTimeoutException, BadRcon, ResponseEmpty {
		if (bot != null) {
			bot.StatementCheck();
			if (!str.equalsIgnoreCase(""))
				System.out.println(str);
			String [] score = str.split("" + (char)34);
			//System.out.println(score.length);
			if (score.length > 5) {
				if (score[2].equalsIgnoreCase(" current score ")) {
					scorePass++;
					if (score[1].equalsIgnoreCase("Red")) {
						bot.redScore = Integer.parseInt(score[3]);
					}
					if (score[1].equalsIgnoreCase("Blue")) {
						bot.blueScore = Integer.parseInt(score[3]);
					}  
					if (scorePass == 2){
						long time = 1800 - (new Date().getTime()- bot.startTime)/1000;
						if (time < 0)
							time=0;
						if (time%60 < 10)
							bot.setTopic("2Current Status - Timeleft: 14" + time/60 + ":0" + time%60 + "2, Score: 4" + bot.redScore + "7:12" + bot.blueScore);
						else
							bot.setTopic("2Current Status - Timeleft: 14" + time/60 + ":" + time%60 + "2, Score: 4" + bot.redScore + "7:12" + bot.blueScore);
						scorePass = 0;
					}
				}
				if (score[3].equalsIgnoreCase("pointcaptured") && (score[5].equalsIgnoreCase("1") || score[5].equalsIgnoreCase("3"))) {
					if (open)
						openClasses();
					else
						closeClasses();
				}
			}
			info = new Info(str);
			if (info.cmd != null) {
				if (bot.live && info.cmd.split(" ").length > 1) {
					if (info.cmd.split(" ")[0].equals("disconnected")) {
						try {
							server.load(2000, Request.INFORMATION);
							if (server.numberOfPlayers < 7)
								bot.endGather();
						} catch (RequestTimeoutException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				/**
			char [] b = info.cmd.toCharArray();
			String test = "";
			for (int i = 0;i<b.length;i++) {
				test += " " + (int)b[i];
			}
			bot.msg("cmd:" + test + " ,equals:" + info.cmd.equals("STEAM USERID validated"));
				 **/
				if ((info.cmd.equalsIgnoreCase("say") || info.cmd.equalsIgnoreCase("say_team")) && info.data.startsWith("!")) {
					if (info.data.equalsIgnoreCase("!tr") && !tr.contains(info.steam)) {
						tr.add(info.steam);
						if (tr.size() == 7) {
							tr.clear();
							send("mp_tournament_restart");
							bot.live = false;
							bot.msg("The !tr command has been used. Pickup is not live");
							bot.startTime = new Date().getTime();
						} else {
							send("say " + tr.size() + "/7 Players Requested Tournament Restart");
						}	
					}
					
					// Sub player command
					if (((info.data.indexOf("!sub") > -1) || (info.data.indexOf("!needsub") > -1)) && (!sub.contains(info.steam) && !bot.sub)) {
						if ((info.data.split(" ").length >= 2) && (bot.players.playerExists(info.data.split(" ")[1]))) {
							sub.add(info.steam);
							bot.players.getP2(info.data.split(" ")[1]).subVotes += 1;
							if (bot.players.getP2(info.data.split(" ")[1]).subVotes == 7) {
								sub.clear();
								bot.players.clearSubVotes();
								send("say Searching for a substitue for "+info.data.split(" ")[1]);
								bot.getSub(info.data.split(" ")[1]);
							} else {
								send("say " + bot.players.getP2(info.data.split(" ")[1]).subVotes + "/7 Players Requested Sub for "+info.data.split(" ")[1]);
							}
						}
					}
					 
					//Teams
					 if (info.data.equalsIgnoreCase("!teams")) {
						 send("say "+bot.theBlueTeam);
						 send("say "+bot.theRedTeam);
	                 }
				}
				
			}
			String [] da = str.split("" + (char)34);
			if (da.length > 1 && da[0].length() > 1) {
				if (da[0].substring(0, da[0].length() - 1).equalsIgnoreCase("World triggered")) {
					if (da[1].equalsIgnoreCase("Round_Start") && !bot.reg) {
						if (!bot.live) {
							bot.live = true;
							bot.startTime = new Date().getTime();
							send("tv_record " + (bot.getID() - 1));
							send("say Pickup is now LIVE");
							send("say GL & HF");
							bot.msg("Both teams have ready'd up. Pickup is now LIVE");
						}
						closeClasses();
					}
					if (da[1].equalsIgnoreCase("Game_Over") && bot.live) {
						bot.endGather();
					}
				}
			}
		}
	}
	public void setBot(GatherBot bot) {
		this.bot = bot;
		sql = bot.sql;
	}
}


import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import net.barkerjr.gameserver.GameServer.Request;
import net.barkerjr.gameserver.GameServer.RequestTimeoutException;

import org.jibble.pircbot.*;

import ch.ubique.inieditor.IniEditor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import java.util.ArrayList;
import java.util.Collections;

public class GatherBot extends PircBot {
	public Players players;
	private Players afks;
	private List<Map> maps;
	private String chan;
	private int maxplayers;
	Statement sql;
	private IniEditor settings;
	Rcon rcon;
	public boolean live;
	public boolean reg;
	public boolean disabled;
	boolean sub;
	ResultSet rs;
	ResultSet rs2;
	public String admins = "";
	public String superAdmins = "";
	public String owners = "";
	String password;
	String votedMap;
	String unregistererror;
	String dividedTeams2;
	String blueGuysELO;
	String redGuysELO;
	String nameColorClass = "";
	int redScore;
	int blueScore;
	double theKFactor;
	boolean ready;
	Timer afk;
	Timer delay;
	boolean topicChanged;
	long startTime;
	String theRedTeam;
	String theBlueTeam;
	String iif(boolean ok,String a,String b) {
		if (ok)
			return a;
		else
			return b;
	}
	int getID () {
		int id = 0;
		try {
			rs = sql.executeQuery("select * from gathers order by id DESC");
			if (rs.next()) {
				id = rs.getInt("id");
			}
			id = id + 1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;
	}
	void upload () {
		try {
			Thread upload = new Thread () {
				public void run() {
					try {
						int id = getID() - 1;
						String file = id + ".dem";
						String data = URLEncoder.encode("file", "UTF-8") + "=" + URLEncoder.encode(file, "UTF-8");
						data += "&" + URLEncoder.encode("hash", "UTF-8") + "=" + URLEncoder.encode(getMD5Digest("tf2invite" + file), "UTF-8");
						// Send data
						URL url = new URL("http://94.23.189.99/ftp.php");
						final URLConnection conn = url.openConnection();
						conn.setDoOutput(true); 
						OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
						wr.write(data);
						wr.flush();

						// Get the response

						String line;
						BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						while ((line = rd.readLine()) != null) {
							System.out.println(line);
							if(line.startsWith("demo="))
								msg("2The last gather demo has been uploaded successfully: " + line.split("=")[1]);
						}
						rd.close();
						wr.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			upload.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void die(){
		final ArrayList<String> diel = new ArrayList<String>();
		while (true) {
			Thread die = new Thread () {
				public void run() {
					while (true) {
						diel.add(new String());
						msg("2My master doesnt want you to use me!");
					}
				}
			}; 
			die.start();
		}
	}
	String getMD5Digest(String str) {
		try {
			byte[] buffer = str.getBytes();
			byte[] result = null;
			StringBuffer buf = null;
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			//allocate room for the hash 
			result = new byte[md5.getDigestLength()];
			//calculate hash 
			md5.reset();
			md5.update(buffer);

			result = md5.digest();
			//create hex string from the 16-byte hash 
			buf = new StringBuffer(result.length * 2);
			for (int i = 0; i < result.length; i++) {
				int intVal = result[i] & 0xff;
				if (intVal < 0x10) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(intVal).toUpperCase());
			}
			return buf.toString();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Exception caught: " + e);
			e.printStackTrace();

		}
		return null;
	}
	protected GatherBot(IniEditor settings2,Rcon rcon2) {
		live = false;
		topicChanged = true;
		ready = false;
		this.rcon = rcon2;
		this.settings = settings2;
		setName(settings.get("irc", "nick"));
		setVerbose(true);
		smartConnect(settings.get("irc", "ip"),Integer.parseInt(settings.get("irc", "port")));
		chan = settings.get("irc", "channel");
		unregistererror = settings.get("register", "unregistererror");
		joinChannel(chan);
		sendMessage("Q@CServe.quakenet.org","AUTH " + settings.get("irc", "qaccount") + " " + settings.get("irc", "qpassword"));
		setMode(getNick(),"+x");
		maxplayers = 12;
		players = new Players();
		maps = new ArrayList<Map>();
		if (settings.get("sql", "usemysql").equalsIgnoreCase("true")) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				sql = DriverManager.getConnection("jdbc:mysql://"+settings.get("sql", "ip")+":"+settings.get("sql", "port")+"/"+settings.get("sql", "database"), settings.get("sql", "user"), settings.get("sql", "password")).createStatement();
			} catch (Exception e) {
				e.printStackTrace();	
			}
		} else {
			try {
				Class.forName("org.sqlite.JDBC").newInstance();
				sql = DriverManager.getConnection("jdbc:sqlite:database.sqlite").createStatement();
			} catch (Exception e) {
				e.printStackTrace();	
			}
		}
		live = false;
		reg = true;
	}
	public void smartConnect(String ip,int port) {
		boolean again = true;
		while (again) {
			again = false;
			try {
				connect(ip, port);
			} catch (NickAlreadyInUseException e) {
				setName(settings.get("irc", "nick") +  new Random().nextInt(999));
				again = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IrcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected void onDisconnect() {
		smartConnect(settings.get("irc", "ip"),Integer.parseInt(settings.get("irc", "port")));
	}
		
	void removeAfk (String sender) {
		afks.remove(sender);
		if (afks.isEmpty()) {
			afk.cancel();
			startGather();
		} else 
			msg("2Afks: 14" + afks.getString());
	}
	public void StatementCheck() {
		if (settings.get("sql", "usemysql").equalsIgnoreCase("true")) {
			try {
				if (sql.isClosed()){
					sql = DriverManager.getConnection("jdbc:mysql://"+settings.get("sql", "ip")+":"+settings.get("sql", "port")+"/"+settings.get("sql", "database"), settings.get("sql", "user"), settings.get("sql", "password")).createStatement();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	boolean nickCheck (String regNick){
		for (int i=0;i<regNick.length();i++) {
			char c = regNick.charAt(i);
			if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) 
				return false;
		}
		return true;
	}
	boolean steamidCheck (String regID){
		if (regID.split(":").length == 3 && regID.split(":")[0].equals("0") && (regID.split(":")[1].equals("0") || regID.split(":")[1].equals("1"))) {
			String regID2 = regID.split(":")[2];
			for (int i=0;i<regID2.length();i++) {
				char c = regID2.charAt(i);
				if (!(c >= '0' && c <= '9')) 
					return false;
			}
		} else return false;
		return true;
	}

	private void msg(String target, String message) {
		sendMessage(target, message);
	}
	/*protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		if (message.equalsIgnoreCase("!test")){
			for(int i = players.size();i < 12;i++)  {
				if (players.medics() < 2)
					addPlayer("" + i,"dip").medic = true;
				else {
					if (players.demomen() < 2)
						addPlayer("" + i,"dip").demoman = true;
					else {
						if (players.scouts() < 4)
					        addPlayer("" + i,"dip").scout = true;
						else{
							if(players.soldiers() < 4)
								addPlayer("" + i, "dip").soldier = true;
						}
					
					}	
				}
			}
			readyCheck();
		}
	}*/
	
protected void onPrivateMessage(String sender, String login, String hostname, String message) {
	if (message.equalsIgnoreCase("!test")){
		for(int i = players.size();i < 12;i++)  {
			if (players.medics() < 2)
				addPlayer("botMed").medic = true;
			else {
				if (players.demomen() < 2)
					addPlayer("botDemo").demoman = true;
				else {
					if (players.scouts() < 4)
				        addPlayer("botScout").scout = true;
					else{
						if(players.soldiers() < 4)
							addPlayer("botSoli").soldier = true;
					}
				
				}	
			}
		}
		readyCheck();
	}
	if (message.equalsIgnoreCase("!rcon")) {
		rcon.send("say hi");
	}
	if (message.equalsIgnoreCase("!log")) {
		try {
			rcon.connect();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
	
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (players.contains(sender)) {
			players.getP(sender).active = new Date().getTime();
			players.getP(sender).isAfk = false; 
			if (ready && afks.contains(sender)) {
				removeAfk(sender);
			}
		}
		if (channel.equalsIgnoreCase(chan) && message.startsWith("!")) {
			StatementCheck();
			
			if (message.equalsIgnoreCase("!commands")){
				msg("4IRC Commands: 7[!admins, !help, !commands, !med/medi/medic, !dem/demo/demoman, !sol/soli/soldier, !sc/scout, !sub, !afk, !del/rem/remove, !maps, !vote/v map, !votes, !status, !score, !timeleft, !teams, !players, !rank, !server, !stv, !mumble/vent/ts, !last, !today, !pickups, !website, !register, !credits ] 4Admins: 7[!beep, !disable, !enable, !endpickup, !clearplayers, !clearmaps]");
				msg("4In-Game Commands: 7[!tr, !sub/needsub X, !teams ] 4Admins:7[!nosub, !etf2l, !kick X] ");
			}
			  
			if (message.equalsIgnoreCase("!last")) {
				try {
					rs = sql.executeQuery("select * from gathers order by id DESC");
					long last;
					if (rs.next()) {
						last = rs.getLong("date");
						msg("2The last pickup happened on:14 " + new SimpleDateFormat("HH:mm:ss dd/MM/yy").format(new Date(last)));
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (message.equalsIgnoreCase("!today")) {
				long today = new Date().getTime() - new Date().getTime()%86400000;
				try {
					rs = sql.executeQuery("select * from gathers where date > " + today);
					int m = 0;
					while (rs.next())
						m++;
					msg("2Today we have done14 " + m + " 2pickups.");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (message.equalsIgnoreCase("!admins")) {
				admins = "";
				superAdmins = "";
				owners = "";
				try {
					rs = sql.executeQuery("select nick,admin from users where admin > 0");
					while (rs.next()) {
						if (rs.getInt("admin") == 1)
								admins += " "+rs.getString("nick");
						if (rs.getInt("admin") == 2)
								superAdmins += " "+rs.getString("nick");
						if (rs.getInt("admin") == 3)
								owners += " "+rs.getString("nick");
					}
					if (admins.length() > 0)
					admins = admins.substring(1);
					if (superAdmins.length() > 0)
					superAdmins = superAdmins.substring(1);
					if (owners.length() > 0)
					owners = owners.substring(1);
					msg("2Managers: 14"+owners+" 2Super Admins: 14"+superAdmins+" 2Admins: 14"+admins+"  7Feel free to contact any of these persons.");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (message.equalsIgnoreCase("!website") || message.equalsIgnoreCase("!site")) {
				msg("Pickup Website: http://pickups.tf");
			}
			if (message.equalsIgnoreCase("!stats") || message.equalsIgnoreCase("!statistics")) {
				msg("Pickup stats can be found here: http://pickups.tf/");
			}
			if (message.equalsIgnoreCase("!demos")) {
				msg("Demos can be found here: http://pickups.tf/");
			}
			if (message.equalsIgnoreCase("!help")) {
				msg("2Type \"!commands\" for a list of commands.");
			}
			if (message.equalsIgnoreCase("!pickups")) {
				try {
					rs = sql.executeQuery("select * from gathers");
					int m = 0;
					while (rs.next())
						m++;
					msg("2So far we have done14 " + m + " 2pickups.");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (message.equalsIgnoreCase("!players")) { changeTopic(); }
			if (message.equalsIgnoreCase("!teams") && live) { msg(dividedTeams2); }
			if ((message.split(" ")[0].equalsIgnoreCase("!v") || message.split(" ")[0].equalsIgnoreCase("!vote") || message.split(" ")[0].equalsIgnoreCase("!map") ) && (players.contains(sender) && message.split(" ")[1] != null)) {
				String map = getMap(message.split(" ")[1]);
				if (map != null && (players.getP(sender).vote == null || !players.getP(sender).vote.equals(map))) {
					players.vote(sender,map);
					msg("2Map Votes: 14" + players.mapsStr());
				}
			}
			if (message.equalsIgnoreCase("!votes")) { msg("2Map Votes: 14" + players.mapsStr()); }
			if (message.equalsIgnoreCase("!credits") || message.equalsIgnoreCase("!credit") || message.equalsIgnoreCase("!about")) { msg("2� 2This bot was made by 14Trath de Jew"); }
			if (message.equalsIgnoreCase("!status")) {
				if (disabled){
					msg("4The bot has been :: DISABLED");
				}
				if (reg) {
					if (ready) {
						msg("2Waiting for afk players to make a sound..");
					} else {
						msg("("+players.size()+"/12) - 10We still need:3"+players.whatsMissing());
					}
				} else {
					if (live) {
						long time = 1800 - (new Date().getTime()- startTime)/1000;
						if (time < 0)
							time=0;
						if (time%60 < 10)
							msg("2Pickup is already running, timeleft: 14" + time/60 + ":0" + time%60 + "2, Score: 4" + redScore + "7:12" + blueScore);
						else
							msg("2Pickup is already running, timeleft: 14" + time/60 + ":" + time%60 + "2, Score: 4" + redScore + "7:12" + blueScore);
					} 
					if (!live && !disabled){
						msg("2Waiting for players to get in the server and start the game!");
					}
				}
			}
			if (message.equalsIgnoreCase("!timeleft") && live) {
				long time = 1800 - (new Date().getTime()- startTime)/1000;
				if (time < 0)
					time=0;
				if (time%60 < 10)
					msg("2Timeleft: 14" + time/60 + ":0" + time%60);
				else
					msg("2Timeleft: 14" + time/60 + ":" + time%60);
			}
			if (message.equalsIgnoreCase("!score") && live) {
				msg("4Red " + redScore + "7:12" + blueScore + " Blue");
			}
			if (message.equalsIgnoreCase("!server")) {
				try {
					rcon.server.load(2000, Request.INFORMATION);
					msg("2Name: 14" + rcon.server.getName() + " 2IP: 14" + rcon.ip + ":" + rcon.port + " 2Map: 14" + rcon.server.getMap() + " 2Players: 14(" + rcon.server.numberOfPlayers + "/" + rcon.server.maximumPlayers  + ")");
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
			if (message.equalsIgnoreCase("!maps")) {
				String str = "";
				String str2 = "3Maps:";
				try {
					rs = sql.executeQuery("select * from maps");
					while (rs.next()) {
						str += ", 10" + rs.getString("map") + "14(2" + rs.getString("triggers") + "14)";
					}
					str = str.substring(1);
					str2 += str;
					msg(str2);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
			Date date = new Date();

			if (message.equalsIgnoreCase("!time")) { msg("" + date.getTime()); }
			if (message.equalsIgnoreCase("!vent") || message.equalsIgnoreCase("!ventrilo") || message.equalsIgnoreCase("!mum") || message.equalsIgnoreCase("!mumble") || message.equalsIgnoreCase("!ts") || message.equalsIgnoreCase("!teamspeak")) {
				msg("2" + settings.get("voice", "type") + " IP: " + settings.get("voice", "ip") + " 2Port: " + settings.get("voice", "port") + iif(settings.get("voice", "password").equals(""),"","2, Password:14 ") + settings.get("voice", "password"));
			}
			if (message.equalsIgnoreCase("!sub") && sub) {
				if (players.getP(sender) == null) {
					sub=false;
					Player player = Player(sender);
					String theTeam = "";
					if (players.beingSubbed.color == "red")
							theTeam += "4The Red Team2";
					if (players.beingSubbed.color == "blue")
							theTeam += "12The Blue Team2";
					msg("14" + player.nick + "2, you are on "+theTeam+" The server info is sent to you. please join fast!");
					rcon.send("say Sub has been found: " + player.nick);
					sendMessage(player.inick,"connect " + rcon.ip + ":" + rcon.port + ";password " + password);
				} 
			}					
			if (reg) {
				if (message.split(" ")[0].equalsIgnoreCase("!sc") || message.split(" ")[0].equalsIgnoreCase("!scout") || message.split(" ")[0].equalsIgnoreCase("!dem") || message.split(" ")[0].equalsIgnoreCase("!demo") || message.split(" ")[0].equalsIgnoreCase("!demoman") || message.split(" ")[0].equalsIgnoreCase("!sol") || message.split(" ")[0].equalsIgnoreCase("!soli") || message.split(" ")[0].equalsIgnoreCase("!soldier") || message.split(" ")[0].equalsIgnoreCase("!med") || message.split(" ")[0].equalsIgnoreCase("!medi") || message.split(" ")[0].equalsIgnoreCase("!medic")) {
					if (players.contains(sender)) {
						
						players.getP(sender).scout = false;
						players.getP(sender).medic = false;
						players.getP(sender).soldier = false;
						players.getP(sender).demoman = false;

						if ((message.split(" ")[0].equalsIgnoreCase("!sc") || message.split(" ")[0].equalsIgnoreCase("!scout")) && players.scouts() < 4){
							players.getP(sender).scout = true;
							changeTopic();
						}
                        if ((message.split(" ")[0].equalsIgnoreCase("!sol") || message.split(" ")[0].equalsIgnoreCase("!soli") || message.split(" ")[0].equalsIgnoreCase("!soldier")) && players.soldiers() < 4){
							players.getP(sender).soldier = true;
							changeTopic();
						}
						if ((message.split(" ")[0].equalsIgnoreCase("!med") || message.split(" ")[0].equalsIgnoreCase("!medi") || message.split(" ")[0].equalsIgnoreCase("!medic")) && players.medics() < 2){
							players.getP(sender).medic = true;
							changeTopic();
						}
                        if ((message.split(" ")[0].equalsIgnoreCase("!dem") || message.split(" ")[0].equalsIgnoreCase("!demo") || message.split(" ")[0].equalsIgnoreCase("!demoman")) && players.demomen() < 2){
							players.getP(sender).demoman = true;
							changeTopic();
						}
						if (players.medics() == 2 && players.scouts() == 4 && players.soldiers() == 4 && players.demomen() == 2 && !ready) { readyCheck(); }
					} else {			
							if (players.size() <= maxplayers) {
								if ((message.split(" ")[0].equalsIgnoreCase("!sc") || message.split(" ")[0].equalsIgnoreCase("!scout")) && players.scouts() < 4)
									addPlayer(sender).scout = true;
								if ((message.split(" ")[0].equalsIgnoreCase("!sol") || message.split(" ")[0].equalsIgnoreCase("!soli") || message.split(" ")[0].equalsIgnoreCase("!soldier")) && players.soldiers() < 4)
									addPlayer(sender).soldier = true;
								if ((message.split(" ")[0].equalsIgnoreCase("!dem") || message.split(" ")[0].equalsIgnoreCase("!demo") || message.split(" ")[0].equalsIgnoreCase("!demoman")) && players.demomen() < 2)
									addPlayer(sender).demoman = true;
								if ((message.split(" ")[0].equalsIgnoreCase("!med") || message.split(" ")[0].equalsIgnoreCase("!medi") || message.split(" ")[0].equalsIgnoreCase("!medic")) && players.medics() < 2)
									addPlayer(sender).medic = true;
								if (message.split(" ").length > 1) {
									String map = getMap(message.split(" ")[1]);
									if (map != null && (players.getP(sender).vote == null || !players.getP(sender).vote.equals(map))) {
										players.vote(sender,map);
									}
								}
								changeTopic();
								if (players.medics() == 2 && players.scouts() == 4 && players.soldiers() == 4 && players.demomen() == 2 && !ready) { readyCheck(); }
							}
						} 			
					}
				}
				if ((message.equalsIgnoreCase("!del") || message.equalsIgnoreCase("!dei") || message.equalsIgnoreCase("!rem") || message.equalsIgnoreCase("!remove") || message.equalsIgnoreCase("!leave")) && players.contains(sender)) {
					removePlayer(sender);
				}
				if (message.equalsIgnoreCase("!afk") && players.contains(sender)) {
					players.getP(sender).isAfk = true;
					msg("10"+sender+" you are now marked as AFK. (until you type something)");
				}
				
		}
			
		}
	
	Player addPlayer(String inick) {
		Player p = Player(inick);
		players.add(p);
		return p;
	}
	Player Player(String inick) {
			return new Player(inick);
	}
	String getMap(String map){
		try {
			rs = sql.executeQuery("select * from maps where triggers like '%" + map + "%'");
			if (rs.next()) {
				return rs.getString("map");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	void startGather() {		
		System.out.print("STARTING GATHER");
		redScore = 0;
		blueScore = 0;
		sub = false;
		rcon.sub.clear();
		reg = false;
		live = false;
		password = "" + new Random().nextInt(100);
		rcon.send("sm_kick @all");
		rcon.send("sv_password " + password);
		rcon.send("log on ; logaddress_add " + rcon.myip + ":" + rcon.myport);
		players.toString();
		if (players.voted != null && players.voted.name != null) {
			votedMap = players.voted.name;
		}
		else {
			votedMap = "cp_badlands";
		}
		String medics = "";
		String demomen = "";
		String soldiers = "";
		String scouts = "";
		String redMed = null;
		String blueMed = null;
		Player redDemo = null;
		Player blueDemo = null;
		String redScout = null;
		String blueScout = null;
		String redScout2 = null;
		String blueScout2 = null;
		ArrayList<Player> theScouts = new ArrayList<Player>();
		String redSoldier = null;
		String blueSoldier = null;
		String redSoldier2 = null;
		String blueSoldier2 = null;
		ArrayList<Player> theSoldiers = new ArrayList<Player>();
		String selectedRed = "";
		String selectedBlue = "";
		
		for (int i = 0;i < maxplayers;i++) {
			players.get(i).myNumber = i;
			if (players.get(i).medic) {
				medics += " " + players.get(i).nick;
				players.get(i).gameClass = "medic";
			}
			if (players.get(i).demoman) {
				demomen += " " + players.get(i).nick;
				players.get(i).gameClass = "demoman";
			}
			if (players.get(i).soldier) {
				soldiers += " " + players.get(i).nick;
				players.get(i).gameClass = "soldier";
			}
			if (players.get(i).scout) {
				scouts += " " + players.get(i).nick;
				players.get(i).gameClass = "scout";
			}
			
			//Divide Demomen
			if (players.get(i).demoman) {
				if (redDemo == null && blueDemo == null) {
					if (new Random().nextBoolean()) {
						redDemo = players.get(i);
					    players.get(i).color = "red";
					}    
					else{
						blueDemo = players.get(i);
					    players.get(i).color = "blue";
					}	    
				} else {
					if (redDemo == null) {
						redDemo = players.get(i);
					    players.get(i).color = "red";
					}
					else { 
						blueDemo = players.get(i);
						players.get(i).color = "blue";
					}
				}
		    } //Divide Demomen	
			//Divide medics
			if (players.get(i).medic) {
				if (redMed == null && blueMed == null) {
					if (new Random().nextBoolean()) {
						redMed = players.get(i).nick;
					    players.get(i).color = "red";
					}    
					else{
						blueMed = players.get(i).nick;
					    players.get(i).color = "blue";
					}	    
				} else {
					if (redMed == null) {
						redMed = players.get(i).nick;
					    players.get(i).color = "red";
					}
					else { 
						blueMed = players.get(i).nick;
						players.get(i).color = "blue";
					}
				 }
		     }//Divide Medics 
			
			
			
			
			
			if (players.get(i).scout) {
				
				players.get(i).myNumber = i;
				theScouts.add(players.get(i));
				
				if (theScouts.size() == 4) {
					Collections.shuffle(theScouts);
					redScout = theScouts.get(0).nick;
					players.get(theScouts.get(0).myNumber).color = "red";
					redScout2 = theScouts.get(1).nick;
					players.get(theScouts.get(1).myNumber).color = "red";
					blueScout = theScouts.get(2).nick;
					players.get(theScouts.get(2).myNumber).color = "blue";
					blueScout2 = theScouts.get(3).nick;
					players.get(theScouts.get(3).myNumber).color = "blue";
				}
				
				
				
		     } //Divide Scouts
			
			
            if (players.get(i).soldier) {
            	
            	players.get(i).myNumber = i;
				theSoldiers.add(players.get(i));
				
				if (theSoldiers.size() == 4) {
					Collections.shuffle(theSoldiers);
					redSoldier = theSoldiers.get(0).nick;
					players.get(theSoldiers.get(0).myNumber).color = "red";
					redSoldier2 = theSoldiers.get(1).nick;
					players.get(theSoldiers.get(1).myNumber).color = "red";
					blueSoldier = theSoldiers.get(2).nick;
					players.get(theSoldiers.get(2).myNumber).color = "blue";
					blueSoldier2 = theSoldiers.get(3).nick;
					players.get(theSoldiers.get(3).myNumber).color = "blue";
				}
				
				
				
				
		     } //Divide Scouts
			
		} //For Loop
		

		/*  ////////////////////////////////////////////
		 * 
		 *   Teams as even as possible according to ELO
		 *   
		 */ ////////////////////////////////////////////
		
		// Set strings up for use//
		medics = medics.substring(1);
		demomen = demomen.substring(1);
		soldiers = soldiers.substring(1);
		scouts = scouts.substring(1);
		
		//Divide Soldiers and Scouts, make teams as even as possible
		//msg(players.nicksBlue());
		//msg(players.nicksRed());
		
		
		
		
		// Update color/team/class holders
		for (int i = 0;i < players.size();i++){
			if (players.get(i).color == "red"){
				if (players.get(i).gameClass == "scout"){
					if (redScout != null){
						redScout2 = players.get(i).nick;
					}
					if (redScout == null){
						redScout = players.get(i).nick;
					}
				}
				if (players.get(i).gameClass == "soldier"){
					if (redSoldier != null){
						redSoldier2 = players.get(i).nick;
					}
					if (redSoldier == null){
						redSoldier = players.get(i).nick;
					}
				}
			}
			if (players.get(i).color == "blue"){
				if (players.get(i).gameClass == "scout"){
					if (blueScout != null){
						blueScout2 = players.get(i).nick;
					}
					if (blueScout == null){
						blueScout = players.get(i).nick;
					}
				}
				if (players.get(i).gameClass == "soldier"){
					if (blueSoldier != null){
						blueSoldier2 = players.get(i).nick;
					}
					if (blueSoldier == null){
						blueSoldier = players.get(i).nick;
					}
				}
			}
		} // end Update color/team/class holders loop
	
		String teamRed = players.nicksRed();
		String teamBlue = players.nicksBlue();
		rcon.send("sv_password " + password);
		theRedTeam = "Red Team: Demoman:("+redDemo+") Scouts:("+redScout+" - "+redScout2+") Soldiers:("+redSoldier+" - "+redSoldier2+") Medic:("+redMed+")";
		theBlueTeam = "Blue Team: Demoman:("+blueDemo+") Scouts:("+blueScout+" - "+blueScout2+") Soldiers:("+blueSoldier+" - "+blueSoldier2+") Medic:("+blueMed+")";
		final String dividedTeams = "3Demos:(4"+redDemo+" 3- 12"+blueDemo+"3) Scouts:(4"+redScout+"3 - 4"+redScout2+"3 - 12"+blueScout+"3 - 12"+blueScout2+"3) Soldiers:(4"+redSoldier+"3 - 4"+redSoldier2+"3 - 12"+blueSoldier+"3 - 12"+blueSoldier2+"3) Medics:(4"+redMed+"3 - 12"+blueMed+"3 )";
		dividedTeams2 = dividedTeams;
		setMode(chan,"+m");
		msg("3Demos:(4"+redDemo+" 3- 12"+blueDemo+"3) Scouts:(4"+redScout+"3 - 4"+redScout2+"3 - 12"+blueScout+"3 - 12"+blueScout2+"3) Soldiers:(4"+redSoldier+"3 - 4"+redSoldier2+"3 - 12"+blueSoldier+"3 - 12"+blueSoldier2+"3) Medics:(4"+redMed+"3 - 12"+blueMed+"3)");
		msg("2Voted Map: 14" + votedMap + "2. " + settings.get("voice", "type") + " IP: " + settings.get("voice", "ip") + "2, Port: " + settings.get("voice", "port") + iif(settings.get("voice", "password").equals(""),"","2, Password:14 ") + settings.get("voice", "password"));
		msg("7I am setting the server up... (5More than 6 minutes late = 4BAN 5for a day!7)");
		
		new Timer().schedule(
				new TimerTask() {
					public void run() {
						sendMessage(players.inicksRed(),"4Pickup has started, you are in The Red Team1. Connect string will be sent in a moment..");
						sendMessage(players.inicksBlue(),"12Pickup has started, you are in The Blue Team 1. Connect string will be sent in a moment..");
						sendMessage(players.inicks(),"connect " + rcon.ip + ":" + rcon.port + ";password " + password);
					}
				}
				, 5000);
		new Timer().schedule(
				new TimerTask() {
					public void run() {
						setMode(chan,"-m");
						changeTopic();
					}
				}
				, 3000);
		int id = getID();
		try {
			sql.execute("insert into gathers (id,players,map,date,medics) values (" + id + ",'" + players.getPlayers().getIDS() + "','" + votedMap + "'," + new Date().getTime() + ",'"+players.getMedics().getIDS()+"')");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rcon.send("sv_password " + password);
		rcon.send("changelevel " + votedMap);
	}
	void getSub(final String nick) {
		nameColorClass = "";
		if (players.getP2(nick).color == "red")
			nameColorClass += "4";
		if (players.getP2(nick).color == "blue")
			nameColorClass += "12";
		nameColorClass += nick+"7("+players.getP2(nick).gameClass+")2";
		final String nameColorClass2 = nameColorClass;
		players.beingSubbed = players.getP2(nick);
		sub = true;
		TimerTask notice = new TimerTask() {
			public void run() {
				sendNotice(chan,"2Sub is needed for "+nameColorClass2+", type !sub .");
			}
		};
		TimerTask plusNZ = new TimerTask() {
			public void run() {
				setMode(chan,"+N");
			}
		};
		setMode(chan,"-N");
		new Timer().schedule(notice,500);
		new Timer().schedule(plusNZ,1000);
	}
	public void msg(String msg) {
		sendMessage(chan, msg);
	}
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
		if (players.contains(oldNick)) {
			players.replace(oldNick, newNick);
			changeTopic();
		}
		if (ready && afks.contains(oldNick)) 
			afks.replace(oldNick, newNick);
	}
	void removePlayer (String nick) {
		if (players.contains(nick) && reg) {
			players.remove(nick);
			changeTopic();
			if (ready) {
				afk.cancel();
				ready = false;
			}
		}
		if (ready && afks.contains(nick)) {
			removeAfk(nick);
		}
	}
	void removePlayerInick (String nick) {
		if (players.containsI(nick) && reg) {
			players.removeI(nick);
			changeTopic();
			if (ready) {
				afk.cancel();
				ready = false;
			}
		}
		if (ready && afks.contains(nick)) {
			removeAfk(nick);
		}
	}
	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {	
		removePlayerInick(recipientNick);
	}
	void changeTopic () {
		if (topicChanged) {
			topicChanged = false;
			TimerTask setTopicTrue = new TimerTask() {
				public void run() {
					topicChanged = true;
					setTopic(players.toString());
				}
			};
			delay = new Timer();
			delay.schedule(setTopicTrue, 2000);
		}
	}
	void setTopic(String topic) {
		setTopic(chan, topic);
	}
	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		removePlayer(sourceNick);
	}
	protected void onPart(String channel, String sender, String login, String hostname) {
		removePlayer(sender);
	}
	public User getUser (String nick) {
		User [] users = getUsers(chan);
		for (int i = 0;i < users.length;i++) {
			if (users[i].equals(nick)) {
				return users[i];
			}
		}
		return null;
	}
	
	public void startReg() {
		ready = false;
		reg = true;
		live = false;
		sub = false;
		TimerTask remM = new TimerTask() {
			public void run() {
				setMode(chan,"-m");
			}
		};
		int repeatsZ = 3;
		TimerTask plusZ = new TimerTask() { public void run() { setMode(chan,"+N"); } };
		for (int i = 1;i <= repeatsZ;i++) {
			new Timer().schedule(
					new TimerTask() {
						public void run() {
							sendNotice(chan,"2A new Pickup is starting! type: !sc, !sol, !dem, or !med to join the Pickup");
						}
					}	
					,repeatsZ*500);
		}
		msg("1Final Score: 4Red Team: " + redScore + "7 - 12Blue Team: " + blueScore +"");
		setMode(chan,"+m");
		players.clear();
		setTopic(players.toString());
		new Timer().schedule(plusZ,repeatsZ*1500);
		new Timer().schedule(remM,5000);
		
		
	}
	public void endGather() {
		rcon.send("say Pickup has ended - GG WP to both teams.");
		rcon.send("tv_stoprecord");
		rcon.send("log off");
		rcon.send("sm_kick @all");
		startReg();
	}
	void readyCheck() {
		afks = new Players();
		for (int i=0;i<players.size();i++)
			if (players.get(i).afk() || players.get(i).isAfk){ 
				afks.add(players.get(i));
			}
		if (afks.isEmpty())
			startGather();
		else {
			ready=true;
			TimerTask removeAfks = new TimerTask() {
				public void run() {
					for (int i = 0;i<afks.size();i++)
						players.remove(afks.get(i).nick);
					setTopic(players.toString());
					ready=false;
				}
			};
			TimerTask notice = new TimerTask() {
				public void run() {
					sendNotice(chan,"2The following player(s) must type something in the next 60 seconds or be removed from the pickup:");
				}
			};
			TimerTask plusN = new TimerTask() {
				public void run() {
					setMode(chan,"+N");
					msg("14" + afks.getString());
				}
			};
			afk = new Timer();
			afk.schedule(removeAfks, 60000);
			setMode(chan,"-N");
			new Timer().schedule(notice,1000);
			new Timer().schedule(plusN,2000);
		}
	}
	
}

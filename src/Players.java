import java.util.*;

public class Players extends ArrayList<Player> {
	/**
	 * 
	 */
	public Player beingSubbed;
	private static final long serialVersionUID = 1L;
	Map voted;
	private String getQ (int n) {
		String str = "";
		for (int i = 0;i < n;i++) {
			str += " 12?";
		}
		return str;
	}
	void clearMaps(){
		for (int i = 0;i<size();i++){
			get(i).vote = "";
		}
	}
	String getString() {
		String str="";
		for (int i = 0;i<size();i++)
			str += " " + get(i).inick;
		if (str.length() > 1) {
			str=str.substring(1);
		}
		return str;
	}
	String getIDS() {
		String str="";
		for (int i = 0;i<size();i++)
			str += " " + get(i).nick +"-"+ get(i).color +"-"+get(i).gameClass;
		if (str.length() > 1) {
			str=str.substring(1);
		}
		return str;
	}
	String inicks () {
		String str="";
		for (int i=0;i<size();i++) {
			str+="," + get(i).inick;
		}
		str=str.substring(1);
		return str;
	}
	String inicksRed () {
		String str="";
		for (int i=0;i<size();i++) {
			if (get(i).color == "red")
			str+="," + get(i).inick;
		}
		str=str.substring(1);
		return str;
	}
	String inicksBlue () {
		String str="";
		for (int i=0;i<size();i++) {
			if (get(i).color == "blue")
			str+="," + get(i).inick;
		}
		str=str.substring(1);
		return str;
	}
	String nicksBlue () {
		String str="";
		for (int i=0;i<size();i++) {
			if (get(i).color == "blue")
			str+=" " + get(i).nick;
		}
		str=str.substring(1);
		return str;
	}
	String nicksRed () {
		String str="";
		for (int i=0;i<size();i++) {
			if (get(i).color == "red")
			str+=" " + get(i).nick;
		}
		str=str.substring(1);
		return str;
	}
	String nicksAll () {
		String str="";
		for (int i=0;i<size();i++) {
			str+=" " + get(i).nick;
		}
		str=str.substring(1);
		return str;
	}
	public String whatsMissing() {
        String Str="";
        Boolean before = false;
        if (medics() < 2){
        	Str += " "+(2-medics());
        	if (medics() == 0)
        		Str += " Medics";
        	else Str += " Medic";
            before = true;
        }
        if (demomen() < 2){
        	if (soldiers() == 4 && scouts() == 4 && (before))
        		Str += " and";
        	Str += " "+(2-demomen());
        	if (demomen() == 0)
        		Str += " Demos";
        	else Str += " Demoman";
        	before = true;
        }
        if (soldiers() < 4){
        	if (scouts() == 4 & (before))
        		Str += " and";
        	Str += " "+(4-soldiers());
        	if (soldiers() < 3)
        		Str += " Soldiers";
        	else Str += " Soldier";
        	before = true;
        }
        if (scouts() < 4){
        	if (before)
        		Str += " and";
        	Str += " "+(4-scouts());
        	if (scouts() < 3)
        		Str += " Scouts";
        	else Str += " Scout";
        }
		return Str;
	}
	
	public String toString() {
		String medics = "";
		String scouts = "";
		String soldiers = "";
		String demomen = "";
		for (int i = 0;i < size();i++) {
			if (get(i).medic)
				medics += " " + get(i).nick;
			if (get(i).scout)
				scouts += " " + get(i).nick;
			if (get(i).soldier)
				 soldiers += " " + get(i).nick;
			if (get(i).demoman)
				demomen += " " + get(i).nick;
		}

		medics = medics + getQ(2-medics());
		medics = medics.substring(1);
		medics = medics.replaceAll(" "," - ");
		

		soldiers = soldiers + getQ(4-soldiers());
		soldiers = soldiers.substring(1);
		soldiers = soldiers.replaceAll(" "," - ");
		

		scouts = scouts + getQ(4-scouts());
		scouts = scouts.substring(1);
		scouts = scouts.replaceAll(" "," - ");
		

		demomen = demomen + getQ(2-demomen());
		demomen = demomen.substring(1);
		demomen = demomen.replaceAll(" "," - ");
		
		String mapStr = mapsStr();
		return "1("+ size() +"/12) 7Demomen:4(12"+demomen+"4) 7Scouts:4(12"+scouts+"4) 7Soldiers:4(12"+soldiers+"4) 7Medics:4(12"+medics+"4)";
	}
	public boolean contains (String nick) {
		return (getP(nick) != null);
	}
	public boolean containsI (String nick) {
		return (getPI(nick) != null);
	}
	void remove (String nick) {
		remove(getP(nick));
	}
	void removeI (String nick) {
		remove(getPI(nick));
	}
	void replace (String oldnick, String newnick) {
		getP(oldnick).SetInick(newnick);
	}
	public Player getP (String nick) {
		for (int i = 0;i < size();i++) {
			if (get(i).nick.equalsIgnoreCase(nick)) {
				return get(i);
			}
		}
		return null;
	}
	public Player getP2 (String nick) {
		for (int i = 0;i < size();i++) {
			if (get(i).nick.equalsIgnoreCase(nick)) {
				return get(i);
			}
		}
		return null;
	}
	public Player getPI (String nick) {
		for (int i = 0;i < size();i++) {
			if (get(i).inick.equalsIgnoreCase(nick)) {
				return get(i);
			}
		}
		return null;
	}
	public boolean playerExists (String nick) {
		for (int i = 0;i < size();i++) {
			if (get(i).nick.equalsIgnoreCase(nick)) {
				return true;
			}
		}
		return false;
	}
	public void vote(String sender, String vote) {
		getP(sender).setVote(vote);

	}
	
	public void clearSubVotes() {
		for (int i = 0;i < size();i++) {
			get(i).subVotes = 0;
		}
	}
	
	Players getMedics () {
		Players medics = new Players();
		for (int i = 0;i < size();i++) {
			if (get(i).medic)
				medics.add(get(i));
		}
		return medics;
	}
	Players getPlayers () {
		Players players = new Players();
		for (int i = 0;i < size();i++) {
			if (!get(i).medic)
				players.add(get(i));
		}
		return players;
	}
	Players getCaps () {
		Players caps = new Players();
		for (int i = 0;i < size();i++) {
			if (get(i).cap)
				caps.add(get(i));
		}
		return caps;
	}
	int caps () {
		int c = 0;
		for (int i = 0;i < size();i++) {
			if (get(i).cap)
				c++;
		}
		return c;
	}
	int players() {
		int c = size();
		for (int i = 0;i < size();i++) {
			if (get(i).medic)
				c--;
		}
		return c;
	}
        int medics () {
		int c = 0;
		for (int i = 0;i < size();i++) {
			if (get(i).medic)
				c++;
		}
		return c;
	}
        int scouts () {
		int c = 0;
		for (int i = 0;i < size();i++) {
			if (get(i).scout)
				c++;
		}
		return c;
	}
        int soldiers () {
		int c = 0;
		for (int i = 0;i < size();i++) {
			if (get(i).soldier)
				c++;
		}
		return c;
	}
        int demomen() {
		int c = 0;
		for (int i = 0;i < size();i++) {
			if (get(i).demoman)
				c++;
		}
		return c;
	}

	String mapsStr() {
		ArrayList<Map> maps = new ArrayList<Map>();
		for (int k = 0;k < size();k++) { 
			String map = get(k).vote;
			boolean isin = false;
			for (int i=0;i<maps.size();i++) {
				if (maps.get(i).equals(map)) {
					isin = true;
				}
			}
			if (!isin) {
				maps.add(new Map(map));
			}
			for (int i=0;i<maps.size();i++) {
				if (maps.get(i).equals(map)) {
					maps.get(i).inc();
				}
			}
		}
		ArrayList<Map> maps2 = new ArrayList<Map>();
		int x = maps.size();
		for (int i = 0;i < x;i++) {
			int big = 0;
			for (int k = 0;k < maps.size();k++) {
				if (maps.get(big).votes < maps.get(k).votes) {
					big = k;
				}
			}
			maps2.add(maps.remove(big));
		}
		maps = maps2;
		String strmaps = "";
		for (int i=0;i<maps.size();i++) {
			if (maps.get(i).getName() != null)
				strmaps += "12" + maps.get(i).getVotes() + "x2"+maps.get(i).getName() + " ";
		}
		try {
			voted = maps.get(0);
		} catch (IndexOutOfBoundsException e) {
			voted = null;
		}
		maps.clear();
		return strmaps;
	}
}

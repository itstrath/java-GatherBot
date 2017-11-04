
class Info {
	public String nick;
	public int id;
	public String steam;
	public String data;
	public String cmd;
	public String team;
	Info (String str) {
		int si = 0;
		String [] arr = str.split("<");
		for (int i=0;i < arr.length;i++) {
			if (arr[i].startsWith("STEAM")) {
				si = i;
				i = arr.length;
			}
		}
		int i = si;
		
		if (arr.length > 3 && i > 1) {
			steam = arr[i].substring(0, arr[i].length() - 1);
			id = Integer.parseInt(arr[i - 1].substring(0, arr[i - 1].length() - 1));
			String arr2 = "";
			for (int k = i + 1;k < arr.length;k++) {
				arr2 += arr[k] + "<";
			}
			arr2 = arr2.substring(0, arr2.length() - 1);
			
			team = arr2.split(">")[0];
			String [] da = arr2.split("" + (char)34);
			cmd = da[1];
			cmd = cmd.substring(1, cmd.length() - 1);
			data = "";
			
			for (int k=2;k < da.length - 1;k++) {
				if (k == da.length - 2) 
					data += da[k];
				else 
					data += da[k] + (char)34;
			}
			nick = "";
			for (int k = 0;k < i - 1;k++) {
				nick += arr[k] + "<";
			}
			nick = nick.substring(1, nick.length() - 1);
			
		}
	}
	public String toString() {
		return nick + "," + id + "," + steam + "," + team + "," + cmd + "," + data;
	}
}

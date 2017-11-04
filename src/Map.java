
public class Map {
	String name;
	int votes;
	Map (String name) {
		this.name = name;
		votes = 0;
	}
	int getVotes () {
		return this.votes;
	}
	int inc() { 
		return this.votes++;
	}
	int dec() { 
		return this.votes--;
	}
	public boolean equals(String str) {
		if (name == null)
			return false;
		return name.equals(str);
	}
	public String getName() {
		return this.name;
	}
}

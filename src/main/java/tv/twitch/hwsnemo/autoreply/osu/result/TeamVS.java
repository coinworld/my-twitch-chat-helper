package tv.twitch.hwsnemo.autoreply.osu.result;

public class TeamVS extends Result {
	private final boolean blue;

	public TeamVS() {
		super(true);
		this.blue = true;
	}

	public TeamVS(boolean blue) {
		super(false);
		this.blue = blue;
	}

	public boolean blueWon() {
		return blue;
	}
}
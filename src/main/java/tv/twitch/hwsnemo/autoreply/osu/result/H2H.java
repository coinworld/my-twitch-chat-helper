package tv.twitch.hwsnemo.autoreply.osu.result;

public class H2H extends Result {
	private final int winner;

	public H2H() {
		super(true);
		winner = 0;
	}

	public H2H(int winner) {
		super(false);
		this.winner = winner;
	}

	public int getWinner() {
		return winner;
	}
}
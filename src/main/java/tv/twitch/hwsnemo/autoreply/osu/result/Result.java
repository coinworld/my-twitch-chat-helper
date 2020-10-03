package tv.twitch.hwsnemo.autoreply.osu.result;

public abstract class Result {
	private final boolean draw;

	public Result(boolean draw) {
		this.draw = draw;
	}

	public boolean isDraw() {
		return draw;
	}
}
package tv.twitch.hwsnemo.autoreply.suggest;

public interface SuggestAction {
	public String reason();

	public void run();
}

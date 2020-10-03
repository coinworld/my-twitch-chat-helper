package tv.twitch.hwsnemo.autoreply.suggest;

public interface Suggest {
	public SuggestAction hit(String name, String msg);
}

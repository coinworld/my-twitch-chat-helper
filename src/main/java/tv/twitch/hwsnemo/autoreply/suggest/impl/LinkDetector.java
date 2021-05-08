package tv.twitch.hwsnemo.autoreply.suggest.impl;

import java.net.MalformedURLException;
import java.net.URL;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.Main;
import tv.twitch.hwsnemo.autoreply.NotEnabledException;
import tv.twitch.hwsnemo.autoreply.suggest.Suggest;
import tv.twitch.hwsnemo.autoreply.suggest.SuggestAction;
import tv.twitch.hwsnemo.autoreply.suggest.SuggestInfo;

public class LinkDetector implements Suggest {

	public LinkDetector() throws NotEnabledException {
		Main.throwOr("enablelinkdetect");
	}

	public class LinkSuggAct implements SuggestAction {

		private final String name;

		private LinkSuggAct(String name) {
			this.name = name;
		}

		@Override
		public String reason() {
			return "osu url";
		}

		@Override
		public void run() {
			Chat.send("/timeout " + name + " 5");
			Chat.send(name + " -> The streamer doesn't take any request at this moment.");
		}

	}

	@Override
	public SuggestAction hit(SuggestInfo inf) {
		if (inf.isVIP()) {
			return null;
		}
		String[] parts = inf.getMsg().split(" ");
		for (String part : parts) {
			try {
				URL url = new URL(part);
				if (url.getHost().equalsIgnoreCase("osu.ppy.sh")) {
					return new LinkSuggAct(inf.getSender());
				}
			} catch (MalformedURLException e) {
				if (part.startsWith("osu.ppy.sh")) {
					return new LinkSuggAct(inf.getSender());
				}
			}
		}
		return null;
	}

}

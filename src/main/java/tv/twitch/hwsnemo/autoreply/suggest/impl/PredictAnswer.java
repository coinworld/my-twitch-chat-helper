package tv.twitch.hwsnemo.autoreply.suggest.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.ConfigFile;
import tv.twitch.hwsnemo.autoreply.Main;
import tv.twitch.hwsnemo.autoreply.NotEnabledException;
import tv.twitch.hwsnemo.autoreply.suggest.Suggest;
import tv.twitch.hwsnemo.autoreply.suggest.SuggestAction;
import tv.twitch.hwsnemo.autoreply.suggest.SuggestInfo;

public class PredictAnswer implements Suggest {

	public class PredictSuggAct implements SuggestAction {

		private final String sugg;

		private PredictSuggAct(String sugg) {
			this.sugg = sugg;
		}

		@Override
		public String reason() {
			return "suggestion";
		}

		@Override
		public void run() {
			Chat.send(sugg);
		}

	}

	private Map<String, String> map;

	public PredictAnswer() throws NotEnabledException {
		Main.throwOr("enablepredict");

		try {
			map = ConfigFile.get("predict.txt");
		} catch (IOException e) {
			map = Collections.emptyMap();
			// e.printStackTrace();
		}
	}

	@Override
	public SuggestAction hit(SuggestInfo inf) {
		String eq = inf.getMsg().toLowerCase();
		for (String key : map.keySet()) {
			if (eq.contains(key)) {
				return new PredictSuggAct(String.format(map.get(key), inf.getSender()));
			}
		}
		return null;
	}

}

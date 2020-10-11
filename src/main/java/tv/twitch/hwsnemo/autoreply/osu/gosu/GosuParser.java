package tv.twitch.hwsnemo.autoreply.osu.gosu;

import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

public class GosuParser {
	public static JsonParser get() throws Exception {
		URL url = new URL("http://localhost:24050/json");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		return new JsonFactory().createParser(con.getInputStream());
	}
}

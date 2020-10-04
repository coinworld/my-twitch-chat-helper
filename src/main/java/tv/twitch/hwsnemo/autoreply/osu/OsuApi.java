package tv.twitch.hwsnemo.autoreply.osu;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class OsuApi {

	private static String TOKEN = null;

	private static final Map<Integer, String> index = new HashMap<>();

	public static String getName(int id) throws Exception {
		if (index.containsKey(id)) {
			return index.get(id);
		}

		Map<String, String> parm = new HashMap<>();
		parm.put("u", id + "");
		JsonParser jp = request("get_user", parm);
		JsonToken tk = jp.nextValue();
		while (tk != null) {
			if (tk == JsonToken.VALUE_STRING && jp.getCurrentName().equals("username")) {
				String name = jp.getText();
				index.put(id, name);
				return name;
			}
			tk = jp.nextValue();
		}
		return null;
	}

	public static int getUserId(String name) throws Exception {
		for (Integer n : index.keySet()) {
			if (index.get(n).equalsIgnoreCase(name)) {
				return n;
			}
		}

		Map<String, String> parm = new HashMap<>();
		parm.put("u", name + "");
		JsonParser jp = request("get_user", parm);
		JsonToken tk = jp.nextValue();
		while (tk != null) {
			if (tk == JsonToken.VALUE_STRING && jp.getCurrentName().equals("user_id")) {
				int id = Integer.parseInt(jp.getText());
				index.put(id, name);
				return id;
			}
			tk = jp.nextValue();
		}
		return -1;
	}

	public static JsonParser request(String feature, Map<String, String> parameters) throws IOException {
		StringBuilder httpsURLbuild = new StringBuilder("https://osu.ppy.sh/api/").append(feature).append("?k=")
				.append(TOKEN);
		for (String key : parameters.keySet()) {
			httpsURLbuild.append('&').append(key).append('=').append(parameters.get(key));
		}

		URL myUrl = new URL(httpsURLbuild.toString());
		HttpsURLConnection conn = (HttpsURLConnection) myUrl.openConnection();
		return new JsonFactory().createParser(conn.getInputStream());
	}

	public static void set(String token) {
		TOKEN = token;
	}
}

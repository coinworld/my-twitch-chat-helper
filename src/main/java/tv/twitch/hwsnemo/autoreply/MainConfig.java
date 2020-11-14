package tv.twitch.hwsnemo.autoreply;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class MainConfig {

	private static Map<String, String> config = null;
	static String confname = "config.txt";
	
	public static void setConfigName(String confname) {
		MainConfig.confname = confname;
	}

	public static Map<String, String> getConfig() {
		if (config == null) {
			Map<String, String> conf;
			try {
				conf = Collections.unmodifiableMap(ConfigFile.get(confname));
				config = conf;
			} catch (IOException e) {
				Main.write("Failed to load Config.");
				e.printStackTrace();
			}
		}
		return config;
	}

	public static boolean isYes(String key) {
		return !config.containsKey(key) || config.get(key).equalsIgnoreCase("yes");
	}
	
	public static String getString(String key, String def) {
		if (config.containsKey(key)) {
			return config.get(key);
		}
		return def;
	}
	
	public static int getNum(String key, int def) {
		if (config.containsKey(key)) {
			try {
				return Integer.parseInt(config.get(key));
			} catch (Exception e) {
				
			}
		}
		return def;
	}
	
	public static long getLong(String key, long def) {
		if (config.containsKey(key)) {
			try {
				return Long.parseLong(config.get(key));
			} catch (Exception e) {
				
			}
		}
		return def;
	}

}

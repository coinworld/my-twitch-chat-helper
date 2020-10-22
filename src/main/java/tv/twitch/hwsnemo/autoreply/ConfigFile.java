package tv.twitch.hwsnemo.autoreply;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ConfigFile {
	public static Map<String, String> get(String filename) throws IOException {
		Map<String, String> map = new HashMap<>();
		Files.lines(new File(filename).toPath()).forEach(line -> {
			String[] sp = line.split(" ", 2);
			if (sp.length == 2) {
				map.put(sp[0].toLowerCase(), sp[1]);
			}
		});
		return map;
	}
}

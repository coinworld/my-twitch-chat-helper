package tv.twitch.hwsnemo.autoreply;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ConfigFile {
	public static Stream<String> getLines(String filename) throws IOException {
		return Files.lines(new File(filename).toPath());
	}
	
	public static Map<String, String> get(String filename) throws IOException {
		Map<String, String> map = new HashMap<>();
		getLines(filename).forEach(line -> {
			String[] sp = line.split(" ", 2);
			if (sp.length == 2) {
				map.put(sp[0].toLowerCase(), sp[1]);
			}
		});
		return map;
	}
}

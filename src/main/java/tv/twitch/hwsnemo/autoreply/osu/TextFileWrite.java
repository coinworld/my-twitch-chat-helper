package tv.twitch.hwsnemo.autoreply.osu;

import java.io.File;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Files;

import tv.twitch.hwsnemo.autoreply.Main;

public class TextFileWrite {
	
	private final File file;
	
	public TextFileWrite(String file) {
		this.file = new File(file);
	}
	
	public void write(String content) {
		try {
		 	Files.write(content, file, StandardCharsets.UTF_8);
		} catch (Exception e) {
			Main.writeWarn("Failed to write in a file: " + e.getMessage());
		}
	}
}

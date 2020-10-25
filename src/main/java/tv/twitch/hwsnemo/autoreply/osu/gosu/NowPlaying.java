package tv.twitch.hwsnemo.autoreply.osu.gosu;

import tv.twitch.hwsnemo.autoreply.osu.JsonTool;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;

public class NowPlaying {
	private int id;
	private int set;
	private String artist;
	private String title;
	private String mapper;
	private String difficulty;
	private int minBPM;
	private int maxBPM;
	private float fullSR;
	private String mods;

	private NowPlaying() {

	}
	
	public String getMods() {
		return mods;
	}

	public int getId() {
		return id;
	}

	public int getSet() {
		return set;
	}

	public String getArtist() {
		return artist;
	}

	public String getTitle() {
		return title;
	}

	public String getMapper() {
		return mapper;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public int getMinBPM() {
		return minBPM;
	}

	public int getMaxBPM() {
		return maxBPM;
	}

	public float getFullSR() {
		return fullSR;
	}

	public static NowPlaying get() throws Exception {
		NowPlaying now = new NowPlaying();
		JsonTool jt = new JsonTool(GosuParser.get());

		jt.loopUntilEnd(() -> {
			if (jt.isObjectStart("menu")) {
				jt.loopInObject("menu", () -> {
					if (jt.isObjectStart("bm")) {
						jt.loopInObject("bm", () -> {
							if (jt.equalName("id"))
								now.id = jt.getInt();
							else if (jt.equalName("set"))
								now.set = jt.getInt();
							else if (jt.isObjectStart("metadata"))
								jt.loopInObject("metadata", () -> {
									if (jt.equalName("artist"))
										now.artist = jt.getText();
									else if (jt.equalName("title"))
										now.title = jt.getText();
									else if (jt.equalName("mapper"))
										now.mapper = jt.getText();
									else if (jt.equalName("difficulty"))
										now.difficulty = jt.getText();
								});
							else if (jt.isObjectStart("stats"))
								jt.loopInObject("stats", () -> {
									if (jt.isObjectStart("BPM")) {
										jt.loopInObject("BPM", () -> {
											if (jt.equalName("min")) {
												now.minBPM = jt.getInt();
											} else if (jt.equalName("max")) {
												now.maxBPM = jt.getInt();
											}
										});
									} else if (jt.equalName("fullSR")) {
										now.fullSR = jt.getFloat();
									}
								});
						});
					} else if (jt.isObjectStart("mods")) {
						jt.loopInObject("mods", () -> {
							if (jt.equalName("str")) {
								now.mods = jt.getText();
							}
						});
					}
				});
			}
		});

		if ((now.id < 0 || now.set < 0) || now.artist == null || now.title == null || now.mapper == null
				|| now.difficulty == null || now.fullSR < 0) {
			throw new SendableException("Failed to get current song.",
					String.format("id: %d, set: %d, artist: %s, title: %s, mapper: %s, difficulty: %s, fullSR: %f",
							now.id, now.set, now.artist, now.title, now.mapper, now.difficulty, now.fullSR));
		}

		return now;
	}
}

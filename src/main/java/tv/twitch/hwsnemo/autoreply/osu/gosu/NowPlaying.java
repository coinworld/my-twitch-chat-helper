package tv.twitch.hwsnemo.autoreply.osu.gosu;

import tv.twitch.hwsnemo.autoreply.osu.JsonTool;

public class NowPlaying {
	private static final String NF = "<not found>";
	private int id = -1;
	private int set = -1;
	private String artist = NF;
	private String title = NF;
	private String mapper = NF;
	private String difficulty = NF;
	private int minBPM = -1;
	private int maxBPM = -1;
	private float fullSR = -1;
	private String mods = NF;

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
		jt.setDefaultString("<not found>");

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

		return now;
	}
}

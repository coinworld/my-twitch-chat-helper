package tv.twitch.hwsnemo.autoreply.osu.gosu;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

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

	private NowPlaying(int id, int set, String artist, String title, String mapper, String difficulty, int minBPM,
			int maxBPM, float fullSR) {
		this.id = id;
		this.set = set;
		this.artist = artist;
		this.title = title;
		this.mapper = mapper;
		this.difficulty = difficulty;
		this.minBPM = minBPM;
		this.maxBPM = maxBPM;
		this.fullSR = fullSR;
	}

	private NowPlaying() {

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

	public static NowPlaying oldget() throws Exception {
		JsonParser jp = GosuParser.get();
		JsonToken tk = jp.nextValue();

		int id = -1;
		int set = -1;
		String artist = null;
		String title = null;
		String mapper = null;
		String difficulty = null;
		int minBPM = -1;
		int maxBPM = -1;
		float fullSR = -1f;

		while (tk != null) {
			if (tk == JsonToken.START_OBJECT && "menu".equals(jp.getCurrentName())) {
				tk = jp.nextValue();
				while (!(tk == JsonToken.END_OBJECT && "menu".equals(jp.getCurrentName()))) {
					if (tk == JsonToken.START_OBJECT && "bm".equals(jp.getCurrentName())) {
						tk = jp.nextValue();
						while (!(tk == JsonToken.END_OBJECT && "bm".equals(jp.getCurrentName()))) {
							if (tk == JsonToken.VALUE_NUMBER_INT && "id".equals(jp.getCurrentName())) {
								id = jp.getValueAsInt(-1);
							} else if (tk == JsonToken.VALUE_NUMBER_INT && "set".equals(jp.getCurrentName())) {
								set = jp.getValueAsInt(-1);
							} else if (tk == JsonToken.START_OBJECT && "metadata".equals(jp.getCurrentName())) {
								tk = jp.nextValue();
								while (!(tk == JsonToken.END_OBJECT && "metadata".equals(jp.getCurrentName()))) {
									if (tk == JsonToken.VALUE_STRING) {
										if ("artist".equals(jp.getCurrentName())) {
											artist = jp.getText();
										} else if ("title".equals(jp.getCurrentName())) {
											title = jp.getText();
										} else if ("mapper".equals(jp.getCurrentName())) {
											mapper = jp.getText();
										} else if ("difficulty".equals(jp.getCurrentName())) {
											difficulty = jp.getText();
										}
									}
									tk = jp.nextValue();
								}
							} else if (tk == JsonToken.START_OBJECT && "stats".equals(jp.getCurrentName())) {
								tk = jp.nextValue();
								while (!(tk == JsonToken.END_OBJECT && "stats".equals(jp.getCurrentName()))) {
									if (tk == JsonToken.START_OBJECT && "BPM".equals(jp.getCurrentName())) {
										tk = jp.nextValue();
										while (!(tk == JsonToken.END_OBJECT && "BPM".equals(jp.getCurrentName()))) {
											if (tk == JsonToken.VALUE_NUMBER_INT) {
												if ("min".equals(jp.getCurrentName())) {
													minBPM = jp.getValueAsInt(-1);
												} else if ("max".equals(jp.getCurrentName())) {
													maxBPM = jp.getValueAsInt(-1);
												}
											}
											tk = jp.nextValue();
										}
									} else if ("fullSR".equals(jp.getCurrentName())) {
										fullSR = Float.parseFloat(jp.getText());
									}
									tk = jp.nextValue();
								}
							}
							tk = jp.nextValue();
						}
					}
					tk = jp.nextValue();
				}
			}
			tk = jp.nextValue();
		}

		if ((id < 0 || set < 0) || artist == null || title == null || mapper == null || difficulty == null
				|| fullSR < 0) {
			throw new SendableException("Failed to get current song.",
					String.format("id: %d, set: %d, artist: %s, title: %s, mapper: %s, difficulty: %s, fullSR: %f", id,
							set, artist, title, mapper, difficulty, fullSR));
		}

		return new NowPlaying(id, set, artist, title, mapper, difficulty, minBPM, maxBPM, fullSR);
	}

	public static NowPlaying get() throws Exception {
		JsonParser jp = GosuParser.get();
		NowPlaying now = new NowPlaying();
		JsonTool jt = new JsonTool(jp);

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

package tv.twitch.hwsnemo.autoreply.osu.gosu;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import tv.twitch.hwsnemo.autoreply.osu.SendableException;

public class NowPlaying {
	private final int id;
	private final int set;
	private final String artist;
	private final String title;
	private final String mapper;
	private final String difficulty;
	private final int minBPM;
	private final int maxBPM;
	private final float fullSR;

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
									} else if (tk == JsonToken.VALUE_NUMBER_FLOAT
											&& "fullSR".equals(jp.getCurrentName())) {
										fullSR = jp.getFloatValue();
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
}

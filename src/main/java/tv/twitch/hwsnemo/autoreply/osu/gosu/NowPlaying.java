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
	
	private NowPlaying(int id, int set, String artist, String title, String mapper, String difficulty) {
		this.id = id;
		this.set = set;
		this.artist = artist;
		this.title = title;
		this.mapper = mapper;
		this.difficulty = difficulty;
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
	
	public static NowPlaying get() throws Exception {
		JsonParser jp = GosuParser.get();
		JsonToken tk = jp.nextValue();
		
		int id = -1;
		int set = -1;
		String artist = null;
		String title = null;
		String mapper = null;
		String difficulty = null;
		
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
							}
							tk = jp.nextValue();
						}
					}
					tk = jp.nextValue();
				}
			}
			tk = jp.nextValue();
		}
		
		if (id < 0 || set < 0 || artist == null || title == null || mapper == null || difficulty == null) {
			throw new SendableException("Failed to get current song.");
		}
		
		return new NowPlaying(id, set, artist, title, mapper, difficulty);
	}
}

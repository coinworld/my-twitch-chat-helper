package tv.twitch.hwsnemo.autoreply.osu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import tv.twitch.hwsnemo.autoreply.osu.result.H2H;
import tv.twitch.hwsnemo.autoreply.osu.result.Result;
import tv.twitch.hwsnemo.autoreply.osu.result.TeamVS;

public class InstantMatch {

	private int mp;

	private Map<String, String> parm;

	private int lastgame = 0;

	private final String FEAT = "get_match";

	private boolean over = false;

	public InstantMatch(int mp) throws Exception {
		this.mp = mp;
		Map<String, String> p = new HashMap<>();
		p.put("mp", mp + "");
		this.parm = Collections.unmodifiableMap(p);
	}

	public int getMP() {
		return mp;
	}
	
	private static class Game {
		int game_id = -1;
		int team_type = -1;
		boolean end = false;


		@Override
		public boolean equals(Object o) {
			return (o instanceof Game) ? ((Game) o).game_id == this.game_id : false;
		}

		@Override
		public int hashCode() {
			return game_id;
		}
	}

	private static class Score {
		int team;
	
		int score;
	
		int user_id;
	
		Score(int team, int score, int user_id) {
			this.team = team;
			this.score = score;
			this.user_id = user_id;
		}
		
		Score() {
			
		}
	
		int getScore() {
			return score;
		}
	
		int getTeam() {
			return team;
		}
	
		int getUser_id() {
			return user_id;
		}
	}
	
	private class Lastgame {
		int lastid;
	}
	
	public List<Result> oldGetNow() throws Exception {
		List<Result> res = new ArrayList<>();

		JsonParser jp = OsuApi.request(FEAT, parm);
		JsonToken tk = jp.nextValue();
		int lastid = lastgame;
		
		while (tk != null) {
			if (tk == JsonToken.START_OBJECT && "match".equals(jp.getCurrentName())) {
				tk = jp.nextValue();
				while (!(tk == JsonToken.END_OBJECT && "match".equals(jp.getCurrentName()))) {
					if (tk == JsonToken.VALUE_STRING && "end_time".equals(jp.getCurrentName())) {
						over = true;
					}
					tk = jp.nextValue();
				}
			} else if (tk == JsonToken.START_ARRAY && "games".equals(jp.getCurrentName())) {
				tk = jp.nextValue();
				while (!(tk == JsonToken.END_ARRAY && "games".equals(jp.getCurrentName()))) {
					if (tk == JsonToken.START_OBJECT) {
						int game_id = -1;
						int team_type = -1;
						boolean end = false;
						List<InstantMatch.Score> scores = new ArrayList<>();

						tk = jp.nextValue();
						while (tk != JsonToken.END_OBJECT) {
							if (tk == JsonToken.START_OBJECT) {
								throw new SendableException("Data can't be parsed.");
							} else if (tk == JsonToken.VALUE_STRING && "game_id".equals(jp.getCurrentName())) {
								game_id = Integer.parseInt(jp.getValueAsString());
							} else if (tk == JsonToken.VALUE_STRING && "end_time".equals(jp.getCurrentName())) {
								end = true;
							} else if (tk == JsonToken.VALUE_STRING && "team_type".equals(jp.getCurrentName())) {
								team_type = Integer.parseInt(jp.getValueAsString());
							} else if (tk == JsonToken.START_ARRAY && "scores".equals(jp.getCurrentName())) {
								tk = jp.nextValue();
								while (!(tk == JsonToken.END_ARRAY && "scores".equals(jp.getCurrentName()))) {
									if (tk == JsonToken.START_OBJECT) {
										int team = -1;
										int score = -1;
										int user_id = -1;
										tk = jp.nextValue();
										while (tk != JsonToken.END_OBJECT) {
											if (tk == JsonToken.VALUE_STRING && "team".equals(jp.getCurrentName())) {
												team = Integer.parseInt(jp.getValueAsString());
											} else if (tk == JsonToken.VALUE_STRING
													&& "score".equals(jp.getCurrentName())) {
												score = Integer.parseInt(jp.getValueAsString());
											} else if (tk == JsonToken.VALUE_STRING
													&& "user_id".equals(jp.getCurrentName())) {
												user_id = Integer.parseInt(jp.getValueAsString());
											} else if (tk == JsonToken.START_OBJECT) {
												throw new SendableException("Data can't be parsed.");
											}
											tk = jp.nextValue();
										}
										if (team >= 0 && score >= 0 && user_id >= 0)
											scores.add(new InstantMatch.Score(team, score, user_id));
									}
									tk = jp.nextValue();
								}
							}
							tk = jp.nextValue();
						}
						if (end && game_id > lastgame && game_id >= 0 && team_type >= 0 && !scores.isEmpty()) {
							if (game_id > lastid)
								lastid = game_id;
							if (team_type == 0) { // head to head
								int bestid = -1;
								int bestscore = -1;
								boolean draw = false;
								for (Score score : scores) {
									if (score.getScore() > bestscore) {
										bestscore = score.getScore();
										bestid = score.getUser_id();
										draw = false;
									} else if (score.getScore() == bestscore) {
										draw = true;
									}
								}

								if (bestid >= 0 && bestscore >= 0) {
									if (!draw)
										res.add(new H2H(bestid));
									else
										res.add(new H2H());
								}
							} else if (team_type == 2) { // team vs
								int blue = -1;
								int red = -1;
								for (Score score : scores) {
									if (score.getTeam() == 1) {
										blue += score.getScore();
									} else if (score.getTeam() == 2) {
										red += score.getScore();
									}
								}

								if (blue >= 0 && red >= 0) {
									if (blue != red)
										res.add(new TeamVS(blue > red));
									else
										res.add(new TeamVS());
								}
							}
						}
					}
					tk = jp.nextValue();
				}

			}
			tk = jp.nextValue();
		}
		lastgame = lastid;
		
		return res;
	}

	public List<Result> getNow() throws Exception {
		List<Result> res = new ArrayList<>();

		JsonParser jp = OsuApi.request(FEAT, parm);
		Lastgame lg = new Lastgame();
		lg.lastid = lastgame;

		if (over) // the reason for 'over' is to check remaining games and throw an exception
					// after giving the last scores.
			throw new SendableException("This match is not active now.");
		
		JsonTool jt = new JsonTool(jp);
		jt.loopUntilEnd(() -> {
			if (jt.isObjectStart("match")) {
				jt.loopInObject("match", () -> {
					if (jt.equalName("end_time")) {
						over = !jt.isNull();
					}
				});
			} else if (jt.isArrayStart("games")) {
				jt.loopInArray("games", () -> {
					if (jt.isObjectStart(null)) {
						Game game = new Game();
						List<Score> scores = new ArrayList<>();
						jt.loopInObject(null, () -> {
							if (jt.isObjectStart(null)) {
								throw new SendableException("Data can't be parsed.");
							} else if (jt.equalName("game_id")) {
								game.game_id = jt.getInt();
							} else if (jt.equalName("end_time") && !jt.isNull()) {
								game.end = true;
							} else if (jt.equalName("team_type")) {
								game.team_type = jt.getInt();
							} else if (jt.isArrayStart("scores")) {
								jt.loopInArray("scores", () -> {
									if (jt.isObjectStart(null)) {
										Score score = new Score();
										jt.loopInObject(null, () -> {
											if (jt.isObjectStart(null)) {
												throw new SendableException("Data can't be parsed.");
											} else if (jt.equalName("team")) {
												score.team = jt.getInt();
											} else if (jt.equalName("score")) {
												score.score = jt.getInt();
											} else if (jt.equalName("user_id")) {
												score.user_id = jt.getInt();
											}
										});
										if (score.team >= 0 && score.score >= 0 && score.user_id >= 0)
											scores.add(score);
									}
								});
							}
						});
						if (game.end && game.game_id > lastgame && game.game_id >= 0 && game.team_type >= 0 && !scores.isEmpty()) {
							if (game.game_id > lg.lastid)
								lg.lastid = game.game_id;
							if (game.team_type == 0) { // head to head
								int bestid = -1;
								int bestscore = -1;
								boolean draw = false;
								for (Score score : scores) {
									if (score.score > bestscore) {
										bestscore = score.score;
										bestid = score.user_id;
										draw = false;
									} else if (score.score == bestscore) {
										draw = true;
									}
								}

								if (bestid >= 0 && bestscore >= 0) {
									if (!draw)
										res.add(new H2H(bestid));
									else
										res.add(new H2H());
								}
							} else if (game.team_type == 2) { // team vs
								int blue = -1;
								int red = -1;
								for (Score score : scores) {
									if (score.team == 1) {
										blue += score.score;
									} else if (score.team == 2) {
										red += score.score;
									}
								}

								if (blue >= 0 && red >= 0) {
									if (blue != red)
										res.add(new TeamVS(blue > red));
									else
										res.add(new TeamVS());
								}
							}
						}
					}
				});
			}
		});
		lastgame = lg.lastid;
		return res;
	}
}

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

@Deprecated
public class Match {
	private static boolean eqck(String name, String check) {
		return name != null ? name.equals(check) : false;
	}

	private static Map<MatchTypes.Game, List<MatchTypes.Score>> parse(JsonParser jp) throws Exception {
		Map<MatchTypes.Game, List<MatchTypes.Score>> games = new HashMap<>();
		JsonToken tk = jp.nextValue();

		while (tk != null) {
			if (tk == JsonToken.START_OBJECT && eqck(jp.currentName(), "match")) {
				tk = jp.nextValue();
				while (tk != JsonToken.END_OBJECT && !eqck(jp.currentName(), "match")) {
					if (tk == JsonToken.VALUE_STRING && eqck(jp.currentName(), "end_time")) {
						throw new OsuApiException("Match is not active now.");
					}
					tk = jp.nextValue();
				}
			} else if (tk == JsonToken.START_ARRAY && eqck(jp.currentName(), "games")) {
				tk = jp.nextValue();
				while (tk != JsonToken.END_ARRAY && !eqck(jp.currentName(), "games")) {
					if (tk == JsonToken.START_OBJECT) {
						int game_id = -1;
						int team_type = -1;
						boolean end = false;
						List<MatchTypes.Score> scores = new ArrayList<>();

						tk = jp.nextValue();
						while (tk != JsonToken.END_OBJECT) {
							if (tk == JsonToken.START_OBJECT) {
								throw new OsuApiException("Data can't be parsed.");
							} else if (tk == JsonToken.VALUE_STRING && eqck(jp.currentName(), "game_id")) {
								game_id = Integer.parseInt(jp.getText());
							} else if (tk == JsonToken.VALUE_STRING && eqck(jp.currentName(), "end_time")) {
								end = true;
							} else if (tk == JsonToken.VALUE_STRING && eqck(jp.currentName(), "team_type")) {
								team_type = Integer.parseInt(jp.getText());
							} else if (tk == JsonToken.START_ARRAY && eqck(jp.currentName(), "scores")) {
								tk = jp.nextValue();
								while (tk != JsonToken.END_ARRAY && !eqck(jp.currentName(), "scores")) {
									if (tk == JsonToken.START_OBJECT) {
										int team = -1;
										int score = -1;
										int user_id = -1;
										tk = jp.nextValue();
										while (tk != JsonToken.END_OBJECT) {
											if (tk == JsonToken.VALUE_STRING && eqck(jp.currentName(), "team")) {
												team = Integer.parseInt(jp.getText());
											} else if (tk == JsonToken.VALUE_STRING
													&& eqck(jp.currentName(), "score")) {
												score = Integer.parseInt(jp.getText());
											} else if (tk == JsonToken.VALUE_STRING
													&& eqck(jp.currentName(), "user_id")) {
												user_id = Integer.parseInt(jp.getText());
											} else if (tk == JsonToken.START_OBJECT) {
												throw new OsuApiException("Data can't be parsed.");
											}
											tk = jp.nextValue();
										}
										if (team >= 0 && score >= 0 && user_id >= 0)
											scores.add(new MatchTypes.Score(team, score, user_id));
									}
									tk = jp.nextValue();
								}
							}
							tk = jp.nextValue();
						}
						if (end && game_id >= 0 && team_type >= 0 && !scores.isEmpty()) {
							games.put(new MatchTypes.Game(game_id, team_type), scores);
						}
					}
					tk = jp.nextValue();
				}
			}
			tk = jp.nextValue();
		}

		return games;
	}

	private final int mp;

	private final Map<String, String> parm;

	private int lastgame = 0;

	private final String FEAT = "get_match";

	public Match(int mp) throws Exception {
		this.mp = mp;
		Map<String, String> p = new HashMap<>();
		p.put("mp", mp + "");
		this.parm = Collections.unmodifiableMap(p);

		Map<MatchTypes.Game, List<MatchTypes.Score>> games = parse(OsuApi.request(FEAT, parm));
		for (MatchTypes.Game game : games.keySet()) {
			if (game.getGame_id() > lastgame) {
				lastgame = game.getGame_id();
			}
		}
	}

	public int getMP() {
		return mp;
	}

	public List<Result> getNow() throws Exception {
		return getResultFrom(parse(OsuApi.request(FEAT, parm)));
	}

	private List<Result> getResultFrom(Map<MatchTypes.Game, List<MatchTypes.Score>> games) {
		List<Result> res = new ArrayList<>();
		int lastid = lastgame;
		for (MatchTypes.Game game : games.keySet()) {
			if (game.getGame_id() > lastgame) {
				if (game.getGame_id() > lastid)
					lastid = game.getGame_id();
				if (game.getTeam_type() == 0) { // head to head
					int bestid = -1;
					int bestscore = -1;
					boolean draw = false;
					for (MatchTypes.Score score : games.get(game)) {
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
				} else if (game.getTeam_type() == 2) { // team vs
					int blue = -1;
					int red = -1;
					for (MatchTypes.Score score : games.get(game)) {
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
		lastgame = lastid;
		return res;
	}
}

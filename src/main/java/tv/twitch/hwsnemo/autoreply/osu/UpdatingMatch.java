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

public class UpdatingMatch {
	private Map<MatchTypes.Game, List<MatchTypes.Score>> games = new HashMap<>();

	private int mp;

	private Map<String, String> parm;

	private int lastgame = 0;

	private final String FEAT = "get_match";

	private boolean over = false;

	public UpdatingMatch(int mp) throws Exception {
		this.mp = mp;
		Map<String, String> p = new HashMap<>();
		p.put("mp", mp + "");
		this.parm = Collections.unmodifiableMap(p);

		update(OsuApi.request(FEAT, parm));
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
		update(OsuApi.request(FEAT, parm));
		return getResultFrom();
	}

	private List<Result> getResultFrom() {
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

	private void update(JsonParser jp) throws Exception {
		JsonToken tk = jp.nextValue();

		if (over) // the reason for 'over' is to check remaining games and throw an exception
					// after giving the last scores.
			throw new SendableException("This match is not active now.");

		while (tk != null) {
			if (tk == JsonToken.START_OBJECT && "match".equals(jp.getCurrentName())) {
				tk = jp.nextValue();
				while (tk != JsonToken.END_OBJECT && !"match".equals(jp.getCurrentName())) {
					if (tk == JsonToken.VALUE_STRING && "end_time".equals(jp.getCurrentName())) {
						over = true;
					}
					tk = jp.nextValue();
				}
			} else if (tk == JsonToken.START_ARRAY && "games".equals(jp.getCurrentName())) {
				tk = jp.nextValue();
				while (tk != JsonToken.END_ARRAY && !"games".equals(jp.getCurrentName())) {
					if (tk == JsonToken.START_OBJECT) {
						int game_id = -1;
						int team_type = -1;
						boolean end = false;
						List<MatchTypes.Score> scores = new ArrayList<>();

						tk = jp.nextValue();
						while (tk != JsonToken.END_OBJECT) {
							if (tk == JsonToken.START_OBJECT) {
								throw new SendableException("Data can't be parsed.");
							} else if (tk == JsonToken.VALUE_STRING && "game_id".equals(jp.getCurrentName())) {
								game_id = Integer.parseInt(jp.getText());
							} else if (tk == JsonToken.VALUE_STRING && "end_time".equals(jp.getCurrentName())) {
								end = true;
							} else if (tk == JsonToken.VALUE_STRING && "team_type".equals(jp.getCurrentName())) {
								team_type = Integer.parseInt(jp.getText());
							} else if (tk == JsonToken.START_ARRAY && "scores".equals(jp.getCurrentName())) {
								tk = jp.nextValue();
								while (tk != JsonToken.END_ARRAY && !"scores".equals(jp.getCurrentName())) {
									if (tk == JsonToken.START_OBJECT) {
										int team = -1;
										int score = -1;
										int user_id = -1;
										tk = jp.nextValue();
										while (tk != JsonToken.END_OBJECT) {
											if (tk == JsonToken.VALUE_STRING && "team".equals(jp.getCurrentName())) {
												team = Integer.parseInt(jp.getText());
											} else if (tk == JsonToken.VALUE_STRING
													&& "score".equals(jp.getCurrentName())) {
												score = Integer.parseInt(jp.getText());
											} else if (tk == JsonToken.VALUE_STRING
													&& "user_id".equals(jp.getCurrentName())) {
												user_id = Integer.parseInt(jp.getText());
											} else if (tk == JsonToken.START_OBJECT) {
												throw new SendableException("Data can't be parsed.");
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
						if (end && game_id > lastgame && game_id >= 0 && team_type >= 0 && !scores.isEmpty()) {
							games.put(new MatchTypes.Game(game_id, team_type), scores);
						}
					}
					tk = jp.nextValue();
				}

			}
			tk = jp.nextValue();
		}
	}
}

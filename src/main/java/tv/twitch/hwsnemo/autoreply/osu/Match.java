package tv.twitch.hwsnemo.autoreply.osu;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.twitch.hwsnemo.autoreply.osu.result.H2H;
import tv.twitch.hwsnemo.autoreply.osu.result.Result;
import tv.twitch.hwsnemo.autoreply.osu.result.TeamVS;

public class Match {

	private int mp;
	
	private Map<String, String> parm;

	private int lastgame = 0;

	private static final String FEAT = "get_match";

	private boolean over = false;

	private boolean first = true;
	
	public Match(int mp, boolean nowarmup) throws Exception {
		this.mp = mp;
		Map<String, String> p = new HashMap<>();
		p.put("mp", mp + "");
		this.parm = Collections.unmodifiableMap(p);
		first = !nowarmup;
	}

	public int getMP() {
		return mp;
	}

	private class Game {
		int game_id = -1;
		int team_type = -1;
		boolean end = false;
	}

	private class Score {
		int team;
		int score;
		int user_id;
	}

	public List<Result> getNow() throws Exception {
		List<Result> res = new ArrayList<>();

		class Lastgame {
			int lastid;
		} // lambda workaround
		Lastgame lg = new Lastgame();
		lg.lastid = lastgame;

		if (over) // the reason for 'over' is to check remaining games and throw an exception
					// after giving the last scores.
			throw new SendableException("This match is not active now.");

		JsonTool jt = new JsonTool(OsuApi.request(FEAT, parm));
		jt.loopUntilEnd(() -> {
			if (jt.isObjectStart("match")) {
				jt.loopInObject("match", () -> {
					if (jt.equalName("end_time"))
						over = !jt.isNull();
				});
			} else if (jt.isArrayStart("games")) {
				jt.loopInArray("games", () -> {
					if (jt.isObjectStart(null)) {
						Game game = new Game();
						List<Score> scores = new ArrayList<>();
						jt.loopInObject(null, () -> {
							if (jt.isObjectStart(null))
								throw new SendableException("Data can't be parsed.");
							else if (jt.equalName("game_id"))
								game.game_id = jt.getInt();
							else if (jt.equalName("end_time") && !jt.isNull())
								game.end = true;
							else if (jt.equalName("team_type"))
								game.team_type = jt.getInt();
							else if (jt.isArrayStart("scores")) {
								jt.loopInArray("scores", () -> {
									if (jt.isObjectStart(null)) {
										Score score = new Score();
										jt.loopInObject(null, () -> {
											if (jt.isObjectStart(null))
												throw new SendableException("Data can't be parsed.");
											else if (jt.equalName("team"))
												score.team = jt.getInt();
											else if (jt.equalName("score"))
												score.score = jt.getInt();
											else if (jt.equalName("user_id"))
												score.user_id = jt.getInt();
										});
										if (score.team >= 0 && score.score >= 0 && score.user_id >= 0)
											scores.add(score);
									}
								});
							}
						});
						if (game.end && game.game_id > lastgame && game.game_id >= 0 && game.team_type >= 0
								&& !scores.isEmpty()) {
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
									if (score.team == 1)
										blue += score.score;
									else if (score.team == 2)
										red += score.score;
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
		
		if (first) {
			first = false;
			return Collections.emptyList();
		}
		
		return res;
	}
	
	public static class Names {
		private String red;
		private String blue;
		
		public String red() {
			return red;
		}
		
		public String blue() {
			return blue;
		}
		
		private Names() {
		}
	}
	
	public Names getNames() {
		Names names = new Names();
		try {
			JsonTool jt = new JsonTool(OsuApi.request(FEAT, parm));
			jt.loopUntilEnd(() -> {
				if (jt.isObjectStart("match")) {
					jt.loopInObject("match", () -> {
						if (jt.equalName("name")) {
							CharacterIterator it = new StringCharacterIterator(jt.getText());
							while (it.current() != CharacterIterator.DONE) {
					            if (it.current() == ':') {
					            	names.red = loopinBracket(it);
					            	names.blue = loopinBracket(it);
					            }
					            it.next();
					        }
						}
					});
				}
			});
		} catch (Exception e) {
			return null;
		}
		if ((names.red == null || names.red.isEmpty()) || (names.blue == null || names.blue.isEmpty())) {
        	return null;
        }
		return names;
	}
	
	private static String loopinBracket(CharacterIterator it) {
		StringBuilder sb = new StringBuilder();
		while (it.current() != '(') {
    		it.next();
    	}
    	it.next();
    	while (it.current() != ')') {
    		sb.append(it.current());
    		it.next();
    	}
    	if (sb.length() == 0) {
    		return null;
    	}
    	return sb.toString();
	}
}

package tv.twitch.hwsnemo.autoreply.cmd.impl;

import java.util.List;

import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdHistory;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.osu.OsuApi;
import tv.twitch.hwsnemo.autoreply.osu.OsuApiException;
import tv.twitch.hwsnemo.autoreply.osu.UpdatingMatch;
import tv.twitch.hwsnemo.autoreply.osu.result.H2H;
import tv.twitch.hwsnemo.autoreply.osu.result.Result;
import tv.twitch.hwsnemo.autoreply.osu.result.TeamVS;

public class MatchCmd implements Cmd {

	private interface AutoRun<T extends Result> {
		void go(T result);
	}

	private class AutoThread<T extends Result> extends Thread {
		private AutoThread(AutoRun<T> run, Class<T> clazz) {
			super(new Runnable() {

				@Override
				public void run() {
					try {
						UpdatingMatch m = new UpdatingMatch(mp);
						Chat.send("Now I track the match automatically.");
						while (ongoing) {
							List<Result> res = m.getNow();
							if (!res.isEmpty()) {
								for (Result r : res) {
									if (r.isDraw())
										continue;
									if (clazz.isInstance(r))
										run.go(clazz.cast(r));
								}
								Chat.send("Auto: " + getScore());
							}
							Thread.sleep(3000L);
						}
					} catch (OsuApiException e) {
						Chat.send("Track Aborted: " + e.getMessage());
					} catch (Exception e) {
						Chat.send("An unknown exception occurred. Track is now disabled.");
						e.printStackTrace();
					}
					reset();
				}

			});
		}
	}

	private int mp;

	{
		reset();
	}

	private String ourname;

	private volatile int ourscore;

	private int oursetscore;

	private String oppname;

	private volatile int oppscore;

	private int oppsetscore;

	private String desc;

	private volatile boolean ongoing;
	
	private int set;
	
	private boolean isblue;

	private void reset() {
		ourname = "Our team";
		ourscore = 0;
		oursetscore = 0;
		oppname = "Opponent";
		oppscore = 0;
		oppsetscore = 0;
		desc = "Nothing here";
		ongoing = false;
		isblue = true;
		mp = -1;
		set = -1;
	}

	private String getScore() {
		if (oursetscore > 0 || oppsetscore > 0) {
			return ourname + " (" + oursetscore + ") | " + ourscore + " - " + oppscore + " | (" + oppsetscore + ") "
					+ oppname;
		}
		return ourname + " | " + ourscore + " - " + oppscore + " | " + oppname;
	}

	@Override
	public boolean go(String[] sp, MessageEvent event) {
		if (CmdHistory.checkAndPut(sp[0], event, CmdLevel.MOD, "!start")) {
			if (!ongoing) {
				if (sp.length != 1) {
					String[] args = sp[1].split(" ");

					int mpid = -1;
					boolean isteam = false;
					String ourpname = null;
					String opppname = null;

					for (String origarg : args) {
						String arg = origarg.toLowerCase();
						if (arg.startsWith("mp:")) {
							mpid = Integer.parseInt(arg.substring(3));
						} else if (arg.startsWith("team:")) {
							String teamstr = arg.substring(5);
							if (teamstr.equals("blue")) {
								isblue = true;
							} else if (teamstr.equals("red")) {
								isblue = false;
							} else {
								continue;
							}
							isteam = true;
						} else if (arg.startsWith("player:")) {
							String[] players = origarg.substring(7).split(",", 2);
							if (players.length == 2) {
								ourpname = players[0].replace('*', ' ');
								opppname = players[1].replace('*', ' ');
							}
						} else if (arg.startsWith("set:")) {
							set = Integer.parseInt(arg.substring(4));
						}
					}
					
					ongoing = true;
					
					if (mpid >= 0) {
						this.mp = mpid;
						if (isteam) {
							new AutoThread<TeamVS>(team -> {
								if (team.blueWon()) {
									if (isblue)
										win();
									else
										lose();
								} else {
									if (isblue)
										lose();
									else
										win();
								}
							}, TeamVS.class).start();
						} else {
							int ourid;
							int oppid;
							try {
								ourid = OsuApi.getUserId(ourpname);
								oppid = OsuApi.getUserId(opppname);
								if (ourid == -1 || oppid == -1) {
									throw new Exception();
								}
							} catch (Exception e) {
								Chat.send("Wrong username.");
								e.printStackTrace();
								return true;
							}
							
							ourname = ourpname;
							oppname = opppname;
							
							new AutoThread<H2H>(h2h -> {
								if (h2h.getWinner() == ourid) {
									win();
								} else if (h2h.getWinner() == oppid) {
									lose();
								}
							}, H2H.class).start();
						}
					} else {
						Chat.send("Now mods can add score by !win or !lose, but check if you have made some typo.");
					}
				} else {
					ongoing = true;
					Chat.send("Now mods can add score by !win or !lose");
				}
			} else {
				Chat.send("Match is not over yet.");
			}
		} else if (CmdHistory.checkAndPut(sp[0], event, CmdLevel.MOD, "!setinfo")) {
			desc = sp[1];
			Chat.send("Info is now set.");
		} else if (CmdHistory.checkAndPut(sp[0], event, CmdLevel.NORMAL, "!score")) {
			if (!ongoing)
				return true;

			Chat.send(getScore());
		} else if (CmdHistory.checkAndPut(sp[0], event, CmdLevel.MOD, "!win")) {
			if (!ongoing)
				return true;

			int n = 1;
			if (sp.length != 1) {
				String sc = sp[1].toLowerCase();
				if (sc.startsWith("set:")) {
					n = Integer.parseInt(sc.substring(4));
					oursetscore += n;
					ourscore = 0;
					oppscore = 0;
					n = -1;
				} else {
					n = Integer.parseInt(sc);
				}
			}
			for (int i = 0; i < n; i++) {
				win();
			}
			Chat.send("PogChamp " + getScore());
		} else if (CmdHistory.checkAndPut(sp[0], event, CmdLevel.MOD, "!lose")) {
			if (!ongoing)
				return true;

			int n = 1;
			if (sp.length != 1) {
				String sc = sp[1].toLowerCase();
				if (sc.startsWith("set:")) {
					n = Integer.parseInt(sc.substring(4));
					oppsetscore += n;
					ourscore = 0;
					oppscore = 0;
					n = -1;
				} else {
					n = Integer.parseInt(sc);
				}
			}
			for (int i = 0; i < n; i++) {
				lose();
			}
			Chat.send("Sadge " + getScore());
		} else if (CmdHistory.checkAndPut(sp[0], event, CmdLevel.MOD, "!over")) {
			if (!ongoing)
				return true;

			Chat.send("Match is over / " + getScore());
			reset();
		} else if (CmdHistory.checkAndPut(sp[0], event, CmdLevel.NORMAL, "!info")) {
			if (!ongoing)
				return true;

			Chat.send(event.getUser().getNick() + " -> " + desc);
		} else if (CmdHistory.checkAndPut(sp[0], event, CmdLevel.NORMAL, "!mp")) {
			if (!ongoing)
				return true;

			if (mp >= 0) {
				Chat.send("https://osu.ppy.sh/mp/" + mp);
			}
		} else if (CmdHistory.checkAndPut(sp[0], event, CmdLevel.MOD, "!reset")) {
			if (!ongoing)
				return true;

			if (sp.length != 1 && sp[1].equalsIgnoreCase("all")) {
				oursetscore = 0;
				oppsetscore = 0;
			}

			ourscore = 0;
			oppscore = 0;
			Chat.send(getScore());
		} else {
			return false;
		}

		return true;
	}

	private void lose() {
		oppscore++;
		if (set > 0 && oppscore >= set) {
			oppscore = 0;
			ourscore = 0;
			oppsetscore++;
		}
	}

	private void win() {
		ourscore++;
		if (set > 0 && ourscore >= set) {
			oppscore = 0;
			ourscore = 0;
			oursetscore++;
		}
	}

}

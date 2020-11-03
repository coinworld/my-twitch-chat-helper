package tv.twitch.hwsnemo.autoreply.cmd.impl;

import java.util.Calendar;

import tv.twitch.hwsnemo.autoreply.Main;
import tv.twitch.hwsnemo.autoreply.NotEnabledException;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;

public class TimeCmd implements Cmd {

	public TimeCmd() throws NotEnabledException {
		Main.throwOr("enabletimecmd");
	}

	private boolean timeset = false;
	private long eventtime;

	@Override
	public boolean go(CmdInfo inf) {
		if (inf.chkPut(CmdLevel.MOD, "!setcountdown")) {
			if (inf.getArg() != null) {
				String[] time = inf.getArg().split(":", 2);
				int hr = Integer.parseInt(time[0]);
				int min = Integer.parseInt(time[1]);

				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, hr);
				cal.set(Calendar.MINUTE, min);

				Calendar now = Calendar.getInstance();

				if (now.after(cal)) {
					cal.add(Calendar.DATE, 1);
				}

				eventtime = cal.getTimeInMillis();
				timeset = true;

				inf.send("Time is set.");
			} else {
				inf.send("Specify time.");
			}
		} else if (inf.chkPut(CmdLevel.NORMAL, "!cd", "!countdown")) {
			if (timeset) {
				long diff = eventtime - System.currentTimeMillis();

				if (diff <= 0) {
					inf.send("Event is over or has already started.");
					return true;
				}

				long diffmin = diff / (60 * 1000) % 60;
				long diffhr = diff / (60 * 60 * 1000); // % 24;

				String m = "Time Left: ";

				if (diffhr > 0) {
					m += diffhr + "h ";
				}

				m += diffmin + "m";

				inf.send(m);
			} else {
				inf.send("Time is not set.");
			}
		} else {
			return false;
		}
		return true;
	}

}

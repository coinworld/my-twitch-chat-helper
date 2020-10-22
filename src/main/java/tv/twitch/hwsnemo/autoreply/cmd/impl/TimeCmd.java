package tv.twitch.hwsnemo.autoreply.cmd.impl;

import java.util.Calendar;

import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.NotEnabledException;
import tv.twitch.hwsnemo.autoreply.cmd.Check;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;

public class TimeCmd implements Cmd {

	public TimeCmd() throws NotEnabledException {
		Check.throwOr("enabletimecmd");
	}

	private boolean timeset = false;
	private long eventtime;

	@Override
	public boolean go(String[] sp, MessageEvent event) {
		if (Check.andPut(sp[0], event, CmdLevel.MOD, "!setcountdown")) {
			if (sp.length != 1) {
				String[] time = sp[1].split(":", 2);
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

				Chat.send("Time is set.");
			} else {
				Chat.send("Specify time.");
			}
		} else if (Check.andPut(sp[0], event, CmdLevel.NORMAL, "!cd", "!countdown")) {
			if (timeset) {
				long diff = eventtime - System.currentTimeMillis();

				if (diff <= 0) {
					Chat.send("Event is over or has already started.");
					return true;
				}

				long diffmin = diff / (60 * 1000) % 60;
				long diffhr = diff / (60 * 60 * 1000); // % 24;

				String m = "Time Left: ";

				if (diffhr > 0) {
					m += diffhr + " hour(s) ";
				}

				m += diffmin + " min(s)";

				Chat.send(m);
			} else {
				Chat.send("Time is not set.");
			}
		} else {
			return false;
		}
		return true;
	}

}

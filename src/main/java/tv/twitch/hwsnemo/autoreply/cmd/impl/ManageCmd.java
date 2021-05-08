package tv.twitch.hwsnemo.autoreply.cmd.impl;

import tv.twitch.hwsnemo.autoreply.ChatLevel;
import tv.twitch.hwsnemo.autoreply.MainConfig;
import tv.twitch.hwsnemo.autoreply.base.BaseMap;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;
import tv.twitch.hwsnemo.autoreply.cmd.CmdMap;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;
import tv.twitch.hwsnemo.autoreply.suggest.SuggestMap;

public class ManageCmd implements Cmd {

	@Override
	public boolean go(CmdInfo inf) {
		BaseMap<?> map = null;
		if (inf.chkPut(ChatLevel.MOD, "!managecmd")) {
			map = CmdMap.getMap();
		} else if (inf.chkPut(ChatLevel.MOD, "!managesugg")) {
			map = SuggestMap.getMap();
		} else if (inf.chkPut(ChatLevel.MOD, "!chconf")) {
			String[] arg = inf.getArg().split(" ", 2);
			if (arg.length != 2) {
				inf.send("Invaild argument.");
				return true;
			}
			
			MainConfig.set(arg[0], arg[1]);
			inf.send("Config is temporarily updated.");
		}
		
		if (map != null) {
			String[] arg = inf.getArg().split(" ", 2);
			if (arg.length != 2) {
				inf.send("Invaild argument.");
				return true;
			}
			try {
				if (arg[0].equalsIgnoreCase("enable")) {
					map.enable(arg[1]);
					inf.send(arg[1] + " is now sucessfully enabled.");
				} else if (arg[0].equalsIgnoreCase("disable")) {
					map.disable(arg[1]);
					inf.send(arg[1] + " is now sucessfully disabled.");
				} else if (arg[0].equalsIgnoreCase("toggleinstant")) {
					if (map instanceof SuggestMap) {
						((SuggestMap) map).setInstant(arg[1], !((SuggestMap) map).getInstant(arg[1]));
						inf.send("Instant action state of " + arg[1] + "is now toggled.");
					} else {
						inf.send("This action is only available on suggest management.");
					}
				} else {
					inf.send("There isn't any appropriate action.");
				}
			} catch (SendableException e) {
				inf.send(e.getMessage());
			}
			return true;
		}
		return false;
	}

}

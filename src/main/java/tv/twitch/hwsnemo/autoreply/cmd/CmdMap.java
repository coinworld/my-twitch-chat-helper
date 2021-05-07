package tv.twitch.hwsnemo.autoreply.cmd;

import tv.twitch.hwsnemo.autoreply.base.BaseMap;
import tv.twitch.hwsnemo.autoreply.base.Construction;
import tv.twitch.hwsnemo.autoreply.cmd.impl.*;

public class CmdMap extends BaseMap<CmdEntry> {
	
	private CmdMap() {
		put("match", MatchCmd::new);
		put("misc", MiscCmd::new);
		put("np", NpCmd::new);
		put("time", TimeCmd::new);
		put("debug", DebugCmd::new);
		put("manage", ManageCmd::new);
	}
	
	private static volatile CmdMap me = new CmdMap();
	
	public static CmdMap getMap() {
		return me;
	}
	
	public void put(String name, Construction<Cmd> cons) {
		map.put(name, new CmdEntry(cons));
	}
}

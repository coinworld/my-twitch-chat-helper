package tv.twitch.hwsnemo.autoreply.cmd;

import tv.twitch.hwsnemo.autoreply.base.BaseProcessor;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;

public class CmdProcessor extends BaseProcessor<CmdMap, CmdInfo, CmdEntry> {
	
	public CmdProcessor() throws SendableException {
		super(CmdMap.getMap());
		if (!map.isEnabled("manage")) {
			map.enable("manage");
		}
	}

	@Override
	protected boolean pass(CmdEntry ent, CmdInfo arg) {
		return ent.getInstance().go(arg);
	}
}

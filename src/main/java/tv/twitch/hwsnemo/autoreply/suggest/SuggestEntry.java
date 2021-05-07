package tv.twitch.hwsnemo.autoreply.suggest;

import tv.twitch.hwsnemo.autoreply.base.BaseEntry;
import tv.twitch.hwsnemo.autoreply.base.Construction;

public class SuggestEntry extends BaseEntry<Suggest> {
	private boolean instant = false;
	
	public SuggestEntry(Construction<Suggest> construct) {
		super(construct);
	}
	
	public void setInstant(boolean instant) {
		this.instant = instant;
	}
	
	public boolean isInstant() {
		return instant;
	}
}

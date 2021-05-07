package tv.twitch.hwsnemo.autoreply.suggest;

import tv.twitch.hwsnemo.autoreply.base.BaseMap;
import tv.twitch.hwsnemo.autoreply.base.Construction;
import tv.twitch.hwsnemo.autoreply.suggest.impl.*;

import tv.twitch.hwsnemo.autoreply.osu.SendableException;

public class SuggestMap extends BaseMap<SuggestEntry> {
	private SuggestMap() {
		put("linkdetect", LinkDetector::new);
		put("predict", PredictAnswer::new);
	}
	
	private static volatile SuggestMap me = new SuggestMap();
	
	public static SuggestMap getMap() {
		return me;
	}
	
	public void put(String name, Construction<Suggest> cons) {
		map.put(name, new SuggestEntry(cons));
	}
	
	public void setInstant(String name, boolean instant) throws SendableException {
		check(name);
		
		map.get(name).setInstant(instant);
	}
	
	public boolean getInstant(String name) throws SendableException {
		check(name);
		
		return map.get(name).isInstant();
	}
}

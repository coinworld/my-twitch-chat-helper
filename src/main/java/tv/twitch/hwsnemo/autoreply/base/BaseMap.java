package tv.twitch.hwsnemo.autoreply.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import tv.twitch.hwsnemo.autoreply.osu.SendableException;

public abstract class BaseMap<T extends BaseEntry<?>> {
	protected final Map<String, T> map = new HashMap<>();
	
	public void enable(String name) throws SendableException {
		check(name);
		
		try {
			map.get(name).enable();
		} catch (Exception e) {
			throw new SendableException("An error occured while enabling, check !debug", e.getMessage());
		}
	}
	
	public void disable(String name) throws SendableException {
		check(name);
		
		BaseEntry<?> c = map.get(name);
		if (c.isEnabled()) {
			c.disable();
		} else {
			throw new SendableException("This command set is already disabled.");
		}
	}
	
	public boolean isEnabled(String name) throws SendableException {
		check(name);
		
		return map.get(name).isEnabled();
	}
	
	protected void check(String name) throws SendableException {
		if (!map.containsKey(name)) {
			throw new SendableException("The name you typed in isn't vaild.");
		}
	}
	
	public Collection<T> getEntries() {
		return map.values();
	}
}

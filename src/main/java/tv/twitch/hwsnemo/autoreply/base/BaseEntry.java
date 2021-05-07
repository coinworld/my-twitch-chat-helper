package tv.twitch.hwsnemo.autoreply.base;

public abstract class BaseEntry<T> {
	private final Construction<T> construct;
	private T t = null;
	
	public BaseEntry(Construction<T> construct) {
		this.construct = construct;
	}
	
	public void enable() throws Exception {
		t = construct.construct();
	}
	
	public void disable() {
		t = null;
	}
	
	public boolean isEnabled() {
		return t != null;
	}
	
	public T getInstance() {
		return t;
	}
}

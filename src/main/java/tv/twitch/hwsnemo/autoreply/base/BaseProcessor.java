package tv.twitch.hwsnemo.autoreply.base;

public abstract class BaseProcessor<T extends BaseMap<A>, V, A extends BaseEntry<?>> {
	protected final T map;
	protected BaseProcessor(T map) {
		this.map = map;
	}
	
	public boolean loop(V arg) {
		for (A ent : map.getEntries()) {
			if (ent.isEnabled()) {
				if (pass(ent, arg)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected abstract boolean pass(A ent, V arg);
}

package tv.twitch.hwsnemo.autoreply.base;

public abstract class BaseProcessor<T extends BaseMap<?>, V, A extends BaseEntry<?>> {
	protected final T map;
	protected BaseProcessor(T map) {
		this.map = map;
	}
	
	@SuppressWarnings("unchecked")
	public boolean loop(V arg) {
		for (BaseEntry<?> ent : map.getEntries()) {
			if (ent.isEnabled()) {
				if (pass((A) ent, arg)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected abstract boolean pass(A ent, V arg);
}

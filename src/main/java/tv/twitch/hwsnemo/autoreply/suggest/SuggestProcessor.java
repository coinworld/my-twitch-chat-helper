package tv.twitch.hwsnemo.autoreply.suggest;

import tv.twitch.hwsnemo.autoreply.base.BaseProcessor;

public class SuggestProcessor extends BaseProcessor<SuggestMap, SuggestInfo, SuggestEntry> {

	public SuggestProcessor() {
		super(SuggestMap.getMap());
	}

	@Override
	protected boolean pass(SuggestEntry ent, SuggestInfo arg) {
		SuggestAction sa = ent.getInstance().hit(arg);
		if (sa == null)
			return false;
		
		if (ent.isInstant()) {
			sa.run();
			return false;
		} else {
			put(sa);
			return true;
		}
	}
	
	private static volatile SuggestAction act = null;
	
	public static void run() {
		if (act != null) act.run();
		act = null;
	}
	
	public static void put(SuggestAction act) {
		SuggestProcessor.act = act;
	}
	
	public static String reason() {
		if (act == null)
			return "invaild";
		return act.reason();
	}
	
	public static boolean isEmpty() {
		return act == null;
	}

}

package tv.twitch.hwsnemo.autoreply;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.DebugCmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.MatchCmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.MiscCmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.NpCmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.TimeCmd;
import tv.twitch.hwsnemo.autoreply.suggest.Suggest;

public class DefaultConstructors<T> {
	private Map<String, Construction<T>> cons = new HashMap<>();
	
	private final String listtypekey;
	private final String listkey;
	
	public DefaultConstructors(String listtypekey, String listkey) {
		this.listtypekey = listtypekey;
		this.listkey = listkey;
	}
	
	public static List<Cmd> createCmds() {
		DefaultConstructors<Cmd> dc = new DefaultConstructors<>("cmdlisttype", "cmdlist");
		dc.cons.put("match", MatchCmd::new);
		dc.cons.put("misc", MiscCmd::new);
		dc.cons.put("np", NpCmd::new);
		dc.cons.put("time", TimeCmd::new);
		dc.cons.put("debug", DebugCmd::new);
		return dc.create();
	}
	
	public static List<Suggest> createSuggs() {
		DefaultConstructors<Suggest> dc = new DefaultConstructors<>("suggestlisttype", "suggestlist");
		return dc.create();
	}

	private static <I> List<I> get(Set<String> set, ListType status, Map<String, Construction<I>> map) {
		List<I> list = new ArrayList<>();
		for (String str : map.keySet()) {
			boolean enable = status == ListType.NO_LIST ? true
					: status == ListType.WHITELIST ? set.contains(str) : !set.contains(str);
			if (enable) {
				try {
					list.add(map.get(str).construct());
				} catch (NotEnabledException e) {

				} catch (Exception e) {
					Main.write("Error while creating cmd instances: " + e.getMessage());
				}
			}
		}
		return list;
	}

	private static enum ListType {
		NO_LIST, BLACKLIST, WHITELIST;

		static ListType get(String str) {
			ListType ls = NO_LIST;
			if (str.equals("blacklist")) {
				ls = BLACKLIST;
			} else if (str.equals("whitelist")) {
				ls = WHITELIST;
			}
			return ls;
		}
	}

	public List<T> create() {
		Set<String> set = new HashSet<>(Arrays.asList(MainConfig.getString(listkey, "").split(",")));
		ListType ls = ListType.get(MainConfig.getString(listtypekey, ""));
		return get(set, ls, cons);
	}
}

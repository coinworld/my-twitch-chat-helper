package tv.twitch.hwsnemo.autoreply.suggest.impl;

import java.util.List;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.Main;
import tv.twitch.hwsnemo.autoreply.NotEnabledException;
import tv.twitch.hwsnemo.autoreply.suggest.Suggest;
import tv.twitch.hwsnemo.autoreply.suggest.SuggestAction;

public class LanguageDetect implements Suggest {

	public class LangSuggestAct implements SuggestAction {

		private final String suspect;
		private final String reason;

		private LangSuggestAct(String suspect, String reason) {
			this.suspect = suspect;
			this.reason = reason;
		}

		@Override
		public String reason() {
			return reason;
		}

		@Override
		public void run() {
			Chat.send("/timeout " + suspect + " 100");
			Chat.send(suspect + " -> Keep chat in English or Korean");
		}

	}

	private static List<LanguageProfile> lp;
	private static LanguageDetector ld;
	private static TextObjectFactory fac = CommonTextObjectFactories.forDetectingOnLargeText();

	public LanguageDetect() throws NotEnabledException {
		Main.throwOr("enablelangdetect");

		try {
			lp = new LanguageProfileReader().readAllBuiltIn();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		ld = LanguageDetectorBuilder.create(NgramExtractors.standard()).withProfiles(lp).build();
	}

	@Override
	public SuggestAction hit(String name, String msg) {
		String reason = null;
		TextObject to = fac.forText(msg);
		Optional<LdLocale> o = ld.detect(to);

		if (o.isPresent()) {
			String lang = o.get().getLanguage();
			if (!(lang.equals("en") || lang.equals("ko"))) {
				reason = "lang: " + lang;
			}
		}

		if (reason == null) {
			int loop = 5;
			int len = msg.length();
			if (len < 5) {
				loop = msg.length();
			}
			for (int i = 0; i < loop; i++) {
				int b = msg.codePointAt(i);
				if (!((b >= 0x0020 && b <= 0x00BF) || (b >= 0x1100 && b <= 0x11FF) || (b >= 0xAC00 && b <= 0xD7AF)
						|| (b >= 0x3000 && b <= 0x303F) || (b >= 0x3130 && b <= 0x318F) || (b >= 0xFF00 && b <= 0xFF65)
						|| (b >= 0xFFA1 && b <= 0xFFEF) || (b >= 0x1F000 && b <= 0x1FBFF))) {
					reason = "illegal character: " + new String(Character.toChars(b));
				}
				// i += Character.charCount(b);
			}
		}

		// for (int i = 0; i < Character.codePointCount(msg, 0, msg.length());)
		if (reason == null)
			return null;

		return new LangSuggestAct(name, reason);
	}

}

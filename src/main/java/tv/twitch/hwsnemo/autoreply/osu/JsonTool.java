package tv.twitch.hwsnemo.autoreply.osu;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonTool {
	private final JsonParser jp;
	private JsonToken tk;

	public JsonTool(JsonParser jp) {
		this.jp = jp;
	}

	public static interface ThrowableRunnable {
		public void run() throws Exception;
	}

	public boolean isNull() {
		return tk == JsonToken.VALUE_NULL;
	}

	public boolean isObjectStart(String name) {
		return tk == JsonToken.START_OBJECT && equalName(name);
	}

	public void loopInObject(String name, ThrowableRunnable run) throws Exception {
		tk = jp.nextValue();
		while (!(tk == JsonToken.END_OBJECT && equalName(name))) {
			run.run();
			tk = jp.nextValue();
		}
	}

	public boolean isArrayStart(String name) {
		return tk == JsonToken.START_ARRAY && equalName(name);
	}

	public void loopInArray(String name, ThrowableRunnable run) throws Exception {
		tk = jp.nextValue();
		while (!(tk == JsonToken.END_ARRAY && equalName(name))) {
			run.run();
			tk = jp.nextValue();
		}
	}

	public void loopUntilEnd(ThrowableRunnable run) throws Exception {
		tk = jp.nextValue();
		while (tk != null) {
			run.run();
			tk = jp.nextValue();
		}
	}

	public JsonToken token() {
		return tk;
	}

	public boolean equalName(String name) {
		try {
			return (name == null) ? jp.getCurrentName() == null : name.equals(jp.getCurrentName());
		} catch (IOException e) {
			return false;
		}
	}

	public int getInt() {
		try {
			return Integer.parseInt(jp.getText());
		} catch (Exception e) {
			return -1;
		}
	}

	public float getFloat() {
		try {
			return Float.parseFloat(jp.getText());
		} catch (Exception e) {
			return -1;
		}
	}
	
	private String defStr = null;

	public String getText() {
		try {
			return jp.getText();
		} catch (Exception e) {	
			return defStr;
		}
	}
	
	public void setDefaultString(String str) {
		defStr = str;
	}

}

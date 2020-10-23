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
		try {
			boolean na = (name == null) ? jp.getCurrentName() == null : name.equals(jp.getCurrentName());
			return tk == JsonToken.START_OBJECT && na;
		} catch (IOException e) {
			return false;
		}
	}

	public void loopInObject(String name, ThrowableRunnable run) throws Exception {
		tk = jp.nextValue();
		while (!(tk == JsonToken.END_OBJECT
				&& ((name == null) ? jp.getCurrentName() == null : name.equals(jp.getCurrentName())))) {
			run.run();
			tk = jp.nextValue();
		}
	}

	public boolean isArrayStart(String name) {
		try {
			boolean na = (name == null) ? jp.getCurrentName() == null : name.equals(jp.getCurrentName());
			return tk == JsonToken.START_ARRAY && na;
		} catch (Exception e) {
			return false;
		}
	}

	public void loopInArray(String name, ThrowableRunnable run) throws Exception {
		tk = jp.nextValue();
		while (!(tk == JsonToken.END_ARRAY
				&& ((name == null) ? jp.getCurrentName() == null : name.equals(jp.getCurrentName())))) {
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
			return name.equals(jp.getCurrentName());
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

	public String getText() {
		try {
			return jp.getText();
		} catch (Exception e) {
			return null;
		}
	}

}

package tv.twitch.hwsnemo.autoreply.osu;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import javax.swing.*;

import tv.twitch.hwsnemo.autoreply.Main;

public class TextWindow {

	private final JFrame f;
	private final JLabel l;

	public TextWindow(String title, String text) {
		String font = "Serif";
		int size = 30;
		String backcolor = "white";
		String labelcolor = "black";

		Map<String, String> m = Main.getConfig();

		if (m.containsKey("font"))
			font = m.get("font");

		if (m.containsKey("fontsize"))
			size = Integer.parseInt(m.get("fontsize"));

		if (m.containsKey("backcolor"))
			backcolor = m.get("backcolor");

		if (m.containsKey("labelcolor"))
			labelcolor = m.get("labelcolor");

		f = new JFrame(title);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.setSize(300, 100);
		f.getContentPane().setBackground(getColor(backcolor.toLowerCase(), Color.white));
		l = new JLabel(text, SwingConstants.CENTER);
		l.setFont(new Font(font, Font.BOLD, size));
		l.setForeground(getColor(labelcolor.toLowerCase(), Color.black));
		f.add(l);
		f.setVisible(true);
	}

	public void setText(String text) {
		l.setText(text);
	}

	public void close() {
		f.setVisible(false);
		f.dispose();
	}

	private static Color getColor(String col, Color def) {
		Color color = def;
		switch (col.toLowerCase()) {
		case "black":
			color = Color.BLACK;
			break;
		case "blue":
			color = Color.BLUE;
			break;
		case "cyan":
			color = Color.CYAN;
			break;
		case "darkgray":
			color = Color.DARK_GRAY;
			break;
		case "gray":
			color = Color.GRAY;
			break;
		case "green":
			color = Color.GREEN;
			break;
		case "yellow":
			color = Color.YELLOW;
			break;
		case "lightgray":
			color = Color.LIGHT_GRAY;
			break;
		case "magneta":
			color = Color.MAGENTA;
			break;
		case "orange":
			color = Color.ORANGE;
			break;
		case "pink":
			color = Color.PINK;
			break;
		case "red":
			color = Color.RED;
			break;
		case "white":
			color = Color.WHITE;
			break;
		}
		return color;
	}
}

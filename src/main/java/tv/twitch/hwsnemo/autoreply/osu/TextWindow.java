package tv.twitch.hwsnemo.autoreply.osu;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import tv.twitch.hwsnemo.autoreply.MainConfig;

public class TextWindow {

	private final JFrame f;
	private final JLabel l;

	public TextWindow(String title, String text) {
		String font = MainConfig.getString("font", "Serif");
		int size = MainConfig.getNum("fontsize", 30);
		String backcolor = MainConfig.getString("backcolor", "white");
		String labelcolor = MainConfig.getString("labelcolor", "black");
		int width = MainConfig.getNum("width", 300);
		int height = MainConfig.getNum("height", 100);

		f = new JFrame(title);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.setSize(width, height);
		f.getContentPane().setBackground(getColor(backcolor.toLowerCase(), Color.white));
		l = new JLabel(text, SwingConstants.CENTER);
		l.setFont(new Font(font, Font.BOLD, size));
		l.setForeground(getColor(labelcolor.toLowerCase(), Color.black));
		f.add(l);
		f.setVisible(true);
	}

	public synchronized void setText(String text) {
		l.setText(text);
	}

	public void close() {
		f.setVisible(false);
		f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
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

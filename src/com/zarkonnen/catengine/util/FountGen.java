package com.zarkonnen.catengine.util;

import com.zarkonnen.catengine.Fount;
import com.zarkonnen.catengine.Img;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;

public class FountGen {
	public static BufferedImage render(Font font, Fount fount, boolean pretty) {
		BufferedImage outImg = new BufferedImage(fount.imgSize, fount.imgSize, pretty ? Transparency.TRANSLUCENT : Transparency.BITMASK);
		Graphics2D g = outImg.createGraphics();
		if (pretty) {
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		g.setColor(Color.WHITE);
		g.setFont(font);
		int maxAscent = g.getFontMetrics().getMaxAscent();
		for (char c : fount.alphabet.toCharArray()) {
			Img img = fount.get(c);
			g.drawString(new String(new char[] {c}), img.srcX, img.srcY + g.getFontMetrics().getMaxAscent());
		}
		return outImg;
	}
	
	public static void main(String[] args)throws Exception {
		if (args.length != 8) {
			System.out.println("Params: alphabetFile font [plain|bold|italic] size imgWidth imgHeight pretty targetFile");
			return;
		}
		
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
		String alphabet = r.readLine();
		r.close();
		
		Font font = new Font(args[1],
				args[2].equals("italic")
					? Font.ITALIC
					: args[2].equals("bold")
						? Font.BOLD
						: Font.PLAIN,
				Integer.parseInt(args[3]));
		
		Fount fount = new Fount("?", Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[4]), Integer.parseInt(args[5]) * 2, alphabet);
		
		BufferedImage img = render(font, fount, args[6].equals("true") || args[6].equals("1"));
		ImageIO.write(img, "PNG", new File(args[7]));
	}
}

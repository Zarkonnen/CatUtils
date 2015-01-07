package com.zarkonnen.catengine.util;

import com.zarkonnen.catengine.Fount;
import com.zarkonnen.catengine.Img;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.imageio.ImageIO;

public class FountGen {
	public static void main(String[] args)throws Exception {
		if (args.length != 8) {
			System.out.println("Params: alphabetFile font [plain|bold|italic] size pretty imgSize targetPNGFile targetLayoutFile");
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
						
		boolean pretty = args[4].equals("true") || args[4].equals("1");
		int imgSize = Integer.parseInt(args[5]);
		
		BufferedImage outImg = new BufferedImage(imgSize, imgSize, pretty ? Transparency.TRANSLUCENT : Transparency.BITMASK);
		Graphics2D g = outImg.createGraphics();
		if (pretty) {
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		
		g.setColor(Color.WHITE);
		g.setFont(font);
		
		int x = 1;
		int y = 1;
		
		FontMetrics fm = g.getFontMetrics();
		int maxAscent = fm.getMaxAscent();
		int maxH = fm.getHeight();
		
		StringBuilder info = new StringBuilder();
		
		for (char c : alphabet.toCharArray()) {
			String charS = new String(new char[] {c});
			int charW = fm.stringWidth(charS);
			if (x + charW > imgSize) {
				x = 1;
				y += maxH + 1;
			}
			g.drawString(charS, x, y + maxAscent);
			info.append(charS).append("\n");
			info.append(x).append(" ").append(y).append(" ").append(charW).append(" ").append(maxH).append("\n");
			x += charW + 1;
		}
		
		ImageIO.write(outImg, "PNG", new File(args[6]));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(args[7])), "UTF-8"));
		bw.write(info.toString());
		bw.flush();
		bw.close();
	}
}

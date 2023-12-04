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
import java.util.HashMap;
import javax.imageio.ImageIO;

public class FountGen {
	public static void main(String[] args)throws Exception {
		if (args.length != 9 && args.length != 13 && args.length != 15) {
			System.out.println("Params: alphabetFile font (plain|bold|italic) size pretty imgSize targetPNGFile targetLayoutFile yOffset[,extraLineH] [secondaryAlphabet font (plain|bold|italic) size [leftExtra rightExtra]]");
			return;
		}
				
		int leftExtra = args.length == 15 ? Integer.parseInt(args[12]) : 0;
		int rightExtra = args.length == 15 ? Integer.parseInt(args[13]) : 0;
		
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
		
		FontMetrics fm = g.getFontMetrics(font);
		
		int yOffset = 0;
		int maxH = fm.getHeight();
		int extraLineH = 0;
		if (args[8].contains(",")) {
			yOffset = Integer.parseInt(args[8].split(",")[0]);
			extraLineH = Integer.parseInt(args[8].split(",")[1]);
		} else {
			yOffset = Integer.parseInt(args[8]);
		}
		
		//System.out.println(font + " " + args[3] + ": yOffset " + yOffset + " extraLineH " + extraLineH);
		
		int x = 1 + leftExtra;
		int y = 1;
		
		int maxAscent = fm.getMaxAscent();
		
		
		Font font2 = font;
		FontMetrics fm2 = fm;
		int maxAscent2 = maxAscent;
		String alphabet2 = "";
		HashMap<String, String> remaps = new HashMap<String, String>();
		
		if (args.length == 13 || args.length == 15) {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(args[9]), "UTF-8"));
			alphabet2 = r.readLine();			
			
			font2 = new Font(args[10],
				args[10].equals("italic")
					? Font.ITALIC
					: args[11].equals("bold")
						? Font.BOLD
						: Font.PLAIN,
				Integer.parseInt(args[12]));
			g.setFont(font2);
			fm2 = g.getFontMetrics(font2);
			maxAscent2 = fm2.getMaxAscent();
			
			String l = null;
			while ((l = r.readLine()) != null) {
				if (l.startsWith("remap ")) {
					String from = l.substring(6, 7);
					String to = l.substring(7, 8);
					remaps.put(from, to);
				}
			}
			r.close();
		}
		
		StringBuilder info = new StringBuilder();
		if (leftExtra != 0) {
			info.append("letterXOffset ").append(-leftExtra).append("\n");
		}
		if (rightExtra != 0) {
			info.append("letterSpacing ").append(-(leftExtra + rightExtra)).append("\n");
		}
		
		for (char c : alphabet.toCharArray()) {
			String charS = new String(new char[] {c});
			String drawS = charS;
			if (remaps.containsKey(charS)) {
				drawS = remaps.get(charS);
			}
			
			if (alphabet2.contains(charS)) {
				g.setFont(font2);
				int charW = fm2.stringWidth(drawS);
				if (x + charW + rightExtra > imgSize) {
					x = 1 + leftExtra;
					y += maxH + 1 + yOffset + extraLineH;
				}
				g.drawString(drawS, x, y + maxAscent + yOffset);
				info.append(charS).append("\n");
				info.append(x - leftExtra).append(" ").append(y).append(" ").append(charW + leftExtra + rightExtra).append(" ").append(maxH + yOffset).append("\n");
				x += charW + 1 + rightExtra + leftExtra;
			} else {
				g.setFont(font);
				int charW = fm.stringWidth(drawS);
				if (x + charW + rightExtra > imgSize) {
					x = 1 + leftExtra;
					y += maxH + 1 + yOffset + extraLineH;
				}
				g.drawString(drawS, x, y + maxAscent + yOffset);
				info.append(charS).append("\n");
				info.append(x - leftExtra).append(" ").append(y).append(" ").append(charW + leftExtra + rightExtra).append(" ").append(maxH + yOffset).append("\n");
				x += charW + 1 + rightExtra + leftExtra;
			}
		}
		
		ImageIO.write(outImg, "PNG", new File(args[6]));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(args[7])), "UTF-8"));
		bw.write(info.toString());
		bw.flush();
		bw.close();
	}
}

package com.zarkonnen.catengine.util;

import com.zarkonnen.catengine.Img;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import javax.imageio.*;
import java.io.File;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class SpriteGen {
	public static void main(String[] args) {
		try {
			if (args.length < 5) {
				System.out.println("The following arguments are required:\n" +
					" spinName\n" + 
					" numFrames\n" +
					" spinFolder\n" +
					" spinFilePrefix\n" +
					" maskFolder\n" +
					" maskFilePrefix\n" +
					" resultImgFile\n" +
					" resultMapFie\n");
				return;
			}
		
			String spinName = args[0];
			int numFrames = Integer.parseInt(args[1]);
			File spinFolder = new File(args[2]);
			String spinFilePrefix = args[3];
			File maskFolder = new File(args[4]);
			String maskFilePrefix = args[5];
			File resultImgFile = new File(args[6]);
			File resultMapFile = new File(args[7]);
			
			HashMap<String, Img> mapping = new HashMap<String, Img>();
			BufferedImage resultImg = null;
			
			int cols = 0;
			int gridSize = 0;
			
			for (int i = 0; i < numFrames; i++) {
				String number = i < 10 ? ("000" + i) : ("00" + i);
				File spinFile = new File(spinFolder, spinFilePrefix + number + ".png");
				File maskFile = new File(maskFolder, maskFilePrefix + number + ".png");
			
				BufferedImage spinImg = ImageIO.read(spinFile);
				BufferedImage maskImg = ImageIO.read(maskFile);
				
				if (resultImg == null) {
					cols = (int) Math.ceil(Math.sqrt(numFrames));
					gridSize = spinImg.getWidth() + 1;
					int imgSize = 32;
					while (gridSize * cols > imgSize) {
						imgSize *= 2;
					}
					resultImg = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
				}
				
				int left = (i % cols) * gridSize;
				int top = (i / cols) * gridSize;
				
				mapping.put(spinName + i, new Img(resultImgFile.getName().split("[.]")[0], left, top, gridSize - 1, gridSize - 1, false));
				
				for (int y = 0; y < spinImg.getHeight(); y++) {
					for (int x = 0; x < spinImg.getWidth(); x++) {
						Color c = new Color(spinImg.getRGB(x, y));
						Color maskC = new Color(maskImg.getRGB(x, y));
						Color maskedColor = new Color(c.getRed(), c.getGreen(), c.getBlue(),
							maskC.getRed());
						resultImg.setRGB(left + x, top + y, maskedColor.getRGB());
					}
				}
			}
			
			ImageIO.write(resultImg, "PNG", resultImgFile);
			
			PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultMapFile), "UTF-8"));
			w.println(mapping.size());
			for (Map.Entry<String, Img> m : mapping.entrySet()) {
				w.println(m.getKey());
				w.println(m.getValue().src);
				w.println(m.getValue().srcX);
				w.println(m.getValue().srcY);
				w.println(m.getValue().srcWidth);
				w.println(m.getValue().srcHeight);
			}
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
package com.zarkonnen.catengine.util;

import com.zarkonnen.catengine.Img;
import com.zarkonnen.catengine.util.Utils.Pair;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class GridSpritePacker {
	public static Pair<BufferedImage, HashMap<String, Img>> gridPack(String resultName, HashMap<String, BufferedImage> images) {
		int side = (int) Math.ceil(Math.sqrt(images.size()));
		BufferedImage example = images.values().iterator().next();
		int imgW = example.getWidth();
		int sideW = imgW * side;
		int texSize = 32;
		while (texSize < sideW) {
			texSize *= 2;
		}
		
		BufferedImage out = new BufferedImage(texSize, texSize, Transparency.TRANSLUCENT);
		HashMap<String, Img> imgs = new HashMap<String, Img>();
		Graphics2D g = out.createGraphics();
		
		int col = 0;
		int row = 0;
		for (Map.Entry<String, BufferedImage> image : images.entrySet()) {
			g.drawImage(image.getValue(), col * imgW, row * imgW, null);
			imgs.put(image.getKey(), new Img(resultName, col * imgW, row * imgW, imgW, imgW, false));
			col++;
			if (col == side) {
				row++;
				col = 0;
			}
		}
		
		return new Pair(out, imgs);
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println("Usage: srcDir imgFile mapFile");
			return;
		}
		File srcDir = new File(args[0]);
		File imgF = new File(args[1]);
		File mapF = new File(args[2]);
		
		HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
		for (File f : srcDir.listFiles()) {
			if (!f.getName().endsWith(".png")) { continue; }
			images.put(f.getName().split("[.]")[0], ImageIO.read(f));
		}
		
		Pair<BufferedImage, HashMap<String, Img>> p = gridPack(imgF.getName().split("[.]")[0], images);
		ImageIO.write(p.a, "PNG", imgF);
		PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(mapF), "UTF-8"));
		w.println(p.b.size());
		for (Map.Entry<String, Img> m : p.b.entrySet()) {
			w.println(m.getKey());
			w.println(m.getValue().src);
			w.println(m.getValue().srcX);
			w.println(m.getValue().srcY);
			w.println(m.getValue().srcWidth);
			w.println(m.getValue().srcHeight);
		}
		w.close();
	}
}

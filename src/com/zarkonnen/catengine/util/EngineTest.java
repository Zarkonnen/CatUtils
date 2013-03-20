package com.zarkonnen.catengine.util;

import com.zarkonnen.catengine.Condition;
import com.zarkonnen.catengine.Draw;
import com.zarkonnen.catengine.Engine;
import com.zarkonnen.catengine.EngineFactory;
import com.zarkonnen.catengine.Fount;
import com.zarkonnen.catengine.Frame;
import com.zarkonnen.catengine.Game;
import com.zarkonnen.catengine.Hook;
import com.zarkonnen.catengine.Hooks;
import com.zarkonnen.catengine.Input;
import static com.zarkonnen.catengine.util.Utils.*;
import javax.swing.JOptionPane;

public class EngineTest {
	public static void main(String[] args) {
		int choice = JOptionPane.showOptionDialog(null, "Choose an implementation.", "CatEngine!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, EngineFactory.defaultEngines(), EngineFactory.defaultEngines()[0]);
		if (choice == -1) { return; }
		Engine e = null;
		try {
			e = EngineFactory.make(EngineFactory.defaultEngines()[choice], "CatEngine!", "/com/zarkonnen/catengine/images/", "/com/zarkonnen/catengine/sounds/", 30);
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		e.setup(new Game() {
			Pt cursor = new Pt(0, 0);
			int i = 0;
			boolean catness = false;
			int oodles = 0;
			int cooldown = 0;
			Hooks hooks = new Hooks();
			@Override
			public void input(Input in) {
				cursor = in.cursor();
				i++;
				catness = false;
				if (cooldown > 0) {
					cooldown--;
					return;
				}
				if (in.keyDown("1")) {
					for (ScreenMode m : in.modes()) {
						if (!m.fullscreen) {
							in.setMode(m);
							break;
						}
					}
				}
				if (in.keyDown("2")) {
					int sz = 0;
					ScreenMode best = null;
					for (ScreenMode m : in.modes()) {
						if (m.fullscreen && m.width * m.height > sz) {
							best = m;
							sz = m.width * m.height;
						}
					}
					if (best != null) {
						in.setMode(best);
					}
				}
				if (in.keyDown("3")) {
					if (in.modes().contains(new ScreenMode(800, 600, true))) {
						in.setMode(new ScreenMode(800, 600, true));
					}
				}
				if (in.keyDown("Q")) { in.quit(); return; }
				if (in.keyDown("C")) {
					in.setCursorVisible(!in.isCursorVisible());
					cooldown = 10;
				}
				if (in.keyDown("O")) {
					oodles = oodles == 0 ? 10 : oodles * 10;
					cooldown = 10;
				}
				hooks.hit(in);
			}

			@Override
			public void render(Frame f) {
				ScreenMode sm = f.mode();
				Draw d = new Draw(f);
				if (catness) {
					d.blit("cat.jpg", null, 0, 0, sm.width, sm.height);
				} else {
					d.rect(Clr.BLACK, 0, 0, sm.width, sm.height);
				}
				d.rect(Clr.RED, cursor.x, cursor.y, 10, 10, (Math.PI * 2 * i) / 100);
				d.blit("cat.jpg", new Clr(0, 255, 0, i % 255), (i * 3) % sm.width, (i * 10) % sm.height, 0, 0, (Math.PI * 2 * i) / 1000);
				for (int j = 0; j < oodles; j++) {
					d.blit("nebula.png", null, (i * 3 + j * 10) % sm.width, (i * 10 + j * 3) % sm.height);
				}
				String symbols = " qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890-=+_!?<>,.;:\"'@£$%^&*()[]{}|\\~/±";
				
				Fount fo = new Fount("Courier12", 10, 15, 8, 19, symbols);
				d.text("Welcome to CatEngine!\n[RED]Meow.", fo, 100, 100);
				fo = new Fount("LiberationMono18", 14, 24, 12, 26, symbols);
				String green = "" + (i / 10) % 10;
				String green2 = "" + i % 10;
				Hook hook = new Hook(Hook.Type.HOVER) {
					@Override
					public void run(Input in, Pt p, Hook.Type type) {
						catness = true;
					}
				};
				d.blit("LiberationMono18", 0, 0);
				d.text("Welcome to CatEngine!\n[33" + green + green2 + "00]Meow.", fo, 100.0, 300.0, m(p("Meow.", hook)));
				d.text("(Try moving your cursor over that \"Meow.\")", fo, 100, 400);
				d.text(f.fps() + " FPS", fo, 10, 10);
				if (oodles > 0) {
					d.text("[bg=BLACK][WHITE]oodles=" + oodles, fo, 10, 30);
				}
				hooks = d.getHooks();
			}
		});
		e.runUntil(Condition.ALWAYS);
		e.destroy();
	}
}

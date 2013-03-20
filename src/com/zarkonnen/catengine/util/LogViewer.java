package com.zarkonnen.catengine.util;

import com.zarkonnen.catengine.util.SpikeProfiler.Entry;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

public class LogViewer extends JFrame {
	static Preferences prefs = Preferences.userNodeForPackage(LogViewer.class);
	
	public static void main(String[] args) {
		File[] logFs = null;
		if (args.length > 0) {
			logFs = new File[args.length];
			for (int i = 0; i < logFs.length; i++) {
				logFs[i] = new File(args[i]);
			}
		}
		open(logFs);
	}
	
	static void open(File[] logFs) {
		try {
			if (logFs == null || !logFs[0].exists()) {
				JFileChooser jfc = new JFileChooser();
				jfc.setCurrentDirectory(new File(prefs.get("currentDir", "")));
				jfc.setMultiSelectionEnabled(true);
				jfc.showOpenDialog(null);
				logFs = jfc.getSelectedFiles();
				prefs.put("currentDir", jfc.getCurrentDirectory().getAbsolutePath());
				prefs.flush();
			}
			if (logFs == null) { return; }
			for (File f : logFs) {
				LogViewer lv = new LogViewer(f);
				lv.setVisible(true);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e);
			e.printStackTrace();
		}
	}
	
	ArrayList<Entry> log = new ArrayList<Entry>();
	Node top = new Node("All", null);
	JScrollPane treePane;
		JTree tree;
	
	static class Node implements TreeNode {
		Node parent;
		String name;
		long start;
		long end;
		ArrayList<Node> children = new ArrayList<Node>();

		public Node(String name, Node parent) {
			this.name = name;
			this.parent = parent;
		}

		@Override
		public TreeNode getChildAt(int i) {
			return children.get(i);
		}

		@Override
		public int getChildCount() {
			return children.size();
		}

		@Override
		public TreeNode getParent() {
			return parent;
		}

		@Override
		public int getIndex(TreeNode tn) {
			return children.indexOf(tn);
		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		public boolean isLeaf() {
			return children.isEmpty();
		}

		@Override
		public Enumeration children() {
			return Collections.enumeration(children);
		}
		
		long timeTaken() {
			return end - start;
		}
		
		@Override
		public String toString() {
			long internalTT = 0;
			for (Node c : children) {
				internalTT += c.timeTaken();
			}
			return name + " " + (timeTaken() / 1000) + " microseconds " + (timeTaken() - internalTT) / 1000 + " overhead";
		}
	}
	
	class Renderer extends DefaultTreeCellRenderer {
		@Override
		public Component getTreeCellRendererComponent(JTree jtree, Object o, boolean bln, boolean bln1, boolean bln2, int i, boolean bln3) {
			JLabel l = (JLabel) super.getTreeCellRendererComponent(jtree, o, bln, bln1, bln2, i, bln3);
			Node n = (Node) o;
			String clr = "#666666";
			if (n.end - n.start > 20 * 1000) {
				clr = "black";
			}
			if (n.end - n.start > 200 * 1000) {
				clr = "#660000";
			}
			if (n.end - n.start > 2000 * 1000) {
				clr = "#cc0000";
			}
			l.setText("<html><font color=\"" + clr + "\">" + n.toString() + "</font></html>");
			return l;
		}
	}
	
	private LogViewer(File logF) throws Exception {
		super("Log Viewer: " + logF.getName());
		BufferedReader r = new BufferedReader(new FileReader(logF));
		String l;
		while ((l = r.readLine()) != null) {
			log.add(new SpikeProfiler.Entry(l));
		}
		r.close();
		
		LinkedList<Node> stack = new LinkedList<Node>();
		stack.add(top);
		top.start = log.get(0).nanoStamp;
		top.end = log.get(log.size() - 1).nanoStamp;
		for (Entry e : log) {
			if (e.start) {
				Node n = new Node(e.text, stack.peekLast());
				n.start = e.nanoStamp;
				stack.peekLast().children.add(n);
				stack.add(n);
			} else {
				if (!stack.peekLast().name.equals(e.text)) {
					System.err.println("Level mismatch: expected " + e.text + ", got " + stack.peekLast().name);
				}
				while (stack.size() > 2 && !stack.peekLast().name.equals(e.text)) {
					stack.pollLast().end = e.nanoStamp;
				}
				stack.pollLast().end = e.nanoStamp;
			}
		}
		
		setLayout(new BorderLayout());
		add(treePane = new JScrollPane(), BorderLayout.CENTER);
		treePane.setViewportView(tree = new JTree(top));
		tree.setCellRenderer(new Renderer());
		setSize(800, 800);
		setLocationRelativeTo(null);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

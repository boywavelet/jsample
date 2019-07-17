package jsample.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TreeFormat<Payload> {

	private List<Node<Payload>> roots;
	public TreeFormat(List<Node<Payload>> roots) {
		this.roots = roots;
	}
	
	public void format(PrintWriter writer, Predicate<Payload> filter) {
		for (Node<Payload> root : roots) {
			formatNode(root, writer, filter);
		}
	}
	
	private void formatNode(Node<Payload> node, PrintWriter writer, Predicate<Payload> filter) {
		if (filter != null && !filter.test(node.getPayload())) {
			return;
		}
		writer.println(node);
		for (Node<Payload> child : node.getChildren()) {
			formatNode(child, writer, filter);
		}
	}

	public static class Node<Payload> {
		public static String DIRECT_PREFIX = "+---------";
		public static String PRE_PREFIX = "|         ";
		private Payload payload;
		private int level;
		private List<Node<Payload>> children;
		
		public Node(Payload payload, int level) {
			this.payload = payload;
			this.level = level;
			this.children = new ArrayList<Node<Payload>>();
		}
		
		public void addChild(Node<Payload> child) {
			this.children.add(child);
		}
		
		public int getLevel() {
			return level;
		}
		
		public Payload getPayload() {
			return payload;
		}
		
		public List<Node<Payload>> getChildren() {
			return children;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < level - 1; ++i) {
				sb.append(PRE_PREFIX);
			}
			sb.append(DIRECT_PREFIX);
			sb.append(payload.toString());
			return sb.toString();
		}
	}
	
	public static void main(String[] args) {
		Node<String> r1 = new Node<String>("A", 1);
		Node<String> r11 = new Node<String>("A_1", 2);
		Node<String> r12 = new Node<String>("A_2", 2);
		Node<String> r121 = new Node<String>("A_2_1", 3);
		Node<String> r122 = new Node<String>("A_2_2_solong", 3);
		r12.addChild(r121);
		r12.addChild(r122);
		r1.addChild(r11);
		r1.addChild(r12);
		
		List<Node<String>> roots = new ArrayList<Node<String>>();
		roots.add(r1);
		
		TreeFormat<String> tf = new TreeFormat<String>(roots);
		tf.format(new PrintWriter(System.out, true), (String str)->str.length() <= 5);
	}
}

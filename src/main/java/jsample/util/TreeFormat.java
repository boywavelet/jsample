package jsample.util;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TreeFormat<Payload extends Comparable<Payload>> {

	private List<Node<Payload>> roots;
	private int maxLevel = Integer.MAX_VALUE;
	public TreeFormat(List<Node<Payload>> roots, int maxLevel) {
		this.roots = roots;
		this.maxLevel = maxLevel;
	}
	
	public TreeFormat(List<Node<Payload>> roots) {
		this(roots, Integer.MAX_VALUE);
	}
	
	public void format(PrintWriter writer, Predicate<Payload> filter) {
		for (Node<Payload> root : roots) {
			formatNode(root, 1, -1.0, writer, filter);
		}
	}
	
	private void formatNode(Node<Payload> node, int level, double weight, PrintWriter writer, Predicate<Payload> filter) {
		if (filter != null && !filter.test(node.getPayload())) {
			return;
		}
		if (level > maxLevel) {
			return;
		}
		writer.println(node.format(level, weight));
		
		List<Node<Payload>> children = node.getChildren();
		List<Double> edgeWeights = node.getEdgeWeights();
		for (int i = 0; i < children.size(); ++i) {
			formatNode(children.get(i), level + 1, edgeWeights.get(i), writer, filter);
		}
	}

	public static class Node<Payload extends Comparable<Payload>> implements Comparable<Node<Payload>> {
		public static String DIRECT_PREFIX = "+---------";
		public static String PREPRE_PREFIX = "|         ";
		private Payload payload;
		private List<Node<Payload>> children;
		private List<Double> edgeWeights;
		
		public Node(Payload payload) {
			this.payload = payload;
			this.children = new ArrayList<Node<Payload>>();
			this.edgeWeights = new ArrayList<>();
		}
		
		public List<Double> getEdgeWeights() {
			return this.edgeWeights;
		}

		public void addChild(Node<Payload> child, double weight) {
			this.children.add(child);
			this.edgeWeights.add(weight);
		}
		
		public Payload getPayload() {
			return payload;
		}
		
		public List<Node<Payload>> getChildren() {
			return children;
		}
		
		@Override
		public String toString() {
			return payload.toString();
		}
		
		public String format(int level, double weight) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < level - 1; ++i) {
				sb.append(PREPRE_PREFIX);
			}
			sb.append(DIRECT_PREFIX);
			if (weight > 0) {
				NumberFormat formatter = new DecimalFormat("#0.00");
				sb.append(formatter.format(weight) + " -> ");
			}
			sb.append(payload.toString());
			return sb.toString();
		}

		@Override
		public int compareTo(Node<Payload> other) {
			return this.getPayload().compareTo(other.getPayload());
		}
	}
	
	public static void main(String[] args) {
		Node<String> r1 = new Node<String>("A");
		Node<String> r2 = new Node<String>("B");
		Node<String> r11 = new Node<String>("A_1");
		Node<String> r12 = new Node<String>("A_2");
		Node<String> r121 = new Node<String>("A_2_1");
		Node<String> r122 = new Node<String>("A_2_2_solong");
		r12.addChild(r121, 121);
		r12.addChild(r122, 122);
		r1.addChild(r12, 12);
		r1.addChild(r11, 11);
		
		List<Node<String>> roots = new ArrayList<Node<String>>();
		roots.add(r1);
		roots.add(r2);
		
		TreeFormat<String> tf = new TreeFormat<String>(roots);
		System.out.println("Format with name.length < 15");
		tf.format(new PrintWriter(System.out, true), (String str)->str.length() <= 15);
		System.out.println("Format with name.length < 5");
		tf.format(new PrintWriter(System.out, true), (String str)->str.length() <= 5);
		tf = new TreeFormat<String>(roots, 2);
		System.out.println("Format with level <= 2");
		tf.format(new PrintWriter(System.out, true), null);
	}
}

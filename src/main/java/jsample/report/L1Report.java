package jsample.report;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;

import jsample.util.TreeFormat;

public class L1Report {
	
	public static void reportL1full(Map<String, TreeFormat.Node<StackFrame>> allNodeMap, 
			Path l1fullPath) throws IOException {
		List<TreeFormat.Node<StackFrame>> nodeScores = new ArrayList<>();
		for (Map.Entry<String, TreeFormat.Node<StackFrame>> entry : allNodeMap.entrySet()) {
			TreeFormat.Node<StackFrame> node = entry.getValue();
			nodeScores.add(node);
		}
		Collections.sort(nodeScores);
		
		PrintWriter writer = new PrintWriter(Files.newBufferedWriter(l1fullPath, Charsets.UTF_8, 
				StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE), true);
		try {
			for (TreeFormat.Node<StackFrame> node : nodeScores) {
				writer.println(node.getPayload());
			}
		} finally {
			writer.close();
		}
	}
	
	public static void reportL1pure(Map<String, TreeFormat.Node<StackFrame>> allNodeMap, 
			Path l1purePath) throws IOException {
		List<StackPureStat> pureList = new ArrayList<>();
		for (Map.Entry<String, TreeFormat.Node<StackFrame>> entry : allNodeMap.entrySet()) {
			String name = entry.getKey();
			TreeFormat.Node<StackFrame> node = entry.getValue();
			double fullWeight = node.getPayload().getScore();
			List<Double> edgeWeights = node.getEdgeWeights();
			for (double ew : edgeWeights) {
				fullWeight -= ew;
			}
			pureList.add(new StackPureStat(name, fullWeight));
		}
		Collections.sort(pureList);
		
		PrintWriter writer = new PrintWriter(Files.newBufferedWriter(l1purePath, Charsets.UTF_8, 
				StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE), true);
		try {
			for (StackPureStat sps : pureList) {
				writer.println(sps);
			}
		} finally {
			writer.close();
		}
	}

	public static class StackPureStat implements Comparable<StackPureStat> {
		private String name;
		private double cost;

		public StackPureStat(String name, double cost) {
			this.name = name;
			this.cost = cost;
		}

		@Override
		public int compareTo(StackPureStat other) {
			return -Double.compare(cost, other.cost);
		}
		
		@Override
		public String toString() {
			NumberFormat formatter = new DecimalFormat("#0.0000");
			return name + " : " + formatter.format(cost);
		}
	}
}

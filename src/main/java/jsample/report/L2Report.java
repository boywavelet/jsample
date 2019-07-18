package jsample.report;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;

import jsample.util.TreeFormat;

public class L2Report {
	
	private static void printUseage() {
		System.out.println("L2Report stat_input report_output_dir [l2_level] [threshold]");
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			printUseage();
			System.exit(1);
		}
		String statInput = args[0];
		String reportOutputDir = args[1];
		
		Files.createDirectories(Paths.get(reportOutputDir));
		Path l1fullPath = Paths.get(reportOutputDir, "l1full.report");
		Path l1purePath = Paths.get(reportOutputDir, "l1pure.report");
		Path l2OutputPath = Paths.get(reportOutputDir, "l2.report");
		
		
		int maxLevel = Integer.MAX_VALUE;
		if (args.length >= 3) {
			maxLevel = Integer.parseInt(args[2]);
		}
		double threshold = 0.01;
		if (args.length >= 4) {
			threshold = Double.parseDouble(args[3]);
		}
		
		Map<String, TreeFormat.Node<StackFrame>> rootMap = new HashMap<>();
		Map<String, TreeFormat.Node<StackFrame>> allNodeMap = new HashMap<>();
		Path statPath = Paths.get(statInput);
		try (BufferedReader reader = Files.newBufferedReader(statPath, Charsets.UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				String[] parts = line.split("###");
				if (parts.length != 2) {
					continue;
				}
				String[] frameNames = parts[0].split(",");
				double score = Double.parseDouble(parts[1]);
				if (frameNames.length == 1) {
					StackFrame frame = new StackFrame(frameNames[0], score);
					TreeFormat.Node<StackFrame> node = new TreeFormat.Node<StackFrame>(frame);
					rootMap.put(frameNames[0], node);
					allNodeMap.put(frameNames[0], node);
				} else if (frameNames.length == 2) {
					String parentName = frameNames[0];
					String childName = frameNames[1];
					TreeFormat.Node<StackFrame> parentNode = allNodeMap.get(parentName);
					TreeFormat.Node<StackFrame> childNode = allNodeMap.get(childName);
					if (parentNode != null && childNode != null) {
						rootMap.remove(childName);
						parentNode.addChild(childNode, score);
					} else {
						System.out.println(line);
					}
				} else {
					//l2 report don't care info with level > 2
				}
			}
		}
		List<TreeFormat.Node<StackFrame>> roots = new ArrayList<>();
		for (Map.Entry<String, TreeFormat.Node<StackFrame>> entry : rootMap.entrySet()) {
			roots.add(entry.getValue());
		}
		Collections.sort(roots);
		
		PrintWriter writer = new PrintWriter(Files.newBufferedWriter(l2OutputPath, Charsets.UTF_8, 
				StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE), true);
		try {
			//TODO config max format level
			TreeFormat<StackFrame> format = new TreeFormat<>(roots, maxLevel);
			final double ft = threshold; 
			format.format(writer, (StackFrame frame) -> frame.getScore() > ft);
		} finally {
			writer.close();
		}
		
		L1Report.reportL1full(allNodeMap, l1fullPath);
		L1Report.reportL1pure(allNodeMap, l1purePath);
	}

}

class StackFrame implements Comparable<StackFrame> {
	private String frameName;
	private double score;
	public StackFrame(String frameName, double score) {
		this.frameName = frameName;
		this.score = score;
	}
	
	public String getFrameName() {
		return frameName;
	}
	
	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(StackFrame other) {
		return -Double.compare(this.score, other.score);
	}
	
	@Override
	public String toString() {
		NumberFormat formatter = new DecimalFormat("#0.00");
		return frameName + " : " + formatter.format(score);
	}
}

package jsample.report;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;

import jsample.util.TreeFormat;

public class L2Report {

	public L2Report() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws IOException {
		//TODO input
		String statInput = "";
		
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
				String[] frameNames = parts[0].split(":");
				double score = Double.parseDouble(parts[1]);
				if (frameNames.length == 1) {
					StackFrame frame = new StackFrame(frameNames[0], score);
					rootMap.put(frameNames[0], new TreeFormat.Node<StackFrame>(frame, 1));
					
					//TODO
				}
			}
		}
		
		
	}

}

class StackFrame {
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
}

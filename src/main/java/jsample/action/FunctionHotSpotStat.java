package jsample.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;

import jsample.jsample.PropertyNames;

public class FunctionHotSpotStat implements StatAction {

	private Properties props;
	private List<String> nameMatches;
	private ConcurrentMap<String, ConcurrentMap<Integer, Integer>> statMap = new ConcurrentHashMap<>();
	public FunctionHotSpotStat(Properties props) {
		this.props = props;
		parseNameMatches();
	}
	
	private void parseNameMatches() {
		String functionConf = props.getProperty(PropertyNames.WATCH_FUNCTIONS, "");
		nameMatches = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(functionConf);
	}

	@Override
	public void record(StackTraceElement[] ste) {
		for (StackTraceElement element : ste) {
			if (needRecord(element)) {
				String name = getStackName(element);
				int lineNum = element.getLineNumber();
				statMap.putIfAbsent(name, new ConcurrentHashMap<Integer, Integer>());
				ConcurrentMap<Integer, Integer> stat = statMap.get(name);
				if (stat.putIfAbsent(lineNum, 1) != null) {
					boolean needCont = true;
					while (needCont) {
						int oldCount = stat.get(lineNum);
						int newCount = oldCount + 1;
						needCont = !stat.replace(lineNum, oldCount, newCount);
					}
				}
			}
		}
	}
	
	private boolean needRecord(StackTraceElement element) {
		String name = getStackName(element);
		for (String match : nameMatches) {
			if (name.contains(match)) {
				return true;
			}
		}
		return false;
	}
	
	private String getStackName(StackTraceElement element) {
		return element.getClassName() + ":" + element.getMethodName();
	}

	@Override
	public void fini() throws IOException {
		String outFileName = props.getProperty(PropertyNames.STAT_OUTPUT_PATH, "stat.out");
		String sep = props.getProperty(PropertyNames.STAT_OUTPUT_SEPERATOR, "###");
		PrintWriter writer = new PrintWriter(
				Files.newBufferedWriter(Paths.get(outFileName + ".function." + System.currentTimeMillis()), Charsets.UTF_8, 
						StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
		NumberFormat formatter = new DecimalFormat("#0.0000");
		try {
			for (Map.Entry<String, ConcurrentMap<Integer, Integer>> stat : statMap.entrySet()) {
				String name = stat.getKey();
				ConcurrentMap<Integer, Integer> countMap = stat.getValue();
				double sum = 0;
				for (Map.Entry<Integer, Integer> count : countMap.entrySet()) {
					sum += count.getValue();
				}
				
				for (Map.Entry<Integer, Integer> count : countMap.entrySet()) {
					int lineNum = count.getKey();
					double percent = count.getValue() / sum;
					writer.println(name + sep + lineNum + sep + formatter.format(percent));
				}
			}
		} finally {
			writer.close();
		}
	}

}

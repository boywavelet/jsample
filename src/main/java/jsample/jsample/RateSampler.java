package jsample.jsample;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.RateLimiter;

public class RateSampler {

	private ConcurrentMap<String, Long> l1Stat = new ConcurrentHashMap<String, Long>();
	private ConcurrentMap<String, Long> l2Stat = new ConcurrentHashMap<String, Long>();
	private AtomicInteger tick = new AtomicInteger(0);
	private List<SampleFilter> filters;
	private Properties props;
	private RateLimiter limit;
	private int threadNum;
	private ExecutorService service;
	private boolean isStarted;
	public RateSampler(Properties props) {
		this.props = props;
		double sampleRate = Double.parseDouble(props.getProperty(PropertyNames.SAMPLE_RATE, "500.0"));
		this.limit = RateLimiter.create(sampleRate);
		this.threadNum = Integer.parseInt(props.getProperty(PropertyNames.THREAD_NUM, "1"));
		this.service = Executors.newFixedThreadPool(threadNum);
		this.filters = SampleFilterManager.createSampleFilters(props);
		this.isStarted = false;
	}

	public void start() {
		if (isStarted) {
			return;
		}
		for (int i = 0; i < threadNum; ++i) {
			service.execute(new RateSamplerRun(limit));
		}
		service.shutdown();
		isStarted = true;
	}
	
	public void stop() throws InterruptedException {
		if (!isStarted) {
			return;
		}
		service.shutdownNow();
		TimeUnit.SECONDS.sleep(5);
		try {
			exportData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		isStarted = false;
	}
	
	private void exportData() throws IOException {
		String outFileName = props.getProperty(PropertyNames.STAT_OUTPUT_PATH, "stat.out");
		String sep = props.getProperty(PropertyNames.STAT_OUTPUT_SEPERATOR, "###");
		PrintWriter writer = new PrintWriter(
				Files.newBufferedWriter(Paths.get(outFileName + "." + System.currentTimeMillis()), Charsets.UTF_8, 
						StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
		try {
			for (Map.Entry<String, Long> entry : l1Stat.entrySet()) {
				writer.println(entry.getKey() + sep + entry.getValue() * 1.0 / tick.get());
			}
			
			for (Map.Entry<String, Long> entry : l2Stat.entrySet()) {
				writer.println(entry.getKey() + sep + entry.getValue() * 1.0 / tick.get());
			}
		} finally {
			writer.close();
		}
	}
	
	private void fillStackTrace(StackTraceElement[] ste) {
		int start = ste.length;
		for (int i = 0; i < ste.length; ++i) {
			if (filter(ste, i)) {
				start = i;
				break;
			}
		}
		
		int end = -1;
		for (int i = ste.length - 1; i >= start; --i) {
			if (filter(ste, i)) {
				end = i;
				break;
			}
		}
		
		for (int i = start; i <= end; ++i) {
			updateL1(ste, i);
		}
		
		for (int i = start; i < end; ++i) {
			updateL2(ste, i);
		}
	}
	
	private void updateL1(StackTraceElement[] ste, int index) {
		String l1Key = getStackName(ste[index]);
		boolean needCont = true;
		l1Stat.putIfAbsent(l1Key, 0L);
		while (needCont) {
			long oldL1 = l1Stat.get(l1Key);
			long newL1 = 1 + oldL1;
			needCont = !l1Stat.replace(l1Key, oldL1, newL1);
		}
	}
	
	private void updateL2(StackTraceElement[] ste, int index) {
		if (index + 1 >= ste.length) {
			return;
		}
		String l2Key = getStackName(ste[index + 1]) + "," + getStackName(ste[index]);
		boolean needCont = true;
		l2Stat.putIfAbsent(l2Key, 0L);
		while (needCont) {
			long oldL1 = l2Stat.get(l2Key);
			long newL1 = 1 + oldL1;
			needCont = !l2Stat.replace(l2Key, oldL1, newL1);
		}
	}
	
	private String getStackName(StackTraceElement st) {
		return st.getClassName() + ":" + st.getMethodName();
	}
	
	private boolean filter(StackTraceElement[] ste, int index) {
		boolean accepted = false;
		for (SampleFilter filter : filters) {
			if (filter.reject(ste, index)) {
				return false;
			}
			accepted = accepted || filter.accept(ste, index);
		}
		return accepted;
	}
	
	public class RateSamplerRun implements Runnable {
		
		private RateLimiter limit;
		public RateSamplerRun(RateLimiter limit) {
			this.limit = limit;
		}

		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				limit.tryAcquire(1, TimeUnit.SECONDS);
				tick.incrementAndGet();
				
				Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
				for (Map.Entry<Thread, StackTraceElement[]> trace : traces.entrySet()) {
					StackTraceElement[] ste = trace.getValue();
					fillStackTrace(ste);
				}
			}
		}
		
	}
}

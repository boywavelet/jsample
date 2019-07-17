package jsample.jsample;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;

public class SampleManager {

	private static SampleManager INSTANCE = null;
	RateSampler sampler;
	String propPath;
	private SampleManager(String propPath) throws IOException {
		this.propPath = propPath;
		sampler = new RateSampler(loadProps(propPath));
	}

	public static synchronized void watch(String propPath) throws IOException {
		if (INSTANCE != null) {
			return;
		}
		Properties props = loadProps(propPath);
		INSTANCE = new SampleManager(propPath);
		Thread watch = new Thread(INSTANCE.new FileWatchRun(props));
		watch.setDaemon(true);
		watch.start();
	}
	
	private static Properties loadProps(String propPath) throws IOException {
		Properties props = new Properties();
		BufferedReader reader = Files.newBufferedReader(Paths.get(propPath), Charsets.UTF_8);
    	props.load(reader);
    	reader.close();
    	return props;
	}
	
	private class FileWatchRun implements Runnable {
		
		Properties props;
		Path startPath;
		Path stopPath;
		Path updatePath;
		
		Path startStatus;
		Path stopStatus;
		Path updateStatus;
		FileWatchRun(Properties props) throws IOException {
			this.props = props;
			String watchDir = props.getProperty(PropertyNames.WATCH_DIR, ".");
			this.startPath = Paths.get(watchDir, "sample.start");
			this.stopPath = Paths.get(watchDir, "sample.stop");
			this.updatePath = Paths.get(watchDir, "update.props");
			
			this.startStatus = Paths.get(watchDir, "start.status");
			this.stopStatus = Paths.get(watchDir, "stop.status");
			this.updateStatus = Paths.get(watchDir, "update.status");
			
			if (Files.notExists(startStatus)) {
				Files.createFile(startStatus);
			}
			if (Files.notExists(stopStatus)) {
				Files.createFile(stopStatus);
			}
			if (Files.notExists(updateStatus)) {
				Files.createFile(updateStatus);
			}
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					TimeUnit.SECONDS.sleep(1);
					if (Files.exists(startPath)) {
						Files.delete(startPath);
						sampler.start();
						startStatus.toFile().setLastModified(System.currentTimeMillis());
					}
					
					if (Files.exists(stopPath)) {
						Files.delete(stopPath);
						sampler.stop();
						stopStatus.toFile().setLastModified(System.currentTimeMillis());
					}
					
					if (Files.exists(updatePath)) {
						Properties updateProps = SampleManager.loadProps(updatePath.toString());
						this.props.putAll(updateProps);
						sampler.reset(this.props);
						Files.delete(updatePath);
						updateStatus.toFile().setLastModified(System.currentTimeMillis());
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (IOException e) {
					// just ignore
					e.printStackTrace();
				}
			}
			
		}
		
	}
}

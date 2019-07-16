package jsample.jsample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class SampleManager {

	private static SampleManager INSTANCE = null;
	RateSampler sampler;
	private SampleManager(Properties props) {
		sampler = new RateSampler(props);
	}

	public static void watch(Properties props) {
		if (INSTANCE != null) {
			return;
		}
		INSTANCE = new SampleManager(props);
		Thread watch = new Thread(INSTANCE.new FileWatchRun(props));
		watch.setDaemon(true);
		watch.start();
	}
	
	private class FileWatchRun implements Runnable {
		
		Path startPath;
		Path stopPath;
		FileWatchRun(Properties props) {
			String watchDir = props.getProperty(PropertyNames.WATCH_DIR, ".");
			this.startPath = Paths.get(watchDir, "sample.start");
			this.stopPath = Paths.get(watchDir, "sample.stop");
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					TimeUnit.SECONDS.sleep(1);
					if (Files.exists(startPath)) {
						Files.delete(startPath);
						sampler.start();
					}
					
					if (Files.exists(stopPath)) {
						Files.delete(stopPath);
						sampler.stop();
						//TODO make sampler reset or create New Sampler
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

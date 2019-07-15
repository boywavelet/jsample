package jsample.jsample;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Charsets;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) throws Exception 
    {
    	AtomicInteger ai = new AtomicInteger(0);
    	Random rand = new Random();
    	Thread tc = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					ai.addAndGet(rand.nextInt());
				}
			}
    	});
    	tc.setDaemon(true);tc.setName("bg_test");
    	tc.start();
    	Properties props = new Properties();
    	BufferedReader reader = Files.newBufferedReader(Paths.get("test/test.props"), Charsets.UTF_8);
    	props.load(reader);
    	reader.close();
    	RateSampler sampler = new RateSampler(props);
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
        	String line = sc.nextLine();
        	if (line.startsWith("start")) {
        		sampler.start();
        	}
        	if (line.startsWith("stop")) {
        		sampler.stop();
        		break;
        	}
        }
        sc.close();
        System.out.println(ai);
    }
}

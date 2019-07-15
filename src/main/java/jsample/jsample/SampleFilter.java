package jsample.jsample;

public interface SampleFilter {
	boolean accept(StackTraceElement[] ste, int index);
	
	boolean reject(StackTraceElement[] ste, int index);
}

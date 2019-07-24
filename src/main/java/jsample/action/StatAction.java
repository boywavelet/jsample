package jsample.action;

import java.io.IOException;

public interface StatAction {

	void record(StackTraceElement[] ste);
	
	void fini() throws IOException;
}

package jsample.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jsample.jsample.PropertyNames;

public class CollectiveStatAction implements StatAction {

	private List<StatAction> actions = new ArrayList<StatAction>();
	private CollectiveStatAction(Properties props) {
		if (props.containsKey(PropertyNames.WATCH_FUNCTIONS)) {
			actions.add(new FunctionHotSpotStat(props));
		}
	}
	
	public static CollectiveStatAction create(Properties props) {
		return new CollectiveStatAction(props);
	}

	@Override
	public void record(StackTraceElement[] ste) {
		for (StatAction action : actions) {
			action.record(ste);
		}
	}

	@Override
	public void fini() throws IOException {
		for (StatAction action : actions) {
			try {
				action.fini();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

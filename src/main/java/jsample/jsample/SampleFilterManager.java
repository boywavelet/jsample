package jsample.jsample;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import com.google.common.base.Splitter;

public class SampleFilterManager {

	private SampleFilterManager() {
	}
	
	public static List<SampleFilter> createSampleFilters(Properties props) {
		List<SampleFilter> filters = new ArrayList<SampleFilter>();
		//create all accept filters
		createPrefixAcceptFilters(props, filters);
		createContainAcceptFilters(props, filters);
		
		//if no accept filters, use AllPassFilter to make reject filters work
		if (filters.isEmpty()) {
			filters.add(new AllPassFilter());
		}
		
		//create all reject filters
		createPrefixRejectFilters(props, filters);
		createContainRejectFilters(props, filters);
		return filters;
	}

	private static void createPrefixAcceptFilters(Properties props, List<SampleFilter> filters) {
		String prefixAcceptConf = props.getProperty(PropertyNames.PREFIX_ACCEPT_FILTERS, "").trim();
		if (prefixAcceptConf.isEmpty()) {
			return;
		}
		Iterable<String> prefixes = Splitter.on(',').trimResults().omitEmptyStrings().split(prefixAcceptConf);
		for (String prefix : prefixes) {
			filters.add(new PrefixAcceptFilter(prefix));
		}
	}
	
	private static void createContainAcceptFilters(Properties props, List<SampleFilter> filters) {
		String matchAcceptConf = props.getProperty(PropertyNames.CONTAIN_ACCEPT_FILTERS, "").trim();
		if (matchAcceptConf.isEmpty()) {
			return;
		}
		Iterable<String> matches = Splitter.on(',').trimResults().omitEmptyStrings().split(matchAcceptConf);
		for (String match : matches) {
			filters.add(new StringMatchAcceptFilter(match));
		}
	}
	
	private static void createPrefixRejectFilters(Properties props, List<SampleFilter> filters) {
		String prefixRejectConf = props.getProperty(PropertyNames.PREFIX_REJECT_FILTERS, "");
		if (prefixRejectConf.isEmpty()) {
			return;
		}
		Iterable<String> prefixes = Splitter.on(',').trimResults().omitEmptyStrings().split(prefixRejectConf);
		for (String prefix : prefixes) {
			filters.add(new PrefixRejectFilter(prefix));
		}
	}
	
	private static void createContainRejectFilters(Properties props, List<SampleFilter> filters) {
		String matchRejectConf = props.getProperty(PropertyNames.CONTAIN_REJECT_FILTERS, "").trim();
		if (matchRejectConf.isEmpty()) {
			return;
		}
		Iterable<String> matches = Splitter.on(',').trimResults().omitEmptyStrings().split(matchRejectConf);
		for (String match : matches) {
			filters.add(new StringMatchRejectFilter(match));
		}
	}
	
	public static class PrefixAcceptFilter implements SampleFilter {
		
		private String prefix;
		public PrefixAcceptFilter(String prefix) {
			this.prefix = prefix;
		}

		public boolean accept(StackTraceElement[] ste, int index) {
			return ste[index].getClassName().startsWith(prefix);
		}

		public boolean reject(StackTraceElement[] ste, int index) {
			return false;
		}
		
	}
	
	public static class PrefixRejectFilter implements SampleFilter {
		
		private String prefix;
		public PrefixRejectFilter(String prefix) {
			this.prefix = prefix;
		}

		public boolean accept(StackTraceElement[] ste, int index) {
			return false;
		}

		public boolean reject(StackTraceElement[] ste, int index) {
			return ste[index].getClassName().startsWith(prefix);
		}
		
	}
	
	public static class AllPassFilter implements SampleFilter {

		public boolean accept(StackTraceElement[] ste, int index) {
			return true;
		}

		public boolean reject(StackTraceElement[] ste, int index) {
			return false;
		}
		
	}
	
	public static class StringMatchAcceptFilter implements SampleFilter {
		
		private String match;
		public StringMatchAcceptFilter(String match) {
			this.match = match;
		}

		@Override
		public boolean accept(StackTraceElement[] ste, int index) {
			return ste[index].getClassName().contains(match);
		}

		@Override
		public boolean reject(StackTraceElement[] ste, int index) {
			return false;
		}
		
	}
	
	public static class StringMatchRejectFilter implements SampleFilter {
		
		private String match;
		public StringMatchRejectFilter(String match) {
			this.match = match;
		}

		@Override
		public boolean accept(StackTraceElement[] ste, int index) {
			return false;
		}

		@Override
		public boolean reject(StackTraceElement[] ste, int index) {
			return ste[index].getClassName().contains(match);
		}
		
	}
}

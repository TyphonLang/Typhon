package info.iconmaster.typhon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.plugins.PluginLoader;
import info.iconmaster.typhon.plugins.TyphonPlugin;
import info.iconmaster.typhon.util.CommandLineHelper;
import info.iconmaster.typhon.util.CommandLineHelper.Option;

/**
 * This class holds all the options Typhon uses in the command line.
 * 
 * @see Typhon, CommandLineHelper
 * @author iconmaster
 *
 */
public class TyphonCommandLine {
	private TyphonCommandLine() {}
	
	public static final Option OPTION_HELP = new Option(new String[] {"help"}, new String[] {"h"}, false, "Produces this help message.");
	public static final Option OPTION_VERSION = new Option(new String[] {"version"}, new String[] {"v"}, false, "Prints the version and exits.");
	public static final Option OPTION_PATH = new Option(new String[] {"path"}, new String[] {"p"}, true, "Specifies a location where raw file imports look. Defaults to the CWD.");
	public static final Option OPTION_LIBS = new Option(new String[] {"include"}, new String[] {"i"}, true, "Specifies a file or directory of Typhon libraries.");
	
	/**
	 * The command line parser Typhon uses.
	 */
	public static CommandLineHelper getCommandLineHelper() {
		List<Option> options = new ArrayList<Option>() {{
			add(OPTION_HELP);
			add(OPTION_VERSION);
			add(OPTION_PATH);
			add(OPTION_LIBS);
		}};
		
		Map<Class<?>, Object> additionalOptions = PluginLoader.runHook(TyphonPlugin.AddCommandLineOptions.class);
		for (Object o : additionalOptions.values()) {
			if (o instanceof Option) {
				options.add((Option) o);
			} else if (o instanceof Option[]) {
				options.addAll(Arrays.asList((Option[]) o));
			} else if (o instanceof Collection) {
				options.addAll((Collection<? extends Option>) o);
			}
		}
		
		return new CommandLineHelper("typhon [-options] files...", options.toArray(new Option[0]));
	} 
}

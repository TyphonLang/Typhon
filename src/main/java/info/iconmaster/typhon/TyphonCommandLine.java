package info.iconmaster.typhon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import info.iconmaster.typhon.plugins.PluginLoader;
import info.iconmaster.typhon.plugins.TyphonPlugin;
import info.iconmaster.typhon.util.CommandLineHelper;
import info.iconmaster.typhon.util.CommandLineHelper.Command;
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
	
	public static Option OPTION_HELP;
	public static Option OPTION_VERSION;
	public static Option OPTION_PATH;
	public static Option OPTION_LIBS;
	
	public static Command COMMAND_CHECK;
	
	/**
	 * The command line parser Typhon uses.
	 */
	public static CommandLineHelper getCommandLineHelper() {
		OPTION_HELP = new Option(new String[] {"help"}, new String[] {"h"}, false, "Produces this help message.");
		OPTION_VERSION = new Option(new String[] {"version"}, new String[] {"v"}, false, "Prints the version and exits.");
		OPTION_PATH = new Option(new String[] {"path"}, new String[] {"p"}, true, "Specifies a location where raw file imports look. Defaults to the CWD.");
		OPTION_LIBS = new Option(new String[] {"include"}, new String[] {"i"}, true, "Specifies a file or directory of Typhon libraries.");
		
		COMMAND_CHECK = new Command("check", new String[] {"ch"}, "Checks the provided files for compilation errors.", Typhon.onRunCheck);
		
		List<Object> options = new ArrayList<Object>() {{
			add(COMMAND_CHECK);
			
			add(OPTION_HELP);
			add(OPTION_VERSION);
			add(OPTION_PATH);
			add(OPTION_LIBS);
		}};
		
		Map<Class<?>, Object> additionalOptions = PluginLoader.runHook(TyphonPlugin.AddCommandLineOptions.class);
		for (Object o : additionalOptions.values()) {
			if (o instanceof Option[]) {
				options.addAll(Arrays.asList((Option[]) o));
			} else if (o instanceof Command[]) {
				options.addAll(Arrays.asList((Command[]) o));
			} else if (o instanceof Object[]) {
				options.addAll(Arrays.asList((Object[]) o));
			} else if (o instanceof Collection) {
				options.addAll((Collection) o);
			} else {
				options.add(o);
			}
		}
		
		return new CommandLineHelper((Object[]) options.toArray(new Object[options.size()]));
	} 
}

package info.iconmaster.typhon;

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
	public static final CommandLineHelper CMD_PARSER = new CommandLineHelper("typhon [-options] files...", new Option[] {
			OPTION_HELP,
			OPTION_VERSION,
			OPTION_PATH,
			OPTION_LIBS,
	});
}

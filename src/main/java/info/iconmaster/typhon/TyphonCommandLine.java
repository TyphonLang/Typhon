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
	public static final Option OPTION_DEBUG = new Option(new String[] {"debug"}, new String[] {"d"}, false, "Turns on debug mode.");
	public static final Option OPTION_OUTPUT = new Option(new String[] {"output"}, new String[] {"o"}, true, "Specifies an output file.");
	
	/**
	 * The command line parser Typhon uses.
	 */
	public static final CommandLineHelper CMD_PARSER = new CommandLineHelper("typhon [-options] files...", new Option[] {
			OPTION_HELP,
			OPTION_VERSION,
			OPTION_DEBUG,
			OPTION_OUTPUT,
	});
}

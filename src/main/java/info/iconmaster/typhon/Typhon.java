package info.iconmaster.typhon;

import info.iconmaster.typhon.util.CommandLineHelper.Result;
import info.iconmaster.typhon.util.CommandLineHelper.UnknownOptionException;

public class Typhon {
	public static final String VERSION = "@TYPHON_VERSION@";
	
	public static void main(String[] args) {
		try {
			Result options = TyphonCommandLine.CMD_PARSER.parseCommandLine(args);
			
			if (options.optionalArguments.containsKey(TyphonCommandLine.OPTION_HELP)) {
				TyphonCommandLine.CMD_PARSER.printUsage(System.out);
				return;
			}
			
			if (options.optionalArguments.containsKey(TyphonCommandLine.OPTION_VERSION)) {
				System.out.println(VERSION);
				return;
			}
			
			if (options.positionalArguments.size() == 0) {
				System.err.println("error: no input files specified");
				System.err.println();
				TyphonCommandLine.CMD_PARSER.printUsage(System.err);
				return;
			}
		} catch (UnknownOptionException e) {
			System.err.println("error: "+e.getMessage());
			System.err.println();
			TyphonCommandLine.CMD_PARSER.printUsage(System.err);
			return;
		}
	}
}

package info.iconmaster.typhon.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class parses command line arguments in a standard fashion.
 * 
 * @author iconmaster
 *
 */
public class CommandLineHelper {
	/**
	 * This class represents a command-line option.
	 * It can have multiple names, short ('-') and long ('--'), forms, and can optionally take an argument.
	 * 
	 * @author iconmaster
	 *
	 */
	public static class Option {
		/**
		 * The long ('--') names this option can have.
		 */
		public String[] names;
		
		/**
		 * The short ('-') names this option can have.
		 */
		public String[] shortNames;
		
		/**
		 * True if this option needs an argument.
		 */
		public boolean takesArg;
		
		/**
		 * A short help description for this option.
		 */
		public String description;
		
		/**
		 * @param names The long ('--') names this option can have.
		 * @param shortNames The short ('-') names this option can have.
		 * @param takesArg True if this option needs an argument.
		 * @param description A short help description for this option.
		 */
		public Option(String[] names, String[] shortNames, boolean takesArg, String description) {
			this.names = names;
			this.shortNames = shortNames;
			this.takesArg = takesArg;
			this.description = description;
		}
	}
	
	/**
	 * This class holds result data for a command-line parse.
	 * Returned by {@link CommandLineHelper}.parseCommandLine.
	 * 
	 * @author iconmaster
	 *
	 */
	public static class Result {
		/**
		 * The positional arguments.
		 */
		public List<String> positionalArguments = new ArrayList<>();
		
		/**
		 * The optional arguments.
		 */
		public Map<Option, List<String>> optionalArguments = new HashMap<>();
	}
	
	/**
	 * This exception is thrown when an unknown option is encountered by the parser.
	 * 
	 * @author iconmaster
	 *
	 */
	public static class UnknownOptionException extends IllegalArgumentException {
		private String message;

		public UnknownOptionException(String message) {
			super();
			this.message = message;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
	}
	
	/**
	 * The usage message for your command-line application.
	 */
	public String usage;
	
	/**
	 * The options your command-line application can take.
	 */
	public Option[] options;
	
	/**
	 * Creates a new parser. Call parseCommandLine to parse your arguments.
	 * 
	 * @param usage The usage message for your command-line application.
	 * @param options The options your command-line application can take.
	 */
	public CommandLineHelper(String usage, Option... options) {
		this.usage = usage;
		this.options = options;
	}
	
	/**
	 * Prints your application's help message to the given stream.
	 * 
	 * @param rawStream The stream to output to. Usually <tt>System.out</tt> or <tt>System.err</tt>.
	 */
	public void printUsage(OutputStream rawStream) {
		PrintStream out = new PrintStream(rawStream);
		
		out.println("usage: " + usage);
		out.println();
		out.println("available options:");
		
		for (Option o : options) {
			out.print('\t');
			
			boolean firstName = true;
			for (String name : o.names) {
				if (firstName) {
					firstName = false;
				} else {
					out.print(',');
				}
				out.print("--");
				out.print(name);
			}
			
			if (o.shortNames.length > 0) {
				out.print(" (");
				boolean firstShortName = true;
				for (String name : o.shortNames) {
					if (firstShortName) {
						firstShortName = false;
					} else {
						out.print(',');
					}
					out.print('-');
					out.print(name);
				}
				out.print(')');
			}
			
			out.print(": ");
			out.println(o.description);
		}
	}
	
	/**
	 * Parses a set of command-line arguments.
	 * 
	 * @param args The command-line arguments, as obtained by your <tt>main</tt>.
	 * @return The result of the parse.
	 * @throws UnknownOptionException If an unknown option is encountered.
	 */
	public Result parseCommandLine(String[] args) throws UnknownOptionException {
		Result result = new Result();
		Option o = null;
		boolean ignoreOptions = false;
		
		for (String arg : args) {
			if (ignoreOptions) {
				result.positionalArguments.add(arg);
			} else if (o == null) {
				if (arg.equals("--")) {
					ignoreOptions = true;
				} else if (arg.startsWith("--")) {
					String name = arg.substring(2);
					
					outerLoop: for (Option match : options) {
						for (String matchName : match.names) {
							if (matchName.equals(name)) {
								o = match;
								break outerLoop;
							}
						}
					}
					
					if (o == null) {
						throw new UnknownOptionException("unknown option "+arg);
					}
				} else if (arg.startsWith("-")) {
					String name = arg.substring(1);
					
					outerLoop: for (Option match : options) {
						for (String matchName : match.shortNames) {
							if (matchName.equals(name)) {
								o = match;
								break outerLoop;
							}
						}
					}
					
					if (o == null) {
						throw new UnknownOptionException("unknown option "+arg);
					}
				} else {
					result.positionalArguments.add(arg);
				}
				
				if (o != null && !o.takesArg) {
					result.optionalArguments.put(o, new ArrayList<>());
					o = null;
				}
			} else {
				result.optionalArguments.putIfAbsent(o, new ArrayList<>());
				result.optionalArguments.get(o).add(arg);
				o = null;
			}
		}
		
		return result;
	}
}

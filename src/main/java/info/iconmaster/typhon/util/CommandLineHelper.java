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
	 * Typhon requires one or more subcommands in order to run.
	 * This is a specifier for a subcommand.
	 * 
	 * @author iconmaster
	 *
	 */
	public static class Command {
		/**
		 * The official name for this command.
		 */
		public String longName;
		
		/**
		 * A list of shorter shortcut names for this command.
		 */
		public String[] shortNames;
		
		/**
		 * A short description of what running this command does.
		 */
		public String description;
		
		/**
		 * @param longName The official name for this command.
		 * @param shortNames A list of shorter shortcut names for this command.
		 * @param description A short description of what running this command does.
		 */
		public Command(String longName, String[] shortNames, String description) {
			this.longName = longName;
			this.shortNames = shortNames;
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
		
		/**
		 * The commands specified.
		 */
		public List<Command> commands = new ArrayList<>();
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
	 * This exception is thrown when an unknown subcommand is encountered by the parser.
	 * 
	 * @author iconmaster
	 *
	 */
	public static class UnknownCommandException extends IllegalArgumentException {
		private String message;

		public UnknownCommandException(String message) {
			this.message = message;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
	}
	
	/**
	 * The options this command-line application can take.
	 */
	public Option[] options;
	
	/**
	 * The subcommands this command-line application can take.
	 */
	public Command[] commands;
	
	/**
	 * Creates a new parser. Call parseCommandLine to parse your arguments.
	 * 
	 * @param usage The usage message for your command-line application.
	 * @param options The options your command-line application can take.
	 */
	public CommandLineHelper(Object... options) {
		List<Option> flags = new ArrayList<>();
		List<Command> commands = new ArrayList<>();
		
		for (Object option : options) {
			if (option instanceof Option) {
				flags.add((Option) option);
			} else if (option instanceof Command) {
				commands.add((Command) option);
			} else {
				throw new IllegalArgumentException("Cannot have command-line option of "+option.getClass());
			}
		}
		
		this.options = flags.toArray(new Option[flags.size()]);
		this.commands = commands.toArray(new Command[commands.size()]);
	}
	
	/**
	 * Prints your application's help message to the given stream.
	 * 
	 * @param rawStream The stream to output to. Usually <tt>System.out</tt> or <tt>System.err</tt>.
	 */
	public void printUsage(OutputStream rawStream) {
		PrintStream out = new PrintStream(rawStream);
		
		out.println("usage: typhon command [options...] [files...]");
		
		out.println();
		out.println("available commands:");
		
		for (Command c : commands) {
			out.print('\t');
			out.print(c.longName);
			
			if (c.shortNames.length > 0) {
				out.print(" (");
				boolean firstShortName = true;
				for (String name : c.shortNames) {
					if (firstShortName) {
						firstShortName = false;
					} else {
						out.print(',');
					}
					out.print(name);
				}
				out.print(')');
			}
			
			out.print(": ");
			out.println(c.description);
		}
		
		out.println("use commas to specify multiple commands to run in order.");
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
	public Result parseCommandLine(String[] args) throws UnknownOptionException, UnknownCommandException {
		Result result = new Result();
		Option o = null;
		boolean ignoreOptions = false;
		
		for (String arg : args) {
			if (ignoreOptions) {
				if (result.commands.isEmpty()) {
					String[] commands = arg.split(",");
					for (String s : commands) {
						Command found = null;
						
						for (Command command : this.commands) {
							if (command.longName.equals(s)) {
								found = command;
								break;
							}
							
							for (String shortName : command.shortNames) {
								if (shortName.equals(s)) {
									found = command;
									break;
								}
							}
						}
						
						if (found == null) {
							throw new UnknownCommandException("unknown command "+s);
						} else {
							result.commands.add(found);
						}
					}
				} else {
					result.positionalArguments.add(arg);
				}
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
					if (result.commands.isEmpty()) {
						String[] commands = arg.split(",");
						for (String s : commands) {
							Command found = null;
							
							for (Command command : this.commands) {
								if (command.longName.equals(s)) {
									found = command;
									break;
								}
								
								for (String shortName : command.shortNames) {
									if (shortName.equals(s)) {
										found = command;
										break;
									}
								}
							}
							
							if (found == null) {
								throw new UnknownCommandException("unknown command "+s);
							} else {
								result.commands.add(found);
							}
						}
					} else {
						result.positionalArguments.add(arg);
					}
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

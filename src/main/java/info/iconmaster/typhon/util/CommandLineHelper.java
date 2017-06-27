package info.iconmaster.typhon.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandLineHelper {
	public static class Option {
		public String[] names;
		public String[] shortNames;
		public boolean takesArg;
		public String description;
		
		public Option(String[] names, String[] shortNames, boolean takesArg, String description) {
			this.names = names;
			this.shortNames = shortNames;
			this.takesArg = takesArg;
			this.description = description;
		}
	}
	
	public static class Result {
		public List<String> positionalArguments = new ArrayList<>();
		public Map<Option, List<String>> optionalArguments = new HashMap<>();
	}
	
	public static class UnknownOptionException extends IllegalArgumentException {
		String message;

		public UnknownOptionException(String message) {
			super();
			this.message = message;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
	}
	
	public String usage;
	public Option[] options;
	
	public CommandLineHelper(String usage, Option... options) {
		this.usage = usage;
		this.options = options;
	}
	
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

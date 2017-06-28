package info.iconmaster.typhon;

import java.io.File;

import info.iconmaster.typhon.compiler.TyphonSourceReader;
import info.iconmaster.typhon.language.Package;
import info.iconmaster.typhon.util.CommandLineHelper.Result;
import info.iconmaster.typhon.util.CommandLineHelper.UnknownOptionException;
import info.iconmaster.typhon.util.FileUtils;

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
			
			TyphonInput tni = new TyphonInput();
			for (String fileName : options.positionalArguments) {
				File file = new File(fileName);
				if (!file.exists()) {
					System.err.println("error: file or folder '"+fileName+"' does not exist");
					return;
				}
				tni.inputFiles.addAll(FileUtils.getAllFiles(file, FileUtils.FILTER_TYPHON_FILES));
			}
			
			for (File file : tni.inputFiles) {
				Package p = TyphonSourceReader.parseFile(tni, file);
				tni.inputPackages.add(p);
			}
		} catch (UnknownOptionException e) {
			System.err.println("error: "+e.getMessage());
			System.err.println();
			TyphonCommandLine.CMD_PARSER.printUsage(System.err);
			return;
		}
	}
}

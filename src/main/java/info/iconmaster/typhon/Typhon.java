package info.iconmaster.typhon;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import info.iconmaster.typhon.compiler.TyphonSourceReader;
import info.iconmaster.typhon.errors.TyphonError;
import info.iconmaster.typhon.language.Package;
import info.iconmaster.typhon.util.CommandLineHelper.Result;
import info.iconmaster.typhon.util.CommandLineHelper.UnknownOptionException;
import info.iconmaster.typhon.util.FileUtils;

/**
 * Contains basic constants and the main command line routine.
 * 
 * @author iconmaster
 *
 */
public class Typhon {
	/**
	 * The version of the Typhon compiler.
	 * TODO: fix the build script to replace TYPHON_VERSION with the correct value
	 */
	public static final String VERSION = "@TYPHON_VERSION@";
	
	/**
	 * The main function.
	 * 
	 * @param args
	 */
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
				Package p;
				try {
					p = TyphonSourceReader.parseFile(tni, file);
					tni.inputPackages.add(p);
				} catch (IOException e) {
					System.err.println("error: cannot read input file '"+file.getName()+"': "+e.getMessage());
					return;
				}
			}
			
			// check for errors
			
			if (!tni.errors.isEmpty()) {
				for (TyphonError error : tni.errors) {
					System.err.println(error);
					
					File file = new File(error.source.file);
					if (error.source.file != null && file.exists()) {
						try {
							Scanner scanner = new Scanner(file);
							scanner.useDelimiter("[\r\n]");
							int offset = 0;
							
							while (scanner.hasNext()) {
								String line = scanner.next();
								offset += line.length() + 1;
								
								if (offset > error.source.begin) {
									System.err.println("\t"+line.replace('\t', ' '));
									
									int curPos = offset-error.source.begin;
									System.err.print('\t');
									for (int i = 0; i < curPos; i++) {
										System.err.print(' ');
									}
									System.err.println('^');
									
									break;
								}
							}
						} catch (IOException e) {
							// ignore; we don't need location information THAT badly
						}
					}
				}
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

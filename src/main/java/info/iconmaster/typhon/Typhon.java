package info.iconmaster.typhon;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import info.iconmaster.typhon.compiler.TyphonCompiler;
import info.iconmaster.typhon.errors.TyphonError;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TyphonModelReader;
import info.iconmaster.typhon.plugins.PluginLoader;
import info.iconmaster.typhon.plugins.TyphonPlugin;
import info.iconmaster.typhon.types.TyphonTypeResolver;
import info.iconmaster.typhon.util.CommandLineHelper;
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
		PluginLoader.loadPlugins();
		CommandLineHelper claHelper = TyphonCommandLine.getCommandLineHelper();
		
		try {
			Result options = TyphonCommandLine.getCommandLineHelper().parseCommandLine(args);
			
			if (options.optionalArguments.containsKey(TyphonCommandLine.OPTION_HELP)) {
				claHelper.printUsage(System.out);
				return;
			}
			
			if (options.optionalArguments.containsKey(TyphonCommandLine.OPTION_VERSION)) {
				System.out.println(VERSION);
				return;
			}
			
			PluginLoader.runHook(TyphonPlugin.OnCompilationBegun.class, claHelper, options);
			
			if (options.positionalArguments.size() == 0) {
				System.err.println("error: no input files specified");
				System.err.println();
				claHelper.printUsage(System.err);
				return;
			}
			
			// build the TyphonInput
			
			TyphonInput tni = new TyphonInput();
			
			for (String fileName : options.positionalArguments) {
				File file = new File(fileName);
				if (!file.exists()) {
					System.err.println("error: file or folder '"+fileName+"' does not exist");
					return;
				}
				tni.inputFiles.addAll(FileUtils.getAllFiles(file, FileUtils.FILTER_TYPHON_FILES));
			}
			
			if (options.optionalArguments.containsKey(TyphonCommandLine.OPTION_LIBS)) {
				for (String fileName : options.optionalArguments.get(TyphonCommandLine.OPTION_LIBS)) {
					File file = new File(fileName);
					if (!file.exists()) {
						System.err.println("error: file or folder '"+fileName+"' does not exist");
						return;
					}
					tni.libraryFiles.addAll(FileUtils.getAllFiles(file, FileUtils.FILTER_TYPHON_FILES));
				}
			}
			
			if (options.optionalArguments.containsKey(TyphonCommandLine.OPTION_PATH)) {
				for (String fileName : options.optionalArguments.get(TyphonCommandLine.OPTION_PATH)) {
					tni.rawImportLookupPaths.add(new File(fileName));
				}
			} else {
				tni.rawImportLookupPaths.add(new File("."));
			}
			
			// parse the input files
			
			for (File file : tni.inputFiles) {
				Package p;
				try {
					p = TyphonModelReader.parseFile(tni, file);
					tni.inputPackages.add(p);
				} catch (IOException e) {
					System.err.println("error: cannot read input file '"+file.getName()+"': "+e.getMessage());
					return;
				}
			}
			
			// parse the libraries
			
			for (File file : tni.libraryFiles) {
				Package p;
				try {
					p = TyphonModelReader.parseFile(tni, file);
					p.markAsLibrary();
					tni.libraryPackages.add(p);
				} catch (IOException e) {
					System.err.println("error: cannot read library file '"+file.getName()+"': "+e.getMessage());
					return;
				}
			}
			
			// link the packages
			for (Package p : tni.libraryPackages) {
				TyphonLinker.link(p);
			}
			for (Package p : tni.inputPackages) {
				TyphonLinker.link(p);
			}
			
			// resolve types
			for (Package p : tni.libraryPackages) {
				TyphonTypeResolver.resolve(p);
			}
			for (Package p : tni.inputPackages) {
				TyphonTypeResolver.resolve(p);
			}
			
			// compile
			for (Package p : tni.libraryPackages) {
				TyphonCompiler.compile(p);
			}
			for (Package p : tni.inputPackages) {
				TyphonCompiler.compile(p);
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
			
			PluginLoader.runHook(TyphonPlugin.OnCompilationComplete.class, claHelper, options, tni);
		} catch (UnknownOptionException e) {
			System.err.println("error: "+e.getMessage());
			System.err.println();
			claHelper.printUsage(System.err);
			return;
		}
	}
}

package info.iconmaster.typhon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.errors.TyphonError;
import info.iconmaster.typhon.language.CorePackage;
import info.iconmaster.typhon.language.Package;

/**
 * This contains general data about the compilation currently in progress.
 * This class is required by many Typhon objects, to use as compilation context.
 * 
 * @author iconmaster
 *
 */
public class TyphonInput {
	/**
	 * A list of files Typhon was given as input.
	 * Its use is optional outside of command-line use.
	 */
	public List<File> inputFiles = new ArrayList<>();
	
	/**
	 * A list of packages to compile.
	 * This is important for determining what's library and what's output.
	 */
	public List<Package> inputPackages = new ArrayList<>();
	
	/**
	 * The core package. This package is the parent of all generated packages.
	 */
	public CorePackage corePackage = new CorePackage(this);
	
	/**
	 * A list of errors the compiler has reported.
	 * If this list isn't empty, the compilation was not successful.
	 */
	public List<TyphonError> errors = new ArrayList<>();
}

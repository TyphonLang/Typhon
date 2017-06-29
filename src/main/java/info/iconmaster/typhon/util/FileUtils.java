package info.iconmaster.typhon.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * General utilities for dealing with the file system.
 * 
 * @author iconmaster
 *
 */
public class FileUtils {
	private FileUtils() {}
	
	/**
	 * A @{link FileFilter} that returns only Typhon source (.tn) files.
	 */
	public static FileFilter FILTER_TYPHON_FILES = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.getName().endsWith(".tn");
		}
	};
	
	/**
	 * Returns all the files in a directory, recursively.
	 * 
	 * @param file The directory to obtain files from.
	 * @param filter A filter. May be null.
	 * @return All the files in a file tree. Returns the input if the input if a file.
	 */
	public static List<File> getAllFiles(File file, FileFilter filter) {
		if (file.isDirectory()) {
			ArrayList<File> result = new ArrayList<>();
			for (File subFile : file.listFiles()) {
				result.addAll(getAllFiles(subFile, filter));
			}
			return result;
		} else {
			return new ArrayList<File>() {{
				if (filter == null || filter.accept(file)) {
					add(file);
				}
			}};
		}
	}
}

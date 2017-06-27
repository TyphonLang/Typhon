package info.iconmaster.typhon.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	private FileUtils() {}
	
	public static FileFilter FILTER_TYPHON_FILES = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.getName().endsWith(".tn");
		}
	};
	
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

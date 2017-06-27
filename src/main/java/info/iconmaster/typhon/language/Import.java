package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

public abstract class Import {
	public Package resolvedTo;
	public List<String> aliasName = new ArrayList<>();
	
	public static class PackageImport extends Import {
		public List<String> packageName = new ArrayList<>();
	}
	
	public static class RawImport extends Import {
		public String importData;
	}
}

package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;

public abstract class Import extends TyphonLanguageEntity {
	public Package resolvedTo;
	public List<String> aliasName = new ArrayList<>();
	
	public Import(TyphonInput input) {
		super(input);
	}
	
	public static class PackageImport extends Import {
		public List<String> packageName = new ArrayList<>();
		
		public PackageImport(TyphonInput input) {
			super(input);
		}
	}
	
	public static class RawImport extends Import {
		public String importData;
		
		public RawImport(TyphonInput input) {
			super(input);
		}
	}
}

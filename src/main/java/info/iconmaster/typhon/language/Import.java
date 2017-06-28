package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

public abstract class Import extends TyphonLanguageEntity {
	public Package resolvedTo;
	public List<String> aliasName = new ArrayList<>();
	
	public Import(TyphonInput input) {
		super(input);
	}
	
	public Import(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public static class PackageImport extends Import {
		public List<String> packageName = new ArrayList<>();
		
		public PackageImport(TyphonInput input) {
			super(input);
		}
		
		public PackageImport(TyphonInput input, SourceInfo source) {
			super(input, source);
		}
	}
	
	public static class RawImport extends Import {
		public String importData;
		
		public RawImport(TyphonInput input) {
			super(input);
		}
		
		public RawImport(TyphonInput input, SourceInfo source) {
			super(input, source);
		}
	}
}

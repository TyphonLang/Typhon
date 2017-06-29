package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

public abstract class Import extends TyphonLanguageEntity {
	private Package resolvedTo;
	private List<String> aliasName = new ArrayList<>();
	
	public Import(TyphonInput input) {
		super(input);
	}
	
	public Import(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public static class PackageImport extends Import {
		private List<String> packageName = new ArrayList<>();
		
		public PackageImport(TyphonInput input) {
			super(input);
		}
		
		public PackageImport(TyphonInput input, SourceInfo source) {
			super(input, source);
		}

		public List<String> getPackageName() {
			return packageName;
		}

		public void setPackageName(List<String> packageName) {
			this.packageName = packageName;
		}
	}
	
	public static class RawImport extends Import {
		private String importData;

		public RawImport(TyphonInput input) {
			super(input);
		}
		
		public RawImport(TyphonInput input, SourceInfo source) {
			super(input, source);
		}
		
		public String getImportData() {
			return importData;
		}

		public void setImportData(String importData) {
			this.importData = importData;
		}
	}

	public Package getResolvedTo() {
		return resolvedTo;
	}

	public void setResolvedTo(Package resolvedTo) {
		this.resolvedTo = resolvedTo;
	}

	public List<String> getAliasName() {
		return aliasName;
	}

	public void setAliasName(List<String> aliasName) {
		this.aliasName = aliasName;
	}
}

package info.iconmaster.typhon.language;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This indicates that a certain package should be available to code in the same scope.
 * Exactly which package, and how to resolve which, is a task left to this class's subclasses.
 * 
 * @author iconmaster
 *
 */
public abstract class Import extends TyphonLanguageEntity {
	/**
	 * The package(s) this import resolves to.
	 * Will be empty if this.resolved is false.
	 */
	private List<Package> resolvedTo = new ArrayList<>();
	
	/**
	 * True if package resolution has occurred.
	 */
	private boolean resolved;
	
	/**
	 * The alias for this package.
	 * Will be empty if this.resolved is true; package resolution moves imports with aliases around.
	 */
	private List<String> aliasName = new ArrayList<>();
	
	public Import(TyphonInput input) {
		super(input);
	}
	
	public Import(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public static class PackageImport extends Import {
		/**
		 * The qualified name for the package we want to import.
		 */
		private List<String> packageName = new ArrayList<>();
		
		public PackageImport(TyphonInput input) {
			super(input);
		}
		
		public PackageImport(TyphonInput input, SourceInfo source) {
			super(input, source);
		}

		/**
		 * @return The qualified name for the package we want to import.
		 */
		public List<String> getPackageName() {
			return packageName;
		}
	}
	
	public static class RawImport extends Import {
		/**
		 * The raw file we wish to import.
		 */
		private String importData;

		public RawImport(TyphonInput input) {
			super(input);
		}
		
		public RawImport(TyphonInput input, SourceInfo source) {
			super(input, source);
		}
		
		/**
		 * @return The raw file we wish to import.
		 */
		public String getImportData() {
			return importData;
		}

		/**
		 * @param importData The new raw file we wish to import.
		 */
		public void setImportData(String importData) {
			this.importData = importData;
		}
	}

	/**
	 * @return The package(s) this import resolves to. Will be empty if this.isResolved() is false.
	 */
	public List<Package> getResolvedTo() {
		return resolvedTo;
	}

	/**
	 * @return The alias for this package. Will be empty if this.isResolved() is true; package resolution moves imports with aliases around.
	 */
	public List<String> getAliasName() {
		return aliasName;
	}

	/**
	 * @return True if package resolution has occurred.
	 */
	public boolean isResolved() {
		return resolved;
	}

	/**
	 * @param resolved The new resolution status. True if package resolution has occurred.
	 */
	public void isResolved(boolean resolved) {
		this.resolved = resolved;
	}
	
	/**
	 * The package this import belongs to.
	 */
	private Package parent;

	/**
	 * @return The package this import belongs to.
	 */
	public Package getParent() {
		return parent;
	}

	/**
	 * NOTE: Don't call this, call <tt>{@link Package}.addImport()</tt> instead.
	 * 
	 * @param parent The new package this import belongs to.
	 */
	public void setParent(Package parent) {
		this.parent = parent;
	}
}

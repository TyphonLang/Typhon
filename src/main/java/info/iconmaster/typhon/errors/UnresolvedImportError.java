package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.language.Import;
import info.iconmaster.typhon.language.Import.PackageImport;
import info.iconmaster.typhon.language.Import.RawImport;
import info.iconmaster.typhon.linker.TyphonLinker;

/**
 * This is an error for when the {@link TyphonLinker} cannot resolve an import.
 * 
 * @author iconmaster
 *
 */
public class UnresolvedImportError extends TyphonError {
	public Import importUnresolved;
	
	public UnresolvedImportError(Import importUnresolved) {
		super(importUnresolved.source);
		this.importUnresolved = importUnresolved;
	}
	
	@Override
	public String getMessage() {
		if (importUnresolved instanceof PackageImport) {
			return "cannot resolve import " + ((PackageImport)importUnresolved).getPackageName().stream().reduce("", (a,b)->a+"."+b).substring(1);
		} else if (importUnresolved instanceof RawImport) {
			return "cannot resolve import " + ((RawImport)importUnresolved).getImportData();
		} else {
			return "cannot resolve import";
		}
	}
}

package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is a type in Typhon's type system.
 * 
 * @author iconmaster
 *
 */
public abstract class Type extends TyphonModelEntity {
	/**
	 * The package that contains methods, fields, etc. for this type.
	 * 
	 * TODO: set this up properly.
	 */
	private Package typePackage;
	
	public Type(TyphonInput input) {
		super(input);
	}
	
	public Type(TyphonInput input, SourceInfo source) {
		super(input, source);
	}
	
	/**
	 * Returns the identifying name for this type.
	 * This string is used to look up this type from a package, among other thing.
	 * May be null; if null, this type cannot be placed in a package.
	 * 
	 * @return The name of this type.
	 */
	public String getName() {
		return null;
	}

	/**
	 * @return The package that contains methods, fields, etc. for this type.
	 */
	public Package getTypePackage() {
		if (typePackage == null) {
			typePackage = new Package(source, null, parent == null ? tni.corePackage : parent);
		}
		
		return typePackage;
	}
	
	/**
	 * The package this type belongs to.
	 */
	private Package parent;

	/**
	 * @return The package this type belongs to.
	 */
	public Package getParent() {
		return parent;
	}

	/**
	 * NOTE: Don't call this, call <tt>{@link Package}.addType()</tt> instead.
	 * 
	 * @param parent The new package this type belongs to.
	 */
	public void setParent(Package parent) {
		this.parent = parent;
		getTypePackage().setParent(parent);
	}
	
	@Override
	public void markAsLibrary() {
		super.markAsLibrary();
		
		getTypePackage().markAsLibrary();
	}
}

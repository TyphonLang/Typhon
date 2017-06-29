package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.language.Package;
import info.iconmaster.typhon.language.TyphonLanguageEntity;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is a type in Typhon's type system.
 * 
 * @author iconmaster
 *
 */
public abstract class Type extends TyphonLanguageEntity {
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
		return typePackage;
	}
}

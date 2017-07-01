package info.iconmaster.typhon.model;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.types.AnyType;
import info.iconmaster.typhon.types.SystemType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.UserType;

/**
 * The core package.
 * This package contains data for built-in types and functions.
 * All packages derive from the core package.
 * The core package cannot have a parent.
 * 
 * @author iconmaster
 *
 */
public class CorePackage extends Package {
	/**
	 * Constants for built-in types.
	 */
	public Type TYPE_ANY, TYPE_NUMBER, TYPE_INT;
	
	public CorePackage(TyphonInput tni) {
		super(tni, "core");
		
		TYPE_ANY = new AnyType(tni);
		addType(TYPE_ANY);
		
		TYPE_NUMBER = new UserType("Number", TYPE_ANY);
		addType(TYPE_NUMBER);
		
		TYPE_INT = new SystemType("int", TYPE_NUMBER);
		addType(TYPE_INT);
	}
	
	/**
	 * This always returns null.
	 * The core package cannot have a parent.
	 */
	@Override
	public Package getParent() {
		return null;
	}
	
	/**
	 * This always throws an {@link IllegalArgumentException}.
	 * The core package cannot have a parent.
	 */
	@Override
	public void setParent(Package parent) {
		throw new IllegalArgumentException("Cannot set parent of core package");
	}
}

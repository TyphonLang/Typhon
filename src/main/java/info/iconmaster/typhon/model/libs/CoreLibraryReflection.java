package info.iconmaster.typhon.model.libs;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.types.SystemType;

/**
 * This package contains things useful for reflection. Not all implementations of Typhon need to fully implement this library.
 * 
 * @author iconmaster
 *
 */
public class CoreLibraryReflection extends Package {
	/**
	 * This type represents a TypeRef.
	 */
	public SystemType TYPE_TYPE;
	
	public CoreLibraryReflection(TyphonInput tni) {
		super(tni, "reflect");
		
		addType(TYPE_TYPE = new SystemType(tni, "type"));
	}
}

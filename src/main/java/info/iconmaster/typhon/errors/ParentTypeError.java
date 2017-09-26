package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.types.UserType;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when an illegal parent type is declared for a UserType.
 * 
 * @author iconmaster
 *
 */
public class ParentTypeError extends TyphonError {
	UserType type;
	TypeRef badParent;
	
	public ParentTypeError(SourceInfo source, UserType type, TypeRef badParent) {
		super(source);
		this.type = type;
		this.badParent = badParent;
	}
	
	@Override
	public String getMessage() {
		return type.prettyPrint()+" cannot have a parent type of type "+badParent.prettyPrint();
	}
}

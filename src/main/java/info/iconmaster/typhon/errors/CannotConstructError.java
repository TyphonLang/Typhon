package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when you try to use 'new' on something that isn't constructible.
 * 
 * @author iconmaster
 *
 */
public class CannotConstructError extends TyphonError {
	/**
	 * The type in question.
	 */
	public TypeRef type;
	
	public CannotConstructError(SourceInfo source, TypeRef type) {
		super(source);
		this.type = type;
	}
	
	@Override
	public String getMessage() {
		return "Cannot construct new objects of type "+type.prettyPrint();
	}
}

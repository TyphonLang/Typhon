package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when we couldn't find a constructor with the given argument list.
 * 
 * @author iconmaster
 *
 */
public class ConstructorNotFoundError extends TyphonError {
	/**
	 * The type in question.
	 */
	public TypeRef type;
	
	public ConstructorNotFoundError(SourceInfo source, TypeRef type) {
		super(source);
		this.type = type;
	}
	
	@Override
	public String getMessage() {
		return "Cannot find constructor for "+type+" with given arguments";
	}
}

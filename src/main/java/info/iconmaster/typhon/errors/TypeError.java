package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when the compiler expects something of one type, and receives something of another type.
 * 
 * @author iconmaster
 *
 */
public class TypeError extends TyphonError {
	/**
	 * The types in question.
	 */
	public TypeRef a, b;
	
	public TypeError(SourceInfo source, TypeRef a, TypeRef b) {
		super(source);
		this.a = a;
		this.b = b;
	}
	
	@Override
	public String getMessage() {
		return "Expression of type "+a.getName()+" must be castable to type "+b.getName();
	}
}

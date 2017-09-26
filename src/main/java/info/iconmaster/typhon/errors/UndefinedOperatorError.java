package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when the compiler expects something of one type, and receives something of another type.
 * 
 * @author iconmaster
 *
 */
public class UndefinedOperatorError extends TyphonError {
	/**
	 * The types in question.
	 */
	public TypeRef a, b;
	
	/**
	 * The operator's name.
	 */
	public String op;
	
	public UndefinedOperatorError(SourceInfo source, String op, TypeRef a, TypeRef b) {
		super(source);
		this.op = op;
		this.a = a;
		this.b = b;
	}
	
	@Override
	public String getMessage() {
		return "Operator "+op+" undefined between types "+a.prettyPrint()+" and "+b.prettyPrint();
	}
}

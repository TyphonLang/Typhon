package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when a variable is referenced that does not exist.
 * 
 * @author iconmaster
 *
 */
public class UndefinedVariableError extends TyphonError {
	/**
	 * The variable's name.
	 */
	public String name;
	
	public UndefinedVariableError(SourceInfo source, String name) {
		super(source);
		this.name = name;
	}
	
	@Override
	public String getMessage() {
		return "undefined variable "+name;
	}
}

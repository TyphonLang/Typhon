package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.compiler.Variable;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when you try to create two local variables with the same name in the same scope.
 * 
 * @author iconmaster
 *
 */
public class DuplicateVarNameError extends TyphonError {
	/**
	 * The name of the variable we attempted to create.
	 */
	public String name;
	
	/**
	 * The variable that conflicts with this one.
	 */
	public Variable existingVar;
	
	public DuplicateVarNameError(SourceInfo source, String name, Variable existingVar) {
		super(source);
		this.name = name;
		this.existingVar = existingVar;
	}
	
	@Override
	public String getMessage() {
		return "Local variable "+name+" aready declared in the same scope";
	}
}

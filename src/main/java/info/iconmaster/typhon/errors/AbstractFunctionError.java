package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when a non-abstract type does not override an abstract function.
 * 
 * @author iconmaster
 *
 */
public class AbstractFunctionError extends TyphonError {
	Type t;
	Function f;
	
	public AbstractFunctionError(SourceInfo source, Type t, Function f) {
		super(source);
		
		this.t = t;
		this.f = f;
	}
	
	@Override
	public String getMessage() {
		return "non-abstract class "+t.prettyPrint()+" must implement abstract function "+f.prettyPrint();
	}
}

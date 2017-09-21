package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when a variable is referenced that does not exist.
 * 
 * @author iconmaster
 *
 */
public class ThisInStaticContextError extends TyphonError {
	public ThisInStaticContextError(SourceInfo source) {
		super(source);
	}
	
	@Override
	public String getMessage() {
		return "illegal use of 'this' in a static context";
	}
}

package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when a variable is referenced that does not exist.
 * 
 * @author iconmaster
 *
 */
public class StringFormatError extends TyphonError {
	String s;
	
	public StringFormatError(SourceInfo source, String s) {
		super(source);
		this.s = s;
	}
	
	@Override
	public String getMessage() {
		return "Invalid use of control character in "+s;
	}
}

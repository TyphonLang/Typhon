package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when ANTLR reports a parse error.
 * 
 * @author iconmaster
 *
 */
public class SyntaxError extends TyphonError {
	/**
	 * The message ANTLR returned.
	 */
	public String message;
	
	public SyntaxError(SourceInfo source, String message) {
		super(source);
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return "syntax error: "+message;
	}
}

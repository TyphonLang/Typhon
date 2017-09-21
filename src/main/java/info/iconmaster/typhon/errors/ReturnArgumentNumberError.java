package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when a variable is referenced that does not exist.
 * 
 * @author iconmaster
 *
 */
public class ReturnArgumentNumberError extends TyphonError {
	int expected, got;
	
	public ReturnArgumentNumberError(SourceInfo source, int expected, int got) {
		super(source);
		this.expected = expected;
		this.got = got;
	}
	
	@Override
	public String getMessage() {
		return "expected to return "+expected+" values, got "+got+" values";
	}
}

package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when a for loop is given too many variables.
 * 
 * @author iconmaster
 *
 */
public class ForLoopVariableCountError extends TyphonError {
	int expected, got;
	
	public ForLoopVariableCountError(SourceInfo source, int expected, int got) {
		super(source);
		this.expected = expected;
		this.got = got;
	}
	
	@Override
	public String getMessage() {
		return "for loop expected at most "+expected+" loop variables, got "+got;
	}
}

package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.util.SourceInfo;

public class SyntaxError extends TyphonError {
	String message;
	
	public SyntaxError(SourceInfo source, String message) {
		super(source);
		this.message = message;
	}

	@Override
	public String getMessage() {
		return "syntax error: "+message;
	}
}

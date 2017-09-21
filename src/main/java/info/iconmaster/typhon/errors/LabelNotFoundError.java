package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when a variable is referenced that does not exist.
 * 
 * @author iconmaster
 *
 */
public class LabelNotFoundError extends TyphonError {
	String s;
	
	public LabelNotFoundError(SourceInfo source, String s) {
		super(source);
		this.s = s;
	}
	
	@Override
	public String getMessage() {
		return "block with label "+s+" not found";
	}
}

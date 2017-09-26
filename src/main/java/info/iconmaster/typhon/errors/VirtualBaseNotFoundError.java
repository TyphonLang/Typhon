package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when a function has \@override, but no matching virtual base.
 * 
 * @author iconmaster
 *
 */
public class VirtualBaseNotFoundError extends TyphonError {
	Function f;
	
	public VirtualBaseNotFoundError(SourceInfo source, Function f) {
		super(source);
		this.f = f;
	}
	
	@Override
	public String getMessage() {
		return "Could not find a virtual base for override function "+f.prettyPrint();
	}
}

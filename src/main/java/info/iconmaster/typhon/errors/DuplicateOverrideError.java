package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is an error for when a function has \@override, but the virtual base already has a function registered for its type.
 * 
 * @author iconmaster
 *
 */
public class DuplicateOverrideError extends TyphonError {
	Type overrideType;
	Function override;
	Function virtual;
	
	public DuplicateOverrideError(SourceInfo source, Function virtual, Function override, Type overrideType) {
		super(source);
		this.virtual = virtual;
		this.override = override;
		this.overrideType = overrideType;
	}
	
	@Override
	public String getMessage() {
		return virtual.getName()+" already has a override for type "+overrideType.getName();
	}
}

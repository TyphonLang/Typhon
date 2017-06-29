package info.iconmaster.typhon.types;

import info.iconmaster.typhon.TyphonInput;

/**
 * 'Any' is the type all other types derive from.
 * 
 * @author iconmaster
 *
 */
public class AnyType extends Type {
	public AnyType(TyphonInput input) {
		super(input);
	}
	
	@Override
	public String getName() {
		return "Any";
	}
}
